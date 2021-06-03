package com.andrey.springs3restapi.dto.auth;

import lombok.Data;

@Data
public class AuthenticationResponseDto {
    private String username;
    private String jwtToken;

}
