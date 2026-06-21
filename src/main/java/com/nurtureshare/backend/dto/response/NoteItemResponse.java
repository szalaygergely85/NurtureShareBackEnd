package com.nurtureshare.backend.dto.response;

import com.nurtureshare.backend.model.enums.NoteItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteItemResponse {

    private UUID id;
    private UUID noteId;
    private String content;
    private NoteItemType itemType;
    private boolean checked;
    private boolean urgent;
    private int orderIndex;
    private LocalDateTime createdAt;
}
