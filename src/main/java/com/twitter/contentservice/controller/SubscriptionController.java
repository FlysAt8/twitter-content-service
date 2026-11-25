package com.twitter.contentservice.controller;

import com.twitter.contentservice.dto.ApiResponse;
import com.twitter.contentservice.dto.CreateSubscriptionRequest;
import com.twitter.contentservice.dto.PageResponse;
import com.twitter.contentservice.dto.SubscriptionResponse;
import com.twitter.contentservice.dto.UserShortResponse;
import com.twitter.contentservice.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(@AuthenticationPrincipal UUID currentUserId,
                                                                       @Valid @RequestBody CreateSubscriptionRequest request) {

        SubscriptionResponse response = subscriptionService.subscribe(currentUserId, request.getFollowingId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @DeleteMapping("/{followingId}")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(@AuthenticationPrincipal UUID currentUserId,
                                                         @PathVariable UUID followingId) {
        subscriptionService.unsubscribe(currentUserId, followingId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Подписка удалена"));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<ApiResponse<PageResponse<UserShortResponse>>> getFollowers(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.getFollowers(userId, limit, offset)));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<ApiResponse<PageResponse<UserShortResponse>>> getFollowing(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.getFollowing(userId, limit, offset)));
    }
}
