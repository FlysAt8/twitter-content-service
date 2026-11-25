package com.twitter.contentservice.service;

import com.twitter.contentservice.client.UserServiceClient;
import com.twitter.contentservice.dto.PageResponse;
import com.twitter.contentservice.dto.SubscriptionResponse;
import com.twitter.contentservice.dto.UserShortResponse;
import com.twitter.contentservice.entity.Subscription;
import com.twitter.contentservice.exceptions.BadRequestException;
import com.twitter.contentservice.exceptions.ConflictException;
import com.twitter.contentservice.exceptions.ForbiddenException;
import com.twitter.contentservice.exceptions.NotFoundException;
import com.twitter.contentservice.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserServiceClient userServiceClient;
    private final UserDirectory userDirectory;
    private final UserRoleService userRoleService;

    public SubscriptionResponse subscribe(UUID followerId, UUID followingId) {

        if (!userRoleService.hasAnyRole(followerId, "subscriber", "administrator")) {
            throw new ForbiddenException(
                    "Подписываться могут только subscriber и administrator"
            );
        }
        if (followerId.equals(followingId)) {
            throw new BadRequestException("Нельзя подписаться на самого себя");
        }
        if (!userServiceClient.exists(followingId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        int inserted = subscriptionRepository.insertIfAbsent(followerId, followingId, LocalDateTime.now());
        if (inserted == 0) {
            throw new ConflictException("Вы уже подписаны на этого пользователя");
        }

        Subscription subscription = subscriptionRepository
                .findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new NotFoundException("Подписка не найдена"));
        return toResponse(subscription);
    }

    public void unsubscribe(UUID followerId, UUID followingId) {
        if (!userRoleService.hasAnyRole(followerId, "subscriber", "administrator")) {
            throw new ForbiddenException(
                    "Отписываться могут только subscriber и administrator"
            );
        }
        int deleted = subscriptionRepository.deleteByPair(followerId, followingId);
        if (deleted == 0) {
            throw new NotFoundException("Подписка не найдена");
        }
    }

    public PageResponse<UserShortResponse> getFollowers(UUID userId, int limit, long offset) {
        int safeLimit = PageResponse.normalizeLimit(limit);
        long safeOffset = PageResponse.normalizeOffset(offset);

        List<Subscription> rows = subscriptionRepository.findFollowers(userId, safeLimit + 1, safeOffset);
        return PageResponse.of(rows, safeLimit, safeOffset, page -> toUsers(page, Subscription::getFollowerId));
    }

    public PageResponse<UserShortResponse> getFollowing(UUID userId, int limit, long offset) {
        int safeLimit = PageResponse.normalizeLimit(limit);
        long safeOffset = PageResponse.normalizeOffset(offset);

        List<Subscription> rows = subscriptionRepository.findFollowing(userId, safeLimit + 1, safeOffset);
        return PageResponse.of(rows, safeLimit, safeOffset, page -> toUsers(page, Subscription::getFollowingId));
    }

    private List<UserShortResponse> toUsers(List<Subscription> subscriptions,
                                            Function<Subscription, UUID> userIdExtractor) {
        List<UUID> ids = subscriptions.stream().map(userIdExtractor).toList();
        Map<UUID, UserShortResponse> users = userDirectory.lookup(ids);
        return ids.stream().map(users::get).toList();
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .followerId(subscription.getFollowerId())
                .followingId(subscription.getFollowingId())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}
