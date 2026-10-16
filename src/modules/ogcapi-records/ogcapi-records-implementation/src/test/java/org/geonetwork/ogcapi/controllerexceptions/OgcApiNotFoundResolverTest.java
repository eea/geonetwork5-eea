/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.geonetwork.ogcapi.controllerexceptions;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class OgcApiNotFoundResolverTest {

    private final OgcApiNotFoundResolver resolver = new OgcApiNotFoundResolver(new ObjectMapper());

    private static HttpServletRequest request(String contextPath, String path) {
        var request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn(contextPath);
        when(request.getRequestURI()).thenReturn(contextPath + path);
        return request;
    }

    @Test
    void unknownOgcApiPathGetsAnOgcApiError() throws Exception {
        var body = new ByteArrayOutputStream();
        var response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {}

            @Override
            public void write(int b) throws IOException {
                body.write(b);
            }
        });
        var ex = new NoResourceFoundException(HttpMethod.GET, "ogcapi-records/does-not-exist");

        var result =
                resolver.resolveException(request("/catalogue", "/ogcapi-records/does-not-exist"), response, null, ex);

        assertNotNull(result);
        verify(response).setStatus(404);
        assertTrue(body.toString(StandardCharsets.UTF_8).contains("\"code\":\"404\""));
    }

    @Test
    void otherPathsAreLeftToTheOtherResolvers() {
        var response = mock(HttpServletResponse.class);
        var ex = new NoResourceFoundException(HttpMethod.GET, "does-not-exist");

        assertNull(resolver.resolveException(request("", "/does-not-exist"), response, null, ex));
        assertNull(resolver.resolveException(request("", "/ogcapi-recordsX"), response, null, ex));
        verifyNoInteractions(response);
    }

    @Test
    void otherExceptionsAreLeftToTheOtherResolvers() {
        var response = mock(HttpServletResponse.class);

        assertNull(resolver.resolveException(
                request("", "/ogcapi-records/collections"), response, null, new IllegalStateException("boom")));
        verifyNoInteractions(response);
    }
}
