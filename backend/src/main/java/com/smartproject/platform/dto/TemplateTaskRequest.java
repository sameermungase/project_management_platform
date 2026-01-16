package com.smartproject.platform.dto;

import lombok.Data;

@Data
public class TemplateTaskRequest {
    private String title;
    private String description;
    private String priority;
    private Integer orderIndex;
}