package com.twitter.contentservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SubscriptionResponse {
    private UUID id;
    private UUID followerId;
    private UUID followingId;
    private LocalDateTime createdAt;
}
