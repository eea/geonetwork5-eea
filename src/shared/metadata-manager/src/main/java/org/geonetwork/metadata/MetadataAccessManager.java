/*
 * (c) 2003 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license,
 * available at the root application directory.
 */
package org.geonetwork.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.geonetwork.domain.Group;
import org.geonetwork.domain.Metadata;
import org.geonetwork.domain.Operation;
import org.geonetwork.domain.Operationallowed;
import org.geonetwork.domain.Profile;
import org.geonetwork.domain.ReservedGroup;
import org.geonetwork.domain.ReservedOperation;
import org.geonetwork.domain.User;
import org.geonetwork.domain.Usergroup;
import org.geonetwork.domain.repository.GroupRepository;
import org.geonetwork.domain.repository.OperationRepository;
import org.geonetwork.domain.repository.UsergroupRepository;
import org.geonetwork.domain.specification.UserGroupSpecs;
import org.geonetwork.security.AuthenticationFacade;
import org.geonetwork.security.user.UserManager;
import org.geonetwork.utility.NetworkUtil;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@AllArgsConstructor
public class MetadataAccessManager implements IMetadataAccessManager {
    private final NetworkUtil networkUtil;
    private final MetadataManager metadataManager;
    private final UserManager userManager;
    private final AuthenticationFacade authenticationFacade;
    private final OperationRepository operationRepository;
    private final UsergroupRepository userGroupRepository;
    private final GroupRepository groupRepository;

    @Override
    public boolean canEdit(final int metadataId) throws Exception {
        return isOwner(metadataId) || hasEditPermission(metadataId);
    }

    @Override
    public boolean isOwner(final int metadataId) throws Exception {
        try {
            if (!this.authenticationFacade.isAuthenticated()) {
                return false;
            }

            String currentUsername = this.authenticationFacade.getUsername();
            if (!StringUtils.hasLength(currentUsername)) {
                return false;
            }
            User currentUser = this.userManager.getUserByUsername(currentUsername);

            // --- check if the user is an administrator
            Profile profile = currentUser.getProfile();
            if (profile == Profile.Administrator) {
                return true;
            }

            Metadata metadata = metadataManager.findMetadataById(metadataId);

            // --- check if the user is the metadata owner
            if (currentUser.getId().equals(metadata.getOwner())) {
                return true;
            }

            // --- check if the user is a reviewer or useradmin
            if (profile != Profile.Reviewer && profile != Profile.UserAdmin) {
                return false;
            }

            // --- if there is no group owner then the reviewer cannot review and the useradmin cannot administer
            final Integer groupOwner = metadata.getGroupowner();
            if (groupOwner == null) {
                return false;
            }

            List<Usergroup> userReviewerGroups = this.userManager.getUserGroups(currentUser.getId(), Profile.Reviewer);

            return userReviewerGroups.stream()
                    .anyMatch(usergroup -> usergroup.getGroupid().getId().equals(groupOwner));

        } catch (MetadataNotFoundException ex) {
            return false;
        }
    }

    @Override
    public boolean hasEditPermission(final int metadataId) throws Exception {
        return hasEditingPermissionWithProfile(metadataId);
    }

