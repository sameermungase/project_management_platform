package com.smartproject.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequest {
    @NotBlank(message = "Comment content is required")
    private String content;
    
    private UUID taskId;
    private UUID projectId;
    private UUID parentCommentId; // For replies
}
