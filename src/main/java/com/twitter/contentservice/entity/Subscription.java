package com.twitter.contentservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("subscriptions")
public class Subscription {
    @Id
    private UUID id;
    private UUID followerId;
    private UUID followingId;
    private LocalDateTime createdAt;
}
