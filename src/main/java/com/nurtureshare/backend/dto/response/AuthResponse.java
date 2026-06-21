package com.nurtureshare.backend.dto.response;

import com.nurtureshare.backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private UUID userId;
    private String email;
    private String name;
    private UserRole role;
}
