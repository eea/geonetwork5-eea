/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.geonetwork.ogcapi.controllerexceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geonetwork.ogcapi.records.generated.model.OgcApiRecordsExceptionDto;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Answers the requests to an unknown path of the OGC API Records with an OgcApiRecordsExceptionDto.
 *
 * <p>Those requests do not reach a controller, so the exception handler of the OGC API controllers does not apply. All
 * the other exceptions and paths are left to the other resolvers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OgcApiNotFoundResolver implements HandlerExceptionResolver, Ordered {
    private static final String OGC_API_PATH = "/ogcapi-records";

    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public ModelAndView resolveException(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!(ex instanceof NoResourceFoundException) || !isOgcApiRequest(request)) {
            return null;
        }
        log.debug(ex.getMessage());
        try {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(
                    response.getOutputStream(),
                    new OgcApiRecordsExceptionDto().code("404").description(ex.getMessage()));
        } catch (IOException e) {
            log.debug("Could not write the not found response: {}", e.getMessage());
        }
        return new ModelAndView();
    }

    private static boolean isOgcApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.equals(OGC_API_PATH) || path.startsWith(OGC_API_PATH + "/");
    }
}
