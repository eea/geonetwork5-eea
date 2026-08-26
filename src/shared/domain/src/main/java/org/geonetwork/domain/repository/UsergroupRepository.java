/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
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
