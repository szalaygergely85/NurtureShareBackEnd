package com.nurtureshare.backend.controller;

import com.nurtureshare.backend.dto.request.PregnancySetupRequest;
import com.nurtureshare.backend.dto.request.UpdateBabyInfoRequest;
import com.nurtureshare.backend.dto.response.ApiResponse;
import com.nurtureshare.backend.dto.response.TimelineResponse;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.service.TimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pregnancy")
@RequiredArgsConstructor
public class PregnancyController extends BaseController {

    private final TimelineService timelineService;

    @PutMapping("/setup")
    public ResponseEntity<ApiResponse<TimelineResponse>> setupPregnancy(
            @Valid @RequestBody PregnancySetupRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        User currentUser = getCurrentUser();
        TimelineResponse timeline = timelineService.setupPregnancy(currentUser, request, acceptLanguage);
        return ResponseEntity.ok(ApiResponse.ok("Due date saved successfully", timeline));
    }

    @PutMapping("/baby-info")
    public ResponseEntity<ApiResponse<TimelineResponse>> updateBabyInfo(
            @RequestBody UpdateBabyInfoRequest request) {
        User currentUser = getCurrentUser();
        TimelineResponse timeline = timelineService.updateBabyInfo(currentUser, request);
        return ResponseEntity.ok(ApiResponse.ok("Baby info updated successfully", timeline));
    }
}
