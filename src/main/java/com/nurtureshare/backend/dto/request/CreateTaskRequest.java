package com.nurtureshare.backend.dto.request;

import com.nurtureshare.backend.model.enums.TaskAssignment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Assignment is required")
    private TaskAssignment assignedTo;

    private LocalDate dueDate;

    private boolean nudgeOnSave = false;
}
