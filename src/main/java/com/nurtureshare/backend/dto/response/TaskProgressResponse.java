package com.nurtureshare.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskProgressResponse {

    private long total;
    private long completed;
    private long pending;
    private int percentComplete;
}
