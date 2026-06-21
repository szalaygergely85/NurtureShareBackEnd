package com.nurtureshare.backend.dto.response;

import com.nurtureshare.backend.model.enums.AppMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimelineResponse {

    private int currentWeek;
    private String trimester;
    private int percentComplete;
    private int weeksLeft;
    private LocalDate dueDate;
    private AppMode appMode;
    private MilestoneResponse currentMilestone;
    private List<String> suggestedActions;
    private String partnerFocusTip;
    private String babyName;
    private String babyGender;
    private String checklistTitle;
    private List<String> checklistItems;
    private boolean partnerConnected;
}
