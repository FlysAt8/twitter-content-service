package com.twitter.contentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMessageRequest {

    @NotBlank(message = "Текст сообщения не может быть пустым")
    @Size(max = 280, message = "Сообщение не должно превышать 280 символов")
    private String text;
}
