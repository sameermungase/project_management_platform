package com.smartproject.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private UUID id;
    private String content;
    private UUID taskId;
    private UUID projectId;
    private UUID parentCommentId;
    private UUID userId;
    private String username;
    private String userEmail;
    private List<CommentDTO> replies;
    private List<ReactionDTO> reactions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean canEdit;
    private boolean canDelete;
}

