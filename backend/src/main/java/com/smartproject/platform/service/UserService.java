package com.smartproject.platform.service;

import com.smartproject.platform.dto.*;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.model.Project;
import com.smartproject.platform.model.Task;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.TaskRepository;
import com.smartproject.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public UserDetailDTO getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get projects where user is owner or member
        List<Project> ownedProjects = projectRepository.findByOwnerId(userId);
        List<Project> memberProjects = projectRepository.findByMembersId(userId);

        Set<ProjectSummaryDTO> projectSummaries = new HashSet<>();
        
        ownedProjects.forEach(p -> projectSummaries.add(ProjectSummaryDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .ownerUsername(p.getOwner().getUsername())
                .role("OWNER")
                .build()));
        
        memberProjects.forEach(p -> projectSummaries.add(ProjectSummaryDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .ownerUsername(p.getOwner().getUsername())
                .role("MEMBER")
                .build()));

        // Get tasks assigned to user
        List<Task> assignedTasks = taskRepository.findByAssigneeId(userId);
        Set<TaskSummaryDTO> taskSummaries = assignedTasks.stream()
                .map(t -> TaskSummaryDTO.builder()
                        .id(t.getId())
                        .title(t.getTitle())
                        .status(t.getStatus())
                        .priority(t.getPriority())
                        .dueDate(t.getDueDate())
                        .projectName(t.getProject().getName())
                        .build())
                .collect(Collectors.toSet());

        return UserDetailDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .projects(projectSummaries)
                .assignedTasks(taskSummaries)
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrganizationUserDTO> getOrganizationUsers() {
        List<User> allUsers = userRepository.findAll();
        
        return allUsers.stream()
                .map(user -> {
                    // Get projects
                    List<Project> ownedProjects = projectRepository.findByOwnerId(user.getId());
                    List<Project> memberProjects = projectRepository.findByMembersId(user.getId());
                    
                    List<ProjectSummaryDTO> activeProjects = new ArrayList<>();
                    ownedProjects.forEach(p -> activeProjects.add(ProjectSummaryDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .ownerUsername(p.getOwner().getUsername())
                            .role("OWNER")
                            .build()));
                    
                    memberProjects.forEach(p -> activeProjects.add(ProjectSummaryDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .ownerUsername(p.getOwner().getUsername())
                            .role("MEMBER")
                            .build()));

                    // Get tasks
                    List<Task> assignedTasks = taskRepository.findByAssigneeId(user.getId());
                    List<TaskSummaryDTO> activeTasks = assignedTasks.stream()
                            .map(t -> TaskSummaryDTO.builder()
                                    .id(t.getId())
                                    .title(t.getTitle())
                                    .status(t.getStatus())
                                    .priority(t.getPriority())
                                    .dueDate(t.getDueDate())
                                    .projectName(t.getProject().getName())
                                    .build())
                            .collect(Collectors.toList());

                    return OrganizationUserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .roles(user.getRoles())
                            .createdAt(user.getCreatedAt())
                            .projectCount(ownedProjects.size() + memberProjects.size())
                            .taskCount(assignedTasks.size())
                            .activeProjects(activeProjects)
                            .activeTasks(activeTasks)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
