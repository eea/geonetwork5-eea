/*
 * (c) 2003 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license,
 * available at the root application directory.
 */
package org.geonetwork.domain.repository;

import java.util.List;
import org.geonetwork.domain.Usergroup;
import org.geonetwork.domain.UsergroupId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface UsergroupRepository
        extends CrudRepository<Usergroup, UsergroupId>, JpaSpecificationExecutor<Usergroup> {
    List<Usergroup> findAllByUserid_Id(Integer id);
}
