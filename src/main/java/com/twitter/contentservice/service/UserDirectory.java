package com.twitter.contentservice.service;

import com.twitter.contentservice.client.UserServiceClient;
import com.twitter.contentservice.dto.UserShortResponse;
import com.twitter.contentservice.exceptions.UserServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDirectory {

    private final UserServiceClient userServiceClient;

    public Map<UUID, UserShortResponse> lookup(Collection<UUID> ids) {
        Set<UUID> unique = new LinkedHashSet<>(ids);
        if (unique.isEmpty()) {
            return Map.of();
        }

        Map<UUID, UserShortResponse> found = Map.of();
        try {
            found = userServiceClient.getUsers(unique);
        } catch (UserServiceUnavailableException e) {
            log.warn("Не удалось получить пользователей, отдаём только id: {}", e.getMessage());
        }

        Map<UUID, UserShortResponse> result = new HashMap<>();
        for (UUID id : unique) {
            result.put(id, found.getOrDefault(id, UserShortResponse.unknown(id)));
        }
        return result;
    }
}
