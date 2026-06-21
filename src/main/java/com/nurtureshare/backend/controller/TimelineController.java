package com.nurtureshare.backend.controller;

import com.nurtureshare.backend.dto.response.ApiResponse;
import com.nurtureshare.backend.dto.response.TimelineResponse;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
public class TimelineController extends BaseController {

    private final TimelineService timelineService;

    @GetMapping
    public ResponseEntity<ApiResponse<TimelineResponse>> getTimeline(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        User currentUser = getCurrentUser();
        TimelineResponse timeline = timelineService.getTimeline(currentUser, acceptLanguage);
        return ResponseEntity.ok(ApiResponse.ok(timeline));
    }
}
