package com.erp.security;

import com.erp.entity.User;
import com.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Utility methods for accessing the currently authenticated user
 * from anywhere in the application via SecurityContextHolder.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /** Returns the email of the currently logged-in user. */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in SecurityContext");
        }
        return auth.getName();
    }

    /** Returns the full User entity of the currently logged-in user. */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Authenticated user not found in DB: " + email
            ));
    }

    /** Returns the ID of the currently logged-in user. */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /** Returns true if the current user has the given role. */
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
