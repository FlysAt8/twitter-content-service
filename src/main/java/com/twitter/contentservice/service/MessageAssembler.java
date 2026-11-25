package com.twitter.contentservice.service;

import com.twitter.contentservice.dto.MessageResponse;
import com.twitter.contentservice.dto.UserShortResponse;
import com.twitter.contentservice.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageAssembler {

    private final UserDirectory userDirectory;

    public MessageResponse toResponse(Message message, UserShortResponse author) {
        return MessageResponse.builder()
                .id(message.getId())
                .text(message.getText())
                .author(author)
                .createdAt(message.getCreatedAt())
                .build();
    }

    public List<MessageResponse> toResponses(List<Message> messages) {
        Map<UUID, UserShortResponse> authors = userDirectory.lookup(
                messages.stream().map(Message::getAuthorId).toList());
        return messages.stream()
                .map(message -> toResponse(message, authors.get(message.getAuthorId())))
                .toList();
    }
}
