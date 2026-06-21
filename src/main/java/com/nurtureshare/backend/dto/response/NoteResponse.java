package com.nurtureshare.backend.dto.response;

import com.nurtureshare.backend.model.enums.NoteCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteResponse {

    private UUID id;
    private UUID coupleId;
    private UserResponse createdBy;
    private String title;
    private NoteCategory category;
    private boolean syncedWithPartner;
    private List<NoteItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
