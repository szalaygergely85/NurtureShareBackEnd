package com.nurtureshare.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MilestoneResponse {

    private int week;
    private String title;
    private String babySize;
    private String description;
    private String imageDescription;
}
