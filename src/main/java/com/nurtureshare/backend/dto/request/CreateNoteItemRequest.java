package com.nurtureshare.backend.dto.request;

import com.nurtureshare.backend.model.enums.NoteItemType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateNoteItemRequest {

    @NotBlank(message = "Content is required")
    private String content;

    private NoteItemType itemType = NoteItemType.CHECKLIST;

    private boolean urgent = false;

    private int orderIndex = 0;
}
