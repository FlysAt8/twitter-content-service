package com.twitter.contentservice.service;

import com.twitter.contentservice.dto.MessageResponse;
import com.twitter.contentservice.dto.PageResponse;
import com.twitter.contentservice.entity.Message;
import com.twitter.contentservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final MessageRepository messageRepository;
    private final MessageAssembler assembler;

    public PageResponse<MessageResponse> getFeed(UUID userId, int limit, long offset) {
        int safeLimit = PageResponse.normalizeLimit(limit);
        long safeOffset = PageResponse.normalizeOffset(offset);

        List<Message> rows = messageRepository.findFeed(userId, safeLimit + 1, safeOffset);
        return PageResponse.of(rows, safeLimit, safeOffset, assembler::toResponses);
    }
}
