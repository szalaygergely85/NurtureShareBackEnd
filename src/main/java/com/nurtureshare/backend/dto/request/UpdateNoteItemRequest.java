package com.nurtureshare.backend.dto.request;

import com.nurtureshare.backend.model.enums.NoteItemType;
import lombok.Data;

@Data
public class UpdateNoteItemRequest {

    private String content;
    private NoteItemType itemType;
    private Boolean checked;
    private Boolean urgent;
    private Integer orderIndex;
}
