package com.smartproject.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ProjectTemplateRequest {
    @NotBlank(message = "Template name is required")
    private String name;
    
    private String description;
    private String templateType;
    private Boolean isPublic = false;
    private List<TemplateTaskRequest> templateTasks;
}


