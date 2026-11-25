package com.twitter.contentservice.controller;

import com.twitter.contentservice.dto.ApiResponse;
import com.twitter.contentservice.dto.MessageResponse;
import com.twitter.contentservice.dto.PageResponse;
import com.twitter.contentservice.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MessageResponse>>> getFeed(
            @AuthenticationPrincipal UUID currentUserId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return ResponseEntity.ok(ApiResponse.ok(feedService.getFeed(currentUserId, limit, offset)));
    }
}
