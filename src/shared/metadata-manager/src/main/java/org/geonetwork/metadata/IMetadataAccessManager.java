/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.geonetwork.metadata;

import java.util.Collection;
import java.util.Set;
import org.geonetwork.domain.Operation;

public interface IMetadataAccessManager {
    /**
     * Returns true if, and only if, at least one of these conditions is satisfied:
     *
     * <ul>
     *   <li>the user is owner (@see #isOwner)
     *   <li>the user has edit rights over the metadata
     * </ul>
     *
     * @param metadataId The metadata internal identifier
     */
    boolean canEdit(final int metadataId) throws Exception;

    boolean isOwner(final int metadataId) throws Exception;

    /**
     * Check if current user can edit the metadata according to the groups where the metadata is editable.
     *
     * @param metadataId The metadata internal identifier
     */
    boolean hasEditPermission(final int metadataId) throws Exception;

    boolean canDownload(final int id) throws Exception;

    boolean canView(final int metadataId) throws Exception;

    /**
     * Given a user(session) a list of groups and a metadata returns all operations that user can perform on that
     * metadata (a set of OPER_XXX as keys). If the user is authenticated the permissions are taken from the groups the
     * user belong. If the user is not authenticated, a dynamic group is assigned depending on user location (0 for
     * internal and 1 for external).
     */
    Set<Operation> getOperations(int mdId, String ip) throws Exception;

    Set<Operation> getOperations(int mdId, String ip, Collection<Operation> operations) throws Exception;

    /** Returns all operations permitted by the user on a particular metadata. */
    Set<Operation> getAllOperations(int mdId, String ip) throws Exception;
}
