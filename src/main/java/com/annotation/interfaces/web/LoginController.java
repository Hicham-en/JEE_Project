package com.annotation.interfaces.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller handling the login view navigation.
 */
@Controller
public class LoginController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) {
            return "redirect:/admin/datasets";
        }

        boolean isAnnotator = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ANNOTATOR"::equals);
        if (isAnnotator) {
            return "redirect:/annotator/workspace";
        }

        return "redirect:/login?error=role";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
            
        if (error != null) {
            model.addAttribute("roleError", "role".equals(error));
            model.addAttribute("error", !"role".equals(error));
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        
        return "login";
    }
}
