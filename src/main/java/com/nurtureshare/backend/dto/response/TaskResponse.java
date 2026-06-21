package com.nurtureshare.backend.dto.response;

import com.nurtureshare.backend.model.enums.TaskAssignment;
import com.nurtureshare.backend.model.enums.TaskStatus;
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
public class TaskResponse {

    private UUID id;
    private UUID coupleId;
    private UserResponse createdBy;
    private TaskAssignment assignedTo;
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;
    private boolean nudgeOnSave;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
