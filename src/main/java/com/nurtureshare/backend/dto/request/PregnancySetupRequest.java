package com.nurtureshare.backend.dto.request;

import com.nurtureshare.backend.model.enums.AppMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PregnancySetupRequest {

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private AppMode appMode;
}
