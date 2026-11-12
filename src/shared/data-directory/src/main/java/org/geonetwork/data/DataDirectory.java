/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.geonetwork.data;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataDirectory {

    @Value("${geonetwork.directory.data}")
    private String dataDir;

    /** Where metadata resources are stored: {@code filesystem} (default) or {@code s3}. */
    @Getter
    @Value("${geonetwork.directory.type:filesystem}")
    private String type;

    /**
     * S3 settings, used when {@link #isS3()}. The object key of a resource is {@code keyPrefix} followed by its path
     * relative to the data directory (e.g. {@code data/metadata_data/00100-00199/101/file.gpkg}).
     */
    @Getter
    @Value("${geonetwork.directory.s3.bucket:}")
    private String s3Bucket;

    @Getter
    @Value("${geonetwork.directory.s3.keyPrefix:}")
    private String s3KeyPrefix;

    /** Endpoint URL for non-AWS S3 (MinIO, LocalStack...), e.g. {@code http://localhost:4566}. Empty for AWS. */
    @Getter
    @Value("${geonetwork.directory.s3.endpoint:}")
    private String s3Endpoint;

    @Getter
    @Value("${geonetwork.directory.s3.region:}")
    private String s3Region;

    @Getter
    @Value("${geonetwork.directory.s3.accessKeyId:}")
    private String s3AccessKeyId;

    @Getter
    @Value("${geonetwork.directory.s3.secretAccessKey:}")
    private String s3SecretAccessKey;

    /** Use virtual-hosted style ({@code bucket.host/key}) instead of path style ({@code host/bucket/key}). */
    @Getter
    @Value("${geonetwork.directory.s3.virtualHosting:false}")
    private boolean s3VirtualHosting;

    public Path getDataDir() {
        return Paths.get(dataDir);
    }

    public Path getMetadataDir() {
        return Paths.get(dataDir).resolve("metadata_data");
    }

    public boolean isS3() {
        return "s3".equalsIgnoreCase(type);
    }

    /** S3 object key of a file located in the data directory. */
    public String toS3Key(Path pathInDataDirectory) {
        String relativePath =
                getDataDir().relativize(pathInDataDirectory).toString().replace('\\', '/');
        if (relativePath.startsWith("..")) {
            throw new IllegalArgumentException("Path is outside the data directory: " + pathInDataDirectory);
        }
        String prefix = s3KeyPrefix == null ? "" : s3KeyPrefix.replaceAll("^/+|/+$", "");
        return prefix.isEmpty() ? relativePath : prefix + "/" + relativePath;
    }

    public DataDirectory() {
        log.info("DataDirectory initialized");
        log.info(String.format("  Data directory: %s", this.dataDir));
    }
}
