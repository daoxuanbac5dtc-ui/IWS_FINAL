package org.example.iws_websitesneaker.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Check if this is an AJAX request
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");

        if ("XMLHttpRequest".equals(requestedWith) ||
                (accept != null && accept.contains("application/json"))) {

            // Return JSON error response instead of triggering Basic Auth
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"success\": false, \"message\": \"Vui lÃ²ng Ä‘Äƒng nháº­p\", \"data\": null}"
            );
        } else {
            // For regular requests, redirect to login page
            response.sendRedirect("/auth/login");
        }
    }
}
