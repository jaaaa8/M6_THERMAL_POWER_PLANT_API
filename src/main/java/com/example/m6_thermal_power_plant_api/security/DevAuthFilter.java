package com.example.m6_thermal_power_plant_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;  // DISABLED for Postman testing
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * DEV-ONLY filter: auto-injects a fake ADMIN principal so every request is
 * treated as authenticated without needing a real JWT token.
 *
 * Active only when the "dev" Spring profile is enabled.
 * To enable: set spring.profiles.active=dev in application.properties
 *            or pass --spring.profiles.active=dev as a JVM argument.
 *
 * Remove or disable this class before deploying to production.
 */
// @Component  // <-- DISABLED: filter not registered in SecurityFilterChain
@Profile("dev")
public class DevAuthFilter extends OncePerRequestFilter {

    private static final Integer FAKE_ACCOUNT_ID = 1;
    private static final String  FAKE_USERNAME   = "dev-admin";
    private static final List<String> FAKE_ROLES = List.of("ADMIN", "MANAGER");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            CustomUserDetails principal = CustomUserDetails.fromClaims(
                    FAKE_ACCOUNT_ID, FAKE_USERNAME, FAKE_ROLES);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}