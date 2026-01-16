package com.smartproject.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalProjects;
    private long activeProjects;
    private long totalTasks;
    private long pendingTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long overdueTasks;
    private long myAssignedTasks;
    
    // Enhanced dashboard data
    private Map<String, Long> tasksByPriority; // HIGH, MEDIUM, LOW counts
    private Map<String, Long> tasksByStatus; // TODO, IN_PROGRESS, DONE counts
    private List<RecentActivityDTO> recentActivities;
    private List<ProjectHealthDTO> projectHealth;
    private TeamVelocityDTO teamVelocity;
    private TaskDistributionDTO taskDistribution;
}

