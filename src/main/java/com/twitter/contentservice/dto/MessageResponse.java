package com.twitter.contentservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private UUID id;
    private String text;
    private UserShortResponse author;
    private LocalDateTime createdAt;
}
