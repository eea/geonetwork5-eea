/*
 * (c) 2003 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license,
 * available at the root application directory.
 */
package org.geonetwork.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade implements IAuthenticationFacade {

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public boolean isAuthenticated() {
        if (getAuthentication() instanceof AnonymousAuthenticationToken) {
            return false;
        }
        if (!getAuthentication().isAuthenticated()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isAdmin() {
        Authentication auth = this.getAuthentication();
        if (auth.isAuthenticated()) {
            return auth.getAuthorities().stream()
                    .anyMatch(
                            grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    @Override
    public String getUsername() {
        Authentication auth = this.getAuthentication();
        if (auth.isAuthenticated()) {
            return auth.getName();
        }

        return "";
    }
}
