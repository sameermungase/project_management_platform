package com.smartproject.platform.service;

import com.smartproject.platform.dto.TaskDependencyDTO;
import com.smartproject.platform.dto.TaskDependencyRequest;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.model.Task;
import com.smartproject.platform.model.TaskDependency;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.TaskDependencyRepository;
import com.smartproject.platform.repository.TaskRepository;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskDependencyService {

    private final TaskDependencyRepository dependencyRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public TaskDependencyDTO createDependency(TaskDependencyRequest request) {
        User currentUser = getCurrentUser();
        
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        Task dependsOnTask = taskRepository.findById(request.getDependsOnTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Dependent task not found"));
        
        // Check for circular dependency
        if (hasCircularDependency(request.getTaskId(), request.getDependsOnTaskId())) {
            throw new IllegalArgumentException("Circular dependency detected");
        }
        
        // Check if dependency already exists
        if (dependencyRepository.existsByTaskIdAndDependsOnTaskId(request.getTaskId(), request.getDependsOnTaskId())) {
            throw new IllegalArgumentException("Dependency already exists");
        }
        
        TaskDependency dependency = TaskDependency.builder()
                .task(task)
                .dependsOnTask(dependsOnTask)
                .dependencyType(request.getDependencyType())
                .build();
        
        TaskDependency saved = dependencyRepository.save(dependency);
        
        activityLogService.logActivity(currentUser, "TASK_DEPENDENCY_CREATED", 
                "Created dependency: " + task.getTitle() + " depends on " + dependsOnTask.getTitle(), 
                task.getId(), "TASK");
        
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskDependencyDTO> getDependenciesByTask(UUID taskId) {
        List<TaskDependency> dependencies = dependencyRepository.findByTaskId(taskId);
        return dependencies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDependencyDTO> getDependentTasks(UUID taskId) {
        List<TaskDependency> dependencies = dependencyRepository.findByDependsOnTaskId(taskId);
        return dependencies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDependencyDTO> getAllRelatedDependencies(UUID taskId) {
        List<TaskDependency> dependencies = dependencyRepository.findAllRelatedDependencies(taskId);
        return dependencies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDependency(UUID dependencyId) {
        User currentUser = getCurrentUser();
        TaskDependency dependency = dependencyRepository.findById(dependencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Dependency not found"));
        
        dependencyRepository.delete(dependency);
        
        activityLogService.logActivity(currentUser, "TASK_DEPENDENCY_DELETED", 
                "Deleted dependency", dependency.getTask().getId(), "TASK");
    }

    private boolean hasCircularDependency(UUID taskId, UUID dependsOnTaskId) {
        // Check if dependsOnTask already depends on task (direct or indirect)
        List<TaskDependency> existingDeps = dependencyRepository.findByTaskId(dependsOnTaskId);
        for (TaskDependency dep : existingDeps) {
            if (dep.getDependsOnTask().getId().equals(taskId)) {
                return true; // Direct circular dependency
            }
            // Check indirect circular dependency recursively
            if (hasCircularDependency(taskId, dep.getDependsOnTask().getId())) {
                return true;
            }
        }
        return false;
    }

    private TaskDependencyDTO toDTO(TaskDependency dependency) {
        return TaskDependencyDTO.builder()
                .id(dependency.getId())
                .taskId(dependency.getTask().getId())
                .taskTitle(dependency.getTask().getTitle())
                .dependsOnTaskId(dependency.getDependsOnTask().getId())
                .dependsOnTaskTitle(dependency.getDependsOnTask().getTitle())
                .dependencyType(dependency.getDependencyType())
                .createdAt(dependency.getCreatedAt())
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
