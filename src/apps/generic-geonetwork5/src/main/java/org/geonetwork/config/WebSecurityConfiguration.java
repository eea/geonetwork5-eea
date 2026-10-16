/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

package org.geonetwork.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.geonetwork.security.GeoNetworkOAuth2UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfiguration {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> clientRegistrations,
            @Autowired(required = false) GeoNetworkOAuth2UserService geoNetworkOAuth2UserService,
            @Value("${geonetwork.home: '/'}") String homeUrl,
            @Value("${geonetwork.signin:}") String signinUrl,
            @Value("${geonetwork.security.frameOptions.mode: 'DENY'}") String frameOptionMode,
            @Value("${geonetwork.security.frameOptions.allowFrom: ''}") String allowFrom)
            throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(requests -> requests.requestMatchers(
                                "/",
                                "/home",
                                "/test",
                                "/actuator/health/liveness",
                                "/actuator/health/readiness",
                                "**",
                                "/ogcapi-records/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()
                        .anyRequest()
                        .authenticated());

        // Only available when at least one spring.security.oauth2.client.registration is configured
        if (clientRegistrations.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth.permitAll()
                    .loginPage("/signin")
                    .userInfoEndpoint(
                            userInfo -> userInfo.oidcUserService(geoNetworkOAuth2UserService.oidcUserService())
                                    .userService(geoNetworkOAuth2UserService.userService())));
        }

        http.formLogin(form -> form.loginPage("/signin")
                        .loginProcessingUrl("/api/user/signin")
                        .successHandler((request, response, authentication) -> {
                            handleRedirectParam(request, response, homeUrl);
                        })
                        .failureUrl(StringUtils.isNotEmpty(signinUrl) ? signinUrl + "?failure=true" : "/signin?error")
                        //                        .defaultSuccessUrl("/", false)
                        .permitAll())
                .httpBasic(Customizer.withDefaults())
                //                .httpBasic(AbstractHttpConfigurer::disable)
                //                .httpBasic(basic ->
                //                        // No popup in browsers
                //                        basic.authenticationEntryPoint((request, response, authException) ->
                // response.sendError(
                //                                HttpStatus.UNAUTHORIZED.value(),
                // HttpStatus.UNAUTHORIZED.getReasonPhrase())))
                .logout(logout -> logout.logoutRequestMatcher(
                                PathPatternRequestMatcher.withDefaults().matcher("/api/user/signout"))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            handleRedirectParam(request, response, homeUrl);
                        }))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> {
                    headers.frameOptions(frameOptions -> {
                        switch (frameOptionMode) {
                            case "DENY":
                                frameOptions.deny();
                                break;
                            case "SAMEORIGIN":
                                frameOptions.sameOrigin();
                                break;
                            default:
                                frameOptions.deny();
                                break;
                        }
                    });
                    if ("ALLOW-FROM".equals(frameOptionMode)) {
                        headers.contentSecurityPolicy(
                                csp -> csp.policyDirectives(String.format("frame-ancestors '%s'", allowFrom)));
                    }
                });

        //    http.sessionManagement(
        //        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    private static void handleRedirectParam(HttpServletRequest request, HttpServletResponse response, String homeUrl)
            throws IOException {
        String redirectUrl = request.getParameter("redirectUrl");
        response.sendRedirect(StringUtils.isNotEmpty(redirectUrl) ? redirectUrl : request.getContextPath() + homeUrl);
    }
}
