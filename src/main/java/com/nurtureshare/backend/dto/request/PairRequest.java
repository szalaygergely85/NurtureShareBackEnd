package com.nurtureshare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PairRequest {

    @NotBlank(message = "Pairing code is required")
    private String pairingCode;
}
