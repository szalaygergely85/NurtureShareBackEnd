package com.nurtureshare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTaskCommentRequest {

    @NotBlank(message = "Comment content is required")
    private String content;
}
