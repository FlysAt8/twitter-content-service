package com.twitter.contentservice.controller;

import com.twitter.contentservice.dto.ApiResponse;
import com.twitter.contentservice.dto.CreateMessageRequest;
import com.twitter.contentservice.dto.MessageResponse;
import com.twitter.contentservice.dto.PageResponse;
import com.twitter.contentservice.service.MessageService;
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
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> createMessage(@AuthenticationPrincipal UUID currentUserId,
                                                                      @Valid @RequestBody CreateMessageRequest request) {
        MessageResponse response = messageService.create(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessage(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MessageResponse>>> getMessagesByAuthor(
            @RequestParam UUID authorId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.getByAuthor(authorId, limit, offset)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@AuthenticationPrincipal UUID currentUserId,
                                                           @PathVariable UUID id) {
        messageService.delete(currentUserId, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Сообщение удалено"));
    }
}
