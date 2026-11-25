package com.twitter.contentservice.client;

import com.twitter.contentservice.dto.ApiResponse;
import com.twitter.contentservice.dto.UserShortResponse;
import com.twitter.contentservice.exceptions.UnauthorizedException;
import com.twitter.contentservice.exceptions.UserServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestClient restClient;

    public Optional<UserShortResponse> getUser(UUID id) {
        try {
            ApiResponse<UserShortResponse> response = restClient.get()
                    .uri("/users/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return Optional.ofNullable(response).map(ApiResponse::getData);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw translate(e);
        }
    }

    public boolean exists(UUID id) {
        return getUser(id).isPresent();
    }

    public Map<UUID, UserShortResponse> getUsers(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        String joinedIds = ids.stream().map(UUID::toString).collect(Collectors.joining(","));
        try {
            ApiResponse<List<UserShortResponse>> response = restClient.get()
                    .uri(uri -> uri.path("/users").queryParam("ids", joinedIds).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            Map<UUID, UserShortResponse> result = new HashMap<>();
            if (response != null && response.getData() != null) {
                response.getData().forEach(user -> result.put(user.getId(), user));
            }
            return result;
        } catch (RestClientException e) {
            throw translate(e);
        }
    }

    private RuntimeException translate(RestClientException e) {
        if (e instanceof HttpClientErrorException.Unauthorized) {
            return new UnauthorizedException("Токен не принят сервисом пользователей");
        }
        return new UserServiceUnavailableException("Ошибка обращения к user-service: " + e.getMessage(), e);
    }
}
