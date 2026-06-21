package com.nurtureshare.backend.dto.response;

import com.nurtureshare.backend.model.enums.AppMode;
import com.nurtureshare.backend.model.enums.BabyGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettingsResponse {

    private UUID settingsId;
    private UUID userId;
    private String userEmail;
    private String userName;
    private boolean notificationsEnabled;
    private AppMode appMode;
    private CoupleStatusResponse coupleStatus;
    private LocalDateTime updatedAt;

    // Pregnancy info
    private LocalDate dueDate;
    private Integer currentWeek;
    private String trimester;
    private BabyGender babyGender;
    private String babyName;
}
