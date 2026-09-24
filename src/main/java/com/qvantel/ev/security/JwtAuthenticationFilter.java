package com.qvantel.ev.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // ---------------------------------------------------------
        // 1. Get Authorization header
        // ---------------------------------------------------------

        String authorizationHeader = request.getHeader("Authorization");

        // No Authorization header
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ---------------------------------------------------------
        // 2. Extract JWT token
        // ---------------------------------------------------------

        String token = authorizationHeader.substring(7);

        try {

            // -----------------------------------------------------
            // 3. Extract username from JWT
            // -----------------------------------------------------

            String username = jwtService.extractUsername(token);

            // -----------------------------------------------------
            // 4. Check whether user is already authenticated
            // -----------------------------------------------------

            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                // -------------------------------------------------
                // 5. Validate JWT
                // -------------------------------------------------

                if (jwtService.isTokenValid(token, username)) {

                    // ---------------------------------------------
                    // 6. Create Spring Security Authentication
                    // ---------------------------------------------

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(
                                            new SimpleGrantedAuthority("ROLE_USER")
                                    )
                            );

                    // ---------------------------------------------
                    // 7. Put authentication into SecurityContext
                    // ---------------------------------------------

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception e) {

            // -----------------------------------------------------
            // Invalid / expired / malformed JWT
            // -----------------------------------------------------

            SecurityContextHolder.clearContext();

            // We don't return here.
            // Spring Security will decide whether the endpoint
            // requires authentication.
        }

        // ---------------------------------------------------------
        // 8. Continue request
        // ---------------------------------------------------------

        filterChain.doFilter(request, response);
    }
}