    /** Returns whether a particular metadata is downloadable. */
    @Override
    public boolean canDownload(final int metadataId) throws Exception {
        if (isOwner(metadataId)) {
            return true;
        }
        int downloadId = ReservedOperation.download.getId();

        Set<Operation> ops = getOperations(metadataId);
        for (Operation op : ops) {
            if (op.getId() == downloadId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canView(int metadataId) throws Exception {
        Set<Operation> hsOper = this.getOperations(metadataId);

        return hsOper.stream().anyMatch(op -> op.getId() == ReservedOperation.view.getId());
    }

    @Override
    public Set<Operation> getOperations(int metadataId) throws Exception {
        return getOperations(metadataId, null);
    }

    @Override
    public Set<Operation> getOperations(int metadataId, Collection<Operation> operations) throws Exception {
        Set<Operation> results;
        // if user is an administrator OR is the owner of the record then allow all operations
        if (isOwner(metadataId)) {
            results = new HashSet<>(this.metadataManager.getAvailableMetadataOperations());
        } else {
            if (operations == null) {
                results = new HashSet<>(getAllOperations(metadataId));
            } else {
                results = new HashSet<>(operations);
            }

            // TODO: Use user session
            /*UserSession us = context.getUserSession();
            if ((us != null) && us.isAuthenticated() && us.getProfile() == Profile.Editor && us.getProfile() == Profile.Reviewer) {
              results.add(operationRepository.findReservedOperation(ReservedOperation.view));
            }*/
        }

        return results;
    }

    @Override
    public Set<Operation> getAllOperations(int metadataId) throws Exception {
        HashSet<Operation> operations = new HashSet<>();
        Set<Integer> groups = getUserGroups(false);
        for (Operationallowed opAllow : this.metadataManager.getMetadataOperations(metadataId)) {
            if (groups.contains(opAllow.getId().getGroupid())) {
                operations.add(operationRepository
                        .findById(opAllow.getId().getOperationid())
                        .get());
            }
        }
        return operations;
    }

    /** Returns all groups accessible by the user (a set of ids). */
    public Set<Integer> getUserGroups(boolean editingGroupsOnly) throws Exception {
        Set<Integer> hs = new HashSet<>();

        // add All (1) network group
        hs.add(ReservedGroup.all.getId());

        Optional<String> ip = networkUtil.getClientIpAddress();
        if (ip.isPresent() && networkUtil.isIntranet(ip.get())) {
            hs.add(ReservedGroup.intranet.getId());
        }

        // get other groups
        if (authenticationFacade.isAuthenticated()) {
            // add (-1) GUEST group
            hs.add(ReservedGroup.guest.getId());

            if (authenticationFacade.isAdmin()) {
                List<Integer> allGroupIds =
                        groupRepository.findAll().stream().map(Group::getId).toList();
                hs.addAll(allGroupIds);
            } else {
                User currentUser = userManager.getUserByUsername(authenticationFacade.getUsername());
                Specification<Usergroup> spec = UserGroupSpecs.hasUserId(currentUser.getId());
                if (editingGroupsOnly) {
                    spec = Specification.where(spec).and(UserGroupSpecs.hasProfile(Profile.Editor));
                }

                List<Usergroup> usergroupList = userGroupRepository.findAll(spec);
                for (Usergroup ug : usergroupList) {
                    hs.add(ug.getGroupid().getId());
                }
            }
        }
        return hs;
    }

    /**
     * Check if current user has permission for the metadata according to the groups where the metadata is editable and
     * specific user profile.
     *
     * @param metadataId The metadata internal identifier
     */
    private boolean hasEditingPermissionWithProfile(final int metadataId) throws Exception {
        if (this.authenticationFacade.getAuthentication().isAuthenticated()) {
            return false;
        }

        String currentUsername = this.authenticationFacade.getUsername();
        if (!StringUtils.hasLength(currentUsername)) {
            return false;
        }
        User currentUser = this.userManager.getUserByUsername(currentUsername);

        // Get the groups where the metadata is editable
        List<Integer> metadataEditableGroups = this.metadataManager.getEditableGroups(metadataId);
        if (metadataEditableGroups.isEmpty()) {
            return false;
        }

        // Get the groups where the user is Editor
        List<Usergroup> userEditableGroups = this.userManager.getUserGroups(currentUser.getId());
        List<Integer> userEditableGroupsIds = userEditableGroups.stream()
                .map(usergroup -> usergroup.getId().getGroupid())
                .toList();

        return userEditableGroupsIds.stream()
                .filter(metadataEditableGroups::contains)
                .distinct()
                .findAny()
                .isPresent();
    }
}
