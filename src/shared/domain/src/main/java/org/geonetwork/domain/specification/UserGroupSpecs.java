/*
 * (c) 2003 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license,
 * available at the root application directory.
 */
package org.geonetwork.domain.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.geonetwork.domain.Profile;
import org.geonetwork.domain.Usergroup;
import org.springframework.data.jpa.domain.Specification;

public final class UserGroupSpecs {

    private UserGroupSpecs() {
        // don't permit instantiation
    }

    public static Specification<Usergroup> hasUserId(final int userId) {
        return new Specification<Usergroup>() {
            @Override
            public Predicate toPredicate(Root<Usergroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> userIdAttributePath = root.get("id").get("userId");
                return cb.equal(userIdAttributePath, cb.literal(userId));
            }
        };
    }

    /**
     * Specification for retrieving all the Usergroups with a given profile.
     *
     * @param profile The {@link Profile} to filter the Usergroups.
     * @return the query.
     */
    public static Specification<Usergroup> hasProfile(final Profile profile) {
        return new Specification<Usergroup>() {
            @Override
            public Predicate toPredicate(Root<Usergroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Profile> profileIdAttributePath = root.get("id").get("profile");
                return cb.equal(profileIdAttributePath, cb.literal(profile));
            }
        };
    }
}
