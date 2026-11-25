package com.twitter.contentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSubscriptionRequest {

    @NotNull(message = "followingId не может быть пустым")
    private UUID followingId;
}
