package com.smartproject.platform.service;

import com.smartproject.platform.dto.DashboardStatsDTO;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.model.*;
import com.smartproject.platform.repository.ActivityLogRepository;
import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.TaskRepository;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        User currentUser = getCurrentUser();

        // Get user's projects
        List<Project> ownedProjects = projectRepository.findByOwnerId(currentUser.getId());
        List<Project> memberProjects = projectRepository.findByMembersId(currentUser.getId());
        
        long totalProjects = ownedProjects.size() + memberProjects.size();

        // Get all tasks from user's projects
        long totalTasks = 0;
        long pendingTasks = 0;
        long completedTasks = 0;
        long inProgressTasks = 0;
        long overdueTasks = 0;

        for (Project project : ownedProjects) {
            List<Task> tasks = taskRepository.findByProjectId(project.getId());
            totalTasks += tasks.size();
            pendingTasks += tasks.stream().filter(t -> t.getStatus() == TaskStatus.TO_DO).count();
            completedTasks += tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
            inProgressTasks += tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
            overdueTasks += tasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) 
                            && t.getStatus() != TaskStatus.DONE)
                    .count();
        }

        for (Project project : memberProjects) {
            List<Task> tasks = taskRepository.findByProjectId(project.getId());
            totalTasks += tasks.size();
            pendingTasks += tasks.stream().filter(t -> t.getStatus() == TaskStatus.TO_DO).count();
            completedTasks += tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
            inProgressTasks += tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
            overdueTasks += tasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) 
                            && t.getStatus() != TaskStatus.DONE)
                    .count();
        }

        // Get tasks assigned to current user
        List<Task> myAssignedTasks = taskRepository.findByAssigneeId(currentUser.getId());
        long myAssignedTasksCount = myAssignedTasks.size();

        // Collect all tasks for enhanced stats
        List<Task> allUserTasks = new ArrayList<>();
        for (Project project : ownedProjects) {
            allUserTasks.addAll(taskRepository.findByProjectId(project.getId()));
        }
        for (Project project : memberProjects) {
            allUserTasks.addAll(taskRepository.findByProjectId(project.getId()));
        }

        // Enhanced statistics
        Map<String, Long> tasksByPriority = allUserTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority() != null ? t.getPriority().name() : "NONE",
                        Collectors.counting()
                ));

        Map<String, Long> tasksByStatus = allUserTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus() != null ? t.getStatus().name() : "NONE",
                        Collectors.counting()
                ));

        // Recent activities
        Pageable recentPageable = PageRequest.of(0, 10);
        List<com.smartproject.platform.dto.RecentActivityDTO> recentActivities = 
                activityLogRepository.findAll(recentPageable).getContent().stream()
                        .map(log -> com.smartproject.platform.dto.RecentActivityDTO.builder()
                                .action(log.getAction())
                                .entityType(log.getEntityType())
                                .entityName(log.getDetails())
                                .username(log.getUser() != null ? log.getUser().getUsername() : "System")
                                .timestamp(log.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .build())
                        .collect(Collectors.toList());

        // Project health
        List<com.smartproject.platform.dto.ProjectHealthDTO> projectHealth = new ArrayList<>();
        for (Project project : Stream.concat(ownedProjects.stream(), memberProjects.stream())
                .distinct()
                .collect(Collectors.toList())) {
            List<Task> projectTasks = taskRepository.findByProjectId(project.getId());
            long projectOverdue = projectTasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now())
                            && t.getStatus() != TaskStatus.DONE)
                    .count();
            long projectTotal = projectTasks.size();
            long projectDone = projectTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE)
                    .count();
            double progress = projectTotal > 0 ? (double) projectDone / projectTotal * 100 : 0.0;
            
            String healthStatus = "ON_TRACK";
            if (projectOverdue > 0) {
                healthStatus = "AT_RISK";
            }
            if (progress < 50 && projectOverdue > projectTotal * 0.2) {
                healthStatus = "DELAYED";
            }

            projectHealth.add(com.smartproject.platform.dto.ProjectHealthDTO.builder()
                    .projectId(project.getId().toString())
                    .projectName(project.getName())
                    .healthStatus(healthStatus)
                    .progressPercentage(progress)
                    .overdueTasks(projectOverdue)
                    .build());
        }

        // Team velocity (simplified - tasks completed this week vs last week)
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate lastWeekStart = weekStart.minusWeeks(1);

        long tasksThisWeek = allUserTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE
                        && t.getUpdatedAt() != null
                        && t.getUpdatedAt().toLocalDate().isAfter(weekStart.minusDays(1)))
                .count();

        long tasksLastWeek = allUserTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE
                        && t.getUpdatedAt() != null
                        && t.getUpdatedAt().toLocalDate().isAfter(lastWeekStart.minusDays(1))
                        && t.getUpdatedAt().toLocalDate().isBefore(weekStart))
                .count();

        double velocityTrend = tasksLastWeek > 0 
                ? ((double) (tasksThisWeek - tasksLastWeek) / tasksLastWeek) * 100 
                : 0.0;

        com.smartproject.platform.dto.TeamVelocityDTO teamVelocity = 
                com.smartproject.platform.dto.TeamVelocityDTO.builder()
                        .averageVelocity((tasksThisWeek + tasksLastWeek) / 2.0)
                        .tasksCompletedThisWeek(tasksThisWeek)
                        .tasksCompletedLastWeek(tasksLastWeek)
                        .velocityTrend(velocityTrend)
                        .build();

        // Task distribution by assignee
        Map<String, Long> tasksByAssignee = allUserTasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getAssignee().getUsername(),
                        Collectors.counting()
                ));

        // Task distribution by project
        Map<String, Long> tasksByProject = new HashMap<>();
        for (Project project : Stream.concat(ownedProjects.stream(), memberProjects.stream())
                .distinct()
                .collect(Collectors.toList())) {
            long count = taskRepository.findByProjectId(project.getId()).size();
            tasksByProject.put(project.getName(), count);
        }

        com.smartproject.platform.dto.TaskDistributionDTO taskDistribution = 
                com.smartproject.platform.dto.TaskDistributionDTO.builder()
                        .tasksByAssignee(tasksByAssignee)
                        .tasksByProject(tasksByProject)
                        .build();

        return DashboardStatsDTO.builder()
                .totalProjects(totalProjects)
                .activeProjects(totalProjects)
                .totalTasks(totalTasks)
                .pendingTasks(pendingTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .overdueTasks(overdueTasks)
                .myAssignedTasks(myAssignedTasksCount)
                .tasksByPriority(tasksByPriority)
                .tasksByStatus(tasksByStatus)
                .recentActivities(recentActivities)
                .projectHealth(projectHealth)
                .teamVelocity(teamVelocity)
                .taskDistribution(taskDistribution)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getAdminDashboardStats() {
        // Get all projects
        long totalProjects = projectRepository.count();

        // Get all tasks
        List<Task> allTasks = taskRepository.findAll();
        long totalTasks = allTasks.size();
        long pendingTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TO_DO).count();
        long completedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long inProgressTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long overdueTasks = allTasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) 
                        && t.getStatus() != TaskStatus.DONE)
                .count();

        return DashboardStatsDTO.builder()
                .totalProjects(totalProjects)
                .activeProjects(totalProjects)
                .totalTasks(totalTasks)
                .pendingTasks(pendingTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .overdueTasks(overdueTasks)
                .myAssignedTasks(0) // Not applicable for admin view
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
