/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.geonetwork;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.geonetwork.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Both the error-handling and the OGC API Records modules define a GlobalExceptionHandler. They must be able to live in
 * the same application context: they used to fail at startup with a ConflictingBeanDefinitionException.
 */
class ExceptionHandlerBeansTest {

    @Test
    void bothGlobalExceptionHandlersCanBeRegistered() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(
                    GlobalExceptionHandler.class,
                    org.geonetwork.ogcapi.controllerexceptions.GlobalExceptionHandler.class);
            context.refresh();

            assertNotNull(context.getBean(GlobalExceptionHandler.class));
            assertNotNull(context.getBean(org.geonetwork.ogcapi.controllerexceptions.GlobalExceptionHandler.class));
        }
    }
}
