package com.nurtureshare.backend.controller;

import com.nurtureshare.backend.dto.request.PairRequest;
import com.nurtureshare.backend.dto.response.ApiResponse;
import com.nurtureshare.backend.dto.response.CoupleStatusResponse;
import com.nurtureshare.backend.model.Couple;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.service.CoupleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/couple")
@RequiredArgsConstructor
public class CoupleController extends BaseController {

    private final CoupleService coupleService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<CoupleStatusResponse>> getStatus() {
        User currentUser = getCurrentUser();
        CoupleStatusResponse status = coupleService.getStatus(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    @PostMapping("/pair")
    public ResponseEntity<ApiResponse<CoupleStatusResponse>> pairWithPartner(@Valid @RequestBody PairRequest request) {
        User currentUser = getCurrentUser();
        CoupleStatusResponse status = coupleService.pairWithPartner(currentUser, request.getPairingCode());
        return ResponseEntity.ok(ApiResponse.ok("Successfully paired with partner!", status));
    }

    @GetMapping("/pairing-code")
    public ResponseEntity<ApiResponse<String>> getPairingCode() {
        User currentUser = getCurrentUser();
        Couple couple = coupleService.getCoupleForUser(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(couple.getPairingCode()));
    }
}
