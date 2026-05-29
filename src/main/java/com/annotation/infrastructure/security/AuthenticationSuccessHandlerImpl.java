package com.annotation.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * Custom success handler that redirects users depending on their assigned roles.
 */
@Component
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        boolean isAnnotator = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ANNOTATOR"));

        if (isAdmin) {
            response.sendRedirect(request.getContextPath() + "/admin/datasets");
        } else if (isAnnotator) {
            response.sendRedirect(request.getContextPath() + "/annotator/workspace");
        } else {
            response.sendRedirect(request.getContextPath() + "/login?error=role");
        }
    }
}
