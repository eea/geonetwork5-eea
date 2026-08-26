/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
/*
 * (c) 2003 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license,
 * available at the root application directory.
 */
package org.geonetwork.utility;

import static org.geonetwork.domain.SettingKey.SYSTEM_INTRANET_IP_SEPARATOR;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Optional;
import java.util.StringTokenizer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.geonetwork.domain.Setting;
import org.geonetwork.domain.SettingKey;
import org.geonetwork.domain.repository.SettingRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@AllArgsConstructor
@Slf4j
public class NetworkUtil {
    private final SettingRepository settingRepository;

    private static final String[] IP_HEADER_CANDIDATES = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR" // Fallback to the direct remote address
    };

    /**
     * Retrieves the client's public IP address from the HTTP request. Checks common proxy headers before falling back
     * to the direct remote address.
     *
     * @return The IP address string.
     */
    public Optional<String> getClientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return Optional.empty();
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);

            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {

                // X-Forwarded-For often contains a list (e.g., client, proxy1, proxy2)
                // We take the first non-internal address, which should be the original client.
                String ip = ipList.split("\\,", 2)[0].trim();

                // Optional: You might want to filter out internal IPs here if necessary
                if (!"127.0.0.1".equals(ip) && !ip.startsWith("10.") && !ip.startsWith("192.168.")) {
                    return Optional.of(ip);
                }
            }
        }

        // Fallback: Use the direct remote address if no proxy header was useful
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null ? Optional.empty() : Optional.of(remoteAddr);
    }

    /**
     * Retrieves the HttpServletRequest object from the global Spring context (the thread-local storage via
     * RequestContextHolder). * NOTE: This only works if a request is actively being processed by the current thread.
     *
     * @return The current HttpServletRequest, or null if outside a web request context.
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra != null) {
            return sra.getRequest();
        }
        return null;
    }

    public boolean isLocalhost(String ip) {
        return ip.startsWith("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1");
    }

    public boolean isIntranet(String ip) {
        // --- consider IPv4 & IPv6 loopback
        // --- we use 'startsWith' because some addresses can be 0:0:0:0:0:0:0:1%0
        if (isLocalhost(ip)) return true;

        // IPv6 link-local
        String ipv6LinkLocalPrefix = "fe80:";
        if (ip.toLowerCase(Locale.getDefault()).startsWith(ipv6LinkLocalPrefix)) {
            return true;
        }
        // other IPv6
        else if (ip.indexOf(':') >= 0) {
            return false;
        }

        // IPv4
        Optional<Setting> network = settingRepository.findById(SettingKey.SYSTEM_INTRANET_NETWORK);
        Optional<Setting> netmask = settingRepository.findById(SettingKey.SYSTEM_INTRANET_NETMASK);

        try {
            if (network.isPresent()
                    && netmask.isPresent()
                    && StringUtils.isNotEmpty(network.get().getValue())
                    && StringUtils.isNotEmpty(netmask.get().getValue())) {
                long lAddress = getAddress(ip.split("\\,", 2)[0]);
                String[] networkArray = network.get().getValue().split(SYSTEM_INTRANET_IP_SEPARATOR);
                String[] netmaskArray = netmask.get().getValue().split(SYSTEM_INTRANET_IP_SEPARATOR);

                if (isValidIntranetSettings(networkArray, netmaskArray)) {
                    for (int i = 0; i < networkArray.length; i++) {
                        long lIntranetNet = getAddress(networkArray[i]);
                        long lIntranetMask = getAddress(netmaskArray[i]);
                        if ((lAddress & lIntranetMask) == (lIntranetNet & lIntranetMask)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } catch (Exception nfe) {
            log.error("isIntranet error: " + nfe.getMessage(), nfe);
        }
        return false;
    }

    private boolean isValidIntranetSettings(String[] networkArray, String[] netmaskArray) {
        if (networkArray.length != netmaskArray.length) {
            log.error(String.format(
                    "Invalid intranet configuration. Define as many network mask (currently %d) as network ip (currently %d). Check Settings > Intranet.",
                    netmaskArray.length, networkArray.length));
            return false;
        } else {
            return true;
        }
    }

    /** Converts an ip x.x.x.x into a long. */
    protected long getAddress(String ip) {
        if (ip.trim().equals("?") || ip.trim().equals("unknown")) {
            return 0;
        } else {
            try {
                StringTokenizer st = new StringTokenizer(ip, ".");
                if (!st.hasMoreElements()) {
                    return 0;
                }
                long a1 = Integer.parseInt(st.nextToken());
                if (!st.hasMoreElements()) {
                    return 0;
                }
                long a2 = Integer.parseInt(st.nextToken());
                if (!st.hasMoreElements()) {
                    return 0;
                }
                long a3 = Integer.parseInt(st.nextToken());
                if (!st.hasMoreElements()) {
                    return 0;
                }
                long a4 = Integer.parseInt(st.nextToken());
                return a1 << 24 | a2 << 16 | a3 << 8 | a4;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}
