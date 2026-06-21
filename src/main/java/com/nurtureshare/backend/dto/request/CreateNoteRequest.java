package com.nurtureshare.backend.dto.request;

import com.nurtureshare.backend.model.enums.NoteCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNoteRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Category is required")
    private NoteCategory category;

    private boolean syncedWithPartner = true;
}
