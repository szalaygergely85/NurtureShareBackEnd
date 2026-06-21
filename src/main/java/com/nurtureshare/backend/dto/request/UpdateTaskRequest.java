package com.nurtureshare.backend.dto.request;

import com.nurtureshare.backend.model.enums.TaskAssignment;
import com.nurtureshare.backend.model.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {

    private String title;
    private String description;
    private TaskAssignment assignedTo;
    private LocalDate dueDate;
    private TaskStatus status;
    private Boolean nudgeOnSave;
}
