/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
/*
 * (c) 2003 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license,
 * available at the root application directory.
 */
package org.geonetwork.metadata.datadir;

import java.nio.file.Path;
import lombok.NoArgsConstructor;
import org.geonetwork.metadata.MetadataNotFoundException;

@NoArgsConstructor
public class DefaultMetadataDirPathProcessor implements IMetadataDirProcessor {
    private MetadataDirPrivileges metadataDirectoryPrivileges = MetadataDirPrivileges.NONE;

    public DefaultMetadataDirPathProcessor(MetadataDirPrivileges metadataDirectoryPrivileges) {
        this.metadataDirectoryPrivileges = metadataDirectoryPrivileges;
    }

    @Override
    public Path calculatePath(Path dataDir, int metadataId) throws MetadataNotFoundException {
        String group = pad(metadataId / 100, 3);
        String groupDir = group + "00-" + group + "99";
        return dataDir.resolve(groupDir).resolve(String.valueOf(metadataId));
    }

    @Override
    public Path calculatePathWithAccess(Path dataDir, String access, int metadataId) throws MetadataNotFoundException {
        if (metadataDirectoryPrivileges.equals(MetadataDirPrivileges.DEFAULT)) {
            // Remove in MEF3            return calculatePath(dataDir, metadataId).resolve(access);
            return calculatePath(dataDir, metadataId);
        } else {
            return calculatePath(dataDir, metadataId);
        }
    }

    private String pad(int group, int length) {
        String text = Integer.toString(group);

        while (text.length() < length) text = "0" + text;

        return text;
    }
}
