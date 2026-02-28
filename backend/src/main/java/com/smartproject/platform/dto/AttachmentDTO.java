package com.smartproject.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {
    private UUID id;
    private String filename;
    private String originalFilename;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdById;
    private String createdByName;
    private UUID taskId;
    private UUID epicId;
}
