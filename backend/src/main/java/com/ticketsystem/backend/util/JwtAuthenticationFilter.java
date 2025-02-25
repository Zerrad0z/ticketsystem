package com.ticketsystem.backend.util;

import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter for handling JWT authentication
 * Extracts user ID from request header and sets up Spring Security context
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Processes each request to extract authentication information
     * Sets up security context if User-Id header is present
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @param filterChain The filter processing chain
     * @throws ServletException If a servlet error occurs
     * @throws IOException If an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {
        try {
            // Extract User-Id from header
            String userId = request.getHeader("User-Id");
            if (userId != null) {
                // Create authentication token with the userId as principal
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList());

                // Set authentication in Spring Security context
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
            // Continue filter chain even if authentication fails
        }

        // Proceed with the filter chain
        filterChain.doFilter(request, response);
    }
}