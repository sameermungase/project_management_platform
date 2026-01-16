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
public class ProjectTemplateDTO {
    private UUID id;
    private String name;
    private String description;
    private String templateType;
    private UUID createdById;
    private String createdByUsername;
    private Boolean isPublic;
    private List<TemplateTaskDTO> templateTasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


