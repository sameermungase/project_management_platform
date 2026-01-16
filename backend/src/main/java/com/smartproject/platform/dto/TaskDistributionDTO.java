package com.smartproject.platform.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDistributionDTO {
    private Map<String, Long> tasksByAssignee;
    private Map<String, Long> tasksByProject;
}
