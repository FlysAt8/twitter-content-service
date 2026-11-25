package com.twitter.contentservice.service;

import com.twitter.contentservice.client.UserServiceClient;
import com.twitter.contentservice.dto.CreateMessageRequest;
import com.twitter.contentservice.dto.MessageResponse;
import com.twitter.contentservice.dto.PageResponse;
import com.twitter.contentservice.dto.UserShortResponse;
import com.twitter.contentservice.entity.Message;
import com.twitter.contentservice.exceptions.ForbiddenException;
import com.twitter.contentservice.exceptions.NotFoundException;
import com.twitter.contentservice.exceptions.UnauthorizedException;
import com.twitter.contentservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserServiceClient userServiceClient;
    private final MessageAssembler assembler;
    private final UserRoleService userRoleService;

    public MessageResponse create(UUID authorId, CreateMessageRequest request) {

        if (!userRoleService.hasAnyRole(authorId, "producer", "administrator")) {
            throw new ForbiddenException(
                    "Создавать сообщения могут только producer и administrator"
            );
        }

        UserShortResponse author = userServiceClient.getUser(authorId)
                .orElseThrow(() -> new UnauthorizedException("Пользователь не найден"));

        Message saved = messageRepository.save(Message.builder()
                .text(request.getText().trim())
                .authorId(authorId)
                .createdAt(LocalDateTime.now())
                .build());

        return assembler.toResponse(saved, author);
    }

    public MessageResponse getById(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Сообщение не найдено"));
        return assembler.toResponses(List.of(message)).getFirst();
    }

    public PageResponse<MessageResponse> getByAuthor(UUID authorId, int limit, long offset) {
        int safeLimit = PageResponse.normalizeLimit(limit);
        long safeOffset = PageResponse.normalizeOffset(offset);

        List<Message> rows = messageRepository.findByAuthor(authorId, safeLimit + 1, safeOffset);
        return PageResponse.of(rows, safeLimit, safeOffset, assembler::toResponses);
    }

    public void delete(UUID currentUserId, UUID id) {

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Сообщение не найдено"));

        boolean isAdministrator = userRoleService.hasAnyRole(
                currentUserId, "administrator"
        );

        boolean isOwner = message.getAuthorId().equals(currentUserId);

        if (!isAdministrator && !isOwner) {
            throw new ForbiddenException(
                    "Можно удалять только свои сообщения"
            );
        }

        if (!isAdministrator && !userRoleService.hasAnyRole(currentUserId, "producer")) {
            throw new ForbiddenException(
                    "Удалять сообщения могут только producer и administrator"
            );
        }

        messageRepository.delete(message);
    }
}
