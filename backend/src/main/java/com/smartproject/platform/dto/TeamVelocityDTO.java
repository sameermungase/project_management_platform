package com.smartproject.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamVelocityDTO {
    private double averageVelocity;
    private long tasksCompletedThisWeek;
    private long tasksCompletedLastWeek;
    private double velocityTrend; // percentage change
}
