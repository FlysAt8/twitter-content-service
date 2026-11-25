
package com.twitter.contentservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Set<String> extractRoles(Authentication authentication) {
        if (authentication == null) {
            return Set.of();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5).toLowerCase())
                .collect(Collectors.toSet());
    }
}
