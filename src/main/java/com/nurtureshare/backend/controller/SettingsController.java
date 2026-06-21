package com.nurtureshare.backend.controller;

import com.nurtureshare.backend.dto.request.UpdateSettingsRequest;
import com.nurtureshare.backend.dto.response.ApiResponse;
import com.nurtureshare.backend.dto.response.SettingsResponse;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController extends BaseController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<SettingsResponse>> getSettings() {
        User currentUser = getCurrentUser();
        SettingsResponse settings = settingsService.getSettings(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<SettingsResponse>> updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request) {
        User currentUser = getCurrentUser();
        SettingsResponse settings = settingsService.updateSettings(currentUser, request);
        return ResponseEntity.ok(ApiResponse.ok("Settings updated successfully", settings));
    }
}
