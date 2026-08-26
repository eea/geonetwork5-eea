/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
/*
 * (c) 2003 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license,
 * available at the root application directory.
 */
package org.geonetwork.data;

import java.util.Optional;
import org.geonetwork.data.model.RasterInfo;

/** Raster data analyzer provides information about a raster data file. */
public interface RasterDataAnalyzer extends DataAnalyzer {
    Optional<RasterInfo> getRasterProperties(String rasterSource);
}
