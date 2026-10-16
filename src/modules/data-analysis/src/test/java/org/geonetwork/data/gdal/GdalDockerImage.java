/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.geonetwork.data.gdal;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** GDAL image used to run the GDAL commands in the tests. */
final class GdalDockerImage {
    // Do not use a latest tag: it is a development build and its output changes.
    static final String NAME = "ghcr.io/osgeo/gdal:alpine-normal-3.13.3";

    private static boolean pulled = false;

    private GdalDockerImage() {}

    /** Pull the image once, so the first test using it does not wait for the download. */
    static synchronized void pull() throws IOException, InterruptedException {
        if (pulled) {
            return;
        }
        Process process = new ProcessBuilder("docker", "pull", NAME)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
        if (!process.waitFor(300, TimeUnit.SECONDS)) {
            process.destroyForcibly();
        }
        pulled = true;
    }

    static String runCommand(String dataFolder, String mountPoint) {
        return String.format("docker run --rm -v %s:%s %s ", dataFolder, mountPoint, NAME);
    }
}
