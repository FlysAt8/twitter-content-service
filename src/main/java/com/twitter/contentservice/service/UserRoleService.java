package com.twitter.contentservice.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.twitter.contentservice.client.UserServiceClient;
import com.twitter.contentservice.dto.UserDetailsResponse;
import com.twitter.contentservice.exceptions.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserServiceClient userServiceClient;

    public Set<String> getCurrentRoles(UUID userId) {
        Optional<UserDetailsResponse> user = userServiceClient.getUserDetails(userId);

        return user
                .map(details -> {
                    if (details.getRoles() == null) {
                        return Set.<String>of();
                    }

                    return details.getRoles().stream()
                            .filter(role -> role.getName() != null)
                            .map(role -> role.getName().toLowerCase())
                            .collect(Collectors.toSet());
                })
                .orElseThrow(() -> new UnauthorizedException("Пользователь не найден"));
            }

    public boolean hasAnyRole(UUID userId, String... requiredRoles) {
        Set<String> currentRoles = getCurrentRoles(userId);

        for (String role : requiredRoles) {
            if (currentRoles.contains(role.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}