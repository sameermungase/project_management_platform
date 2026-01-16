package com.smartproject.platform.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTaskDTO {
    private UUID id;
    private String title;
    private String description;
    private String priority;
    private Integer orderIndex;
}