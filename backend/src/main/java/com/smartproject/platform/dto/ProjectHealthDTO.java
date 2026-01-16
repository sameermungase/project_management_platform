package com.smartproject.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectHealthDTO {
    private String projectId;
    private String projectName;
    private String healthStatus; // ON_TRACK, AT_RISK, DELAYED
    private double progressPercentage;
    private long overdueTasks;
}
