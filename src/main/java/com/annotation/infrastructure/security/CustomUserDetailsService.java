package com.annotation.infrastructure.security;

import com.annotation.domain.entity.User;
import com.annotation.domain.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom implementation of UserDetailsService to load our JPA User entities.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their login.
     *
     * @param login the login username
     * @return UserDetails representing the authenticated user
     * @throws UsernameNotFoundException if the user is not found
     * @throws DisabledException if the user account is soft deleted / inactive
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + login));

        if (!user.isActive()) {
            throw new DisabledException("Compte désactivé");
        }

        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(toSpringAuthority(role.getNomRole())))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())
                .password(user.getPasswordHash())
                .disabled(!user.isActive())
                .authorities(authorities)
                .build();
    }

    private String toSpringAuthority(String roleName) {
        String normalized = roleName.toUpperCase();
        if (normalized.endsWith("_ROLE")) {
            normalized = normalized.substring(0, normalized.length() - "_ROLE".length());
        }
        if (normalized.startsWith("ROLE_")) {
            return normalized;
        }
        return "ROLE_" + normalized;
    }
}
