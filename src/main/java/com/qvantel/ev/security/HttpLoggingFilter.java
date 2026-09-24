package com.qvantel.ev.security;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(HttpLoggingFilter.class);

    private static final String REQUEST_ID = "requestId";
    private static final String EVENT = "event";
    private static final String METHOD = "method";
    private static final String PATH = "path";
    private static final String STATUS = "status";
    private static final String DURATION_MS = "durationMs";
    private static final String USERNAME = "username";
    private static final String CLIENT_IP = "clientIp";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        String requestId = UUID.randomUUID().toString();

        try {
            /*
             * Basic request information
             */
            MDC.put(REQUEST_ID, requestId);
            MDC.put(METHOD, request.getMethod());
            MDC.put(PATH, request.getRequestURI());
            MDC.put(CLIENT_IP, getClientIp(request));

            /*
             * Execute the rest of the filter chain.
             *
             * This includes:
             * - Spring Security
             * - JWT filter
             * - Controller
             * - Service
             * - Exception handling
             */
            filterChain.doFilter(request, response);

        } finally {

            /*
             * Calculate request duration
             */
            long durationMs =
                    System.currentTimeMillis() - startTime;

            int status = response.getStatus();

            MDC.put(STATUS, String.valueOf(status));
            MDC.put(DURATION_MS, String.valueOf(durationMs));

            /*
             * Get authenticated username if available.
             */
            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getName() != null
                    && !"anonymousUser".equals(authentication.getName())) {

                MDC.put(USERNAME, authentication.getName());
            }

            /*
             * Determine event and log level based on HTTP status.
             */
            if (status >= 200 && status < 300) {

                if (status == 201) {

                    MDC.put(EVENT, "HTTP_REQUEST_CREATED");

                    log.info("Request completed successfully");

                } else if (status == 204) {

                    MDC.put(EVENT, "HTTP_REQUEST_NO_CONTENT");

                    log.info("Request completed successfully");

                } else {

                    MDC.put(EVENT, "HTTP_REQUEST_SUCCESS");

                    log.info("Request completed successfully");
                }

            } else if (status == 400) {

                MDC.put(EVENT, "HTTP_REQUEST_BAD_REQUEST");

                log.warn("Bad request");

            } else if (status == 401) {

                MDC.put(EVENT, "HTTP_REQUEST_UNAUTHORIZED");

                log.warn("Unauthorized request");

            } else if (status == 403) {

                MDC.put(EVENT, "HTTP_REQUEST_FORBIDDEN");

                log.warn("Forbidden request");

            } else if (status == 404) {

                MDC.put(EVENT, "HTTP_REQUEST_NOT_FOUND");

                log.warn("Resource not found");

            } else if (status == 405) {

                MDC.put(EVENT, "HTTP_REQUEST_METHOD_NOT_ALLOWED");

                log.warn("HTTP method not allowed");

            } else if (status >= 500) {

                MDC.put(EVENT, "HTTP_REQUEST_ERROR");

                log.error("Server error");

            } else {

                MDC.put(EVENT, "HTTP_REQUEST_COMPLETED");

                log.info("Request completed");
            }

            /*
             * Always clear MDC.
             *
             * Servlet containers reuse threads.
             * Without clearing MDC, information from one
             * request could appear in another request's log.
             */
            MDC.clear();
        }
    }

    /**
     * Get the client IP address.
     *
     * X-Forwarded-For is checked first because the application
     * may eventually run behind HAProxy, F5, Nginx, etc.
     */
    private String getClientIp(HttpServletRequest request) {

        String xForwardedFor =
                request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null
                && !xForwardedFor.isBlank()) {

            /*
             * X-Forwarded-For can contain:
             *
             * clientIP, proxy1, proxy2
             *
             * The first IP is normally the original client.
             */
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp =
                request.getHeader("X-Real-IP");

        if (xRealIp != null
                && !xRealIp.isBlank()) {

            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}