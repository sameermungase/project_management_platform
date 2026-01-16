package com.smartproject.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class ProjectRequest {
    @NotBlank(message = "Project name is required")
    private String name;
    
    private String description;
    
    private Set<UUID> memberIds;
}
