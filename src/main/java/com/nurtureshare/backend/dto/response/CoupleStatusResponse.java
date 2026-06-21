package com.nurtureshare.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoupleStatusResponse {

    private UUID coupleId;
    private String pairingCode;
    private boolean connected;
    private UserResponse partner;
    private LocalDateTime syncedAt;
    private LocalDateTime createdAt;
}
