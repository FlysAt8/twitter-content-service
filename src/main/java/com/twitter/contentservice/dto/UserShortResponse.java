package com.twitter.contentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserShortResponse {
    private UUID id;
    private String firstname;
    private String lastname;

    public static UserShortResponse unknown(UUID id) {
        return new UserShortResponse(id, null, null);
    }
}
