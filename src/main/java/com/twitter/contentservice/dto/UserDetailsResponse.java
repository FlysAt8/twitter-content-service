package com.twitter.contentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailsResponse {

    private UUID id;
    private List<RoleResponse> roles;
}