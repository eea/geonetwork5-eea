/*
 * (c) 2003 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license,
 * available at the root application directory.
 */
package org.geonetwork.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.geonetwork.domain.Setting;
import org.geonetwork.domain.SettingKey;
import org.geonetwork.domain.repository.SettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class NetworkUtilTest {
    private SettingRepository settingRepository;
    private NetworkUtil networkUtil;

    @BeforeEach
    void setUp() {
        settingRepository = mock(SettingRepository.class);
        networkUtil = new NetworkUtil(settingRepository);
    }

    @Test
    void getClientIpAddress_ReturnsFirstValidHeaderIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(anyString())).thenAnswer(invocation -> {
            String argument = invocation.getArgument(0);
            if ("X-Forwarded-For".equals(argument)) {
                return "203.0.113.1, 10.0.0.1";
            }
            return null;
        });
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        when(attrs.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attrs);

        Optional<String> ip = networkUtil.getClientIpAddress();
        assertEquals("203.0.113.1", ip.orElse(null));
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getClientIpAddress_FallbacksToRemoteAddr() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(anyString())).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("198.51.100.2");
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        when(attrs.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attrs);

        Optional<String> ip = networkUtil.getClientIpAddress();
        assertEquals("198.51.100.2", ip.orElse(null));
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getClientIpAddress_ReturnsEmptyOptionalIfNoRequest() {
        RequestContextHolder.resetRequestAttributes();
        Optional<String> ip = networkUtil.getClientIpAddress();
        assertTrue(ip.isEmpty());
    }

    @Test
    void isLocalhost_ReturnsTrueForIPv4Loopback() {
        assertTrue(networkUtil.isLocalhost("127.0.0.1"));
    }

    @Test
    void isLocalhost_ReturnsTrueForIPv6Loopback() {
        assertTrue(networkUtil.isLocalhost("0:0:0:0:0:0:0:1"));
    }

    @Test
    void isLocalhost_ReturnsFalseForNonLoopback() {
        assertFalse(networkUtil.isLocalhost("192.168.1.1"));
    }

    @Test
    void isIntranet_ReturnsTrueForIPv6LinkLocal() {
        assertTrue(networkUtil.isIntranet("fe80::1"));
    }

    @Test
    void isIntranet_ReturnsFalseForOtherIPv6() {
        assertFalse(networkUtil.isIntranet("2001:db8::1"));
    }

    @Test
    void isIntranet_ReturnsTrueForConfiguredNetwork() {
        setupNetworkSettings("10.0.0.0", "255.0.0.0");
        assertTrue(networkUtil.isIntranet("10.1.1.42"));
    }

    @Test
    void isIntranet_ReturnsTrueForMultipleConfiguredNetwork() {
        setupNetworkSettings("10.0.0.0,11.0.0.0", "255.0.0.0,255.0.0.0");
        assertTrue(networkUtil.isIntranet("10.1.1.42"));
        assertTrue(networkUtil.isIntranet("11.1.1.42"));
    }

    @Test
    void isIntranet_ReturnsFalseForNonConfiguredNetwork() {
        setupNetworkSettings("10.0.0.0", "255.0.0.0");
        assertFalse(networkUtil.isIntranet("192.168.1.42"));
    }

    @Test
    void isIntranet_ReturnsFalseForInvalidSettings() {
        setupNetworkSettings("192.168.1.0", "255.255.255.0,255.0.0.0");
        assertFalse(networkUtil.isIntranet("192.168.1.42"));
    }

    @Test
    void getAddress_ReturnsZeroForUnknownOrQuestionMark() {
        assertEquals(0, networkUtil.getAddress("unknown"));
        assertEquals(0, networkUtil.getAddress("?"));
    }

    @Test
    void getAddress_ReturnsZeroForInvalidIpFormat() {
        assertEquals(0, networkUtil.getAddress("not.an.ip"));
        assertEquals(0, networkUtil.getAddress("192.168.1"));
    }

    @Test
    void getAddress_ReturnsLongForValidIp() {
        assertEquals((192L << 24) | (168L << 16) | (1L << 8) | 42L, networkUtil.getAddress("192.168.1.42"));
    }

    private void setupNetworkSettings(String value, String value1) {
        when(settingRepository.findById(SettingKey.SYSTEM_INTRANET_NETWORK))
                .thenReturn(Optional.of(Setting.builder()
                        .name(SettingKey.SYSTEM_INTRANET_NETWORK)
                        .value(value)
                        .build()));
        when(settingRepository.findById(SettingKey.SYSTEM_INTRANET_NETMASK))
                .thenReturn(Optional.of(Setting.builder()
                        .name(SettingKey.SYSTEM_INTRANET_NETMASK)
                        .value(value1)
                        .build()));
    }
}
