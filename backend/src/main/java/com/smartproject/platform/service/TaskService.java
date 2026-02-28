package com.smartproject.platform.service;

import com.smartproject.platform.dto.TaskDTO;
import com.smartproject.platform.dto.TaskRequest;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.mapper.TaskMapper;
import com.smartproject.platform.model.Epic;
import com.smartproject.platform.model.Milestone;
import com.smartproject.platform.model.Project;
import com.smartproject.platform.model.Task;
import com.smartproject.platform.model.TaskStatus;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.EpicRepository;
import com.smartproject.platform.repository.MilestoneRepository;
import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.TaskRepository;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EpicRepository epicRepository;
    private final MilestoneRepository milestoneRepository;
    private final ApprovalService approvalService;
    private final TaskMapper taskMapper;
    private final ActivityLogService activityLogService;

    @Transactional
    public TaskDTO createTask(TaskRequest request) {
        User currentUser = getCurrentUser();
        
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));
        
        // Verify user is member of project
        if (!isOwnerOrMember(project, currentUser)) {
            throw new UnauthorizedException("You must be a member of the project to create tasks");
        }
        
        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            task.setAssignee(assignee);
        }
        
        // Link to Epic if provided
        if (request.getEpicId() != null) {
            Epic epic = epicRepository.findById(request.getEpicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Epic not found with id: " + request.getEpicId()));
            if (!epic.getProject().getId().equals(project.getId())) {
                throw new UnauthorizedException("Epic does not belong to this project");
            }
            task.setEpic(epic);
        }
        
        // Link to Milestone if provided
        if (request.getMilestoneId() != null) {
            Milestone milestone = milestoneRepository.findById(request.getMilestoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + request.getMilestoneId()));
            if (!milestone.getProject().getId().equals(project.getId())) {
                throw new UnauthorizedException("Milestone does not belong to this project");
            }
            task.setMilestone(milestone);
        }
        
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TO_DO);
        }
        
        Task savedTask = taskRepository.save(task);
        
        // Request approval if flag is set
        if (request.isRequestApproval()) {
            approvalService.requestApproval(
                    savedTask.getId(),
                    "TASK",
                    null,
                    request.getApprovalComments()
            );
        }
        
        // Log task creation
        activityLogService.logActivity(
                currentUser,
                "TASK_CREATED",
                "Created task: " + savedTask.getTitle(),
                savedTask.getId(),
                "TASK"
        );
        
        // Log task assignment if assignee is set
        if (savedTask.getAssignee() != null) {
            activityLogService.logActivity(
                    currentUser,
                    "TASK_ASSIGNED",
                    "Assigned task '" + savedTask.getTitle() + "' to " + savedTask.getAssignee().getUsername(),
                    savedTask.getId(),
                    "TASK"
            );
        }
        
        // Log epic linkage if set
        if (savedTask.getEpic() != null) {
            activityLogService.logActivity(
                    currentUser,
                    "TASK_LINKED_TO_EPIC",
                    "Linked task '" + savedTask.getTitle() + "' to epic: " + savedTask.getEpic().getTitle(),
                    savedTask.getId(),
                    "TASK"
            );
        }
        
        // Log milestone linkage if set
        if (savedTask.getMilestone() != null) {
            activityLogService.logActivity(
                    currentUser,
                    "TASK_LINKED_TO_MILESTONE",
                    "Linked task '" + savedTask.getTitle() + "' to milestone: " + savedTask.getMilestone().getTitle(),
                    savedTask.getId(),
                    "TASK"
            );
        }
        
        return taskMapper.toDTO(savedTask);
    }
    
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        // Verify user has access to view this task
        User currentUser = getCurrentUser();
        Project project = task.getProject();
        if (!isOwnerOrMember(project, currentUser)) {
            throw new UnauthorizedException("You must be a member of the project to view this task");
        }
        
        return taskMapper.toDTO(task);
    }
    
    @Transactional(readOnly = true)
    public Page<TaskDTO> getTasksByProjectId(UUID projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        // Verify user is member of project
        User currentUser = getCurrentUser();
        if (!isOwnerOrMember(project, currentUser)) {
             throw new UnauthorizedException("You must be a member of the project to view tasks");
        }

        return taskRepository.findByProjectId(projectId, pageable)
                .map(taskMapper::toDTO);
    }

    @Transactional
    public TaskDTO updateTask(UUID id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        User currentUser = getCurrentUser();
        // Check permissions: Owner of project, Assignee, or just Member?
        // Let's say any member can update for collaboration.
        
        // Track changes for logging
        TaskStatus oldStatus = task.getStatus();
        User oldAssignee = task.getAssignee();
        UUID oldEpicId = task.getEpic() != null ? task.getEpic().getId() : null;
        UUID oldMilestoneId = task.getMilestone() != null ? task.getMilestone().getId() : null;
        
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        
        // Handle assignee changes
        User newAssignee = null;
        if (request.getAssigneeId() != null) {
             newAssignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
             task.setAssignee(newAssignee);
        } else {
            task.setAssignee(null);
        }
        
        // Handle epic linkage
        if (request.getEpicId() != null) {
            Epic epic = epicRepository.findById(request.getEpicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
            if (!epic.getProject().getId().equals(task.getProject().getId())) {
                throw new UnauthorizedException("Epic does not belong to this project");
            }
            task.setEpic(epic);
        } else {
            task.setEpic(null);
        }
        
        // Handle milestone linkage
        if (request.getMilestoneId() != null) {
            Milestone milestone = milestoneRepository.findById(request.getMilestoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));
            if (!milestone.getProject().getId().equals(task.getProject().getId())) {
                throw new UnauthorizedException("Milestone does not belong to this project");
            }
            task.setMilestone(milestone);
        } else {
            task.setMilestone(null);
        }
        
        Task updatedTask = taskRepository.save(task);
        
        // Log status change if it changed
        if (request.getStatus() != null && !request.getStatus().equals(oldStatus)) {
            activityLogService.logActivity(
                    currentUser,
                    "TASK_STATUS_CHANGED",
                    "Changed task '" + updatedTask.getTitle() + "' status from " + oldStatus + " to " + updatedTask.getStatus(),
                    updatedTask.getId(),
                    "TASK"
            );
        }
        
        // Log assignment change if it changed
        if (newAssignee != null && (oldAssignee == null || !oldAssignee.getId().equals(newAssignee.getId()))) {
            activityLogService.logActivity(
                    currentUser,
                    "TASK_ASSIGNED",
                    "Assigned task '" + updatedTask.getTitle() + "' to " + newAssignee.getUsername(),
                    updatedTask.getId(),
                    "TASK"
            );
        } else if (oldAssignee != null && newAssignee == null) {
            activityLogService.logActivity(
                    currentUser,
                    "TASK_UNASSIGNED",
                    "Unassigned task '" + updatedTask.getTitle() + "' from " + oldAssignee.getUsername(),
                    updatedTask.getId(),
                    "TASK"
            );
        }
        
        // Log epic linkage changes
        UUID newEpicId = updatedTask.getEpic() != null ? updatedTask.getEpic().getId() : null;
        if (!Objects.equals(oldEpicId, newEpicId)) {
            if (newEpicId != null) {
                activityLogService.logActivity(
                        currentUser,
                        "TASK_LINKED_TO_EPIC",
                        "Linked task '" + updatedTask.getTitle() + "' to epic: " + updatedTask.getEpic().getTitle(),
                        updatedTask.getId(),
                        "TASK"
                );
            } else {
                activityLogService.logActivity(
                        currentUser,
                        "TASK_UNLINKED_FROM_EPIC",
                        "Unlinked task '" + updatedTask.getTitle() + "' from epic",
                        updatedTask.getId(),
                        "TASK"
                );
            }
        }
        
        // Log milestone linkage changes
        UUID newMilestoneId = updatedTask.getMilestone() != null ? updatedTask.getMilestone().getId() : null;
        if (!Objects.equals(oldMilestoneId, newMilestoneId)) {
            if (newMilestoneId != null) {
                activityLogService.logActivity(
                        currentUser,
                        "TASK_LINKED_TO_MILESTONE",
                        "Linked task '" + updatedTask.getTitle() + "' to milestone: " + updatedTask.getMilestone().getTitle(),
                        updatedTask.getId(),
                        "TASK"
                );
            } else {
                activityLogService.logActivity(
                        currentUser,
                        "TASK_UNLINKED_FROM_MILESTONE",
                        "Unlinked task '" + updatedTask.getTitle() + "' from milestone",
                        updatedTask.getId(),
                        "TASK"
                );
            }
        }
        
        // Log general update if other fields changed (but not status, assignee, epic, or milestone)
        if ((request.getStatus() == null || request.getStatus().equals(oldStatus)) &&
            (newAssignee == null || (oldAssignee != null && oldAssignee.getId().equals(newAssignee.getId()))) &&
            Objects.equals(oldEpicId, newEpicId) &&
            Objects.equals(oldMilestoneId, newMilestoneId)) {
            activityLogService.logActivity(
                    currentUser,
                    "TASK_UPDATED",
                    "Updated task: " + updatedTask.getTitle(),
                    updatedTask.getId(),
                    "TASK"
            );
        }
        
        return taskMapper.toDTO(updatedTask);
    }
    
    @Transactional
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
         
        // Allow deletion by project owner or members with management permission
        User currentUser = getCurrentUser();
        Project project = task.getProject();
        
        // Project owner can always delete
        if (project.getOwner().getId().equals(currentUser.getId())) {
            taskRepository.delete(task);
            return;
        }
        
        // Check if user is a member of the project with management permissions
        if (!project.getMembers().stream().anyMatch(m -> m.getId().equals(currentUser.getId()))) {
            throw new UnauthorizedException("You are not a member of this project");
        }
        
        // Member can delete if they have admin, manager, or technical lead role
        boolean hasDeletionPermission = currentUser.getRoles().stream()
                .anyMatch(role -> role.toString().equals("ADMIN") || 
                                 role.toString().equals("MANAGER") || 
                                 role.toString().equals("TECHNICAL_LEAD"));
        
        if (!hasDeletionPermission) {
            throw new UnauthorizedException("You don't have permission to delete tasks in this project");
        }
        
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> getAllUserTasks(Pageable pageable) {
        User currentUser = getCurrentUser();
        
        // Get all projects user is part of
        List<Project> ownedProjects = projectRepository.findByOwnerId(currentUser.getId());
        List<Project> memberProjects = projectRepository.findByMembersId(currentUser.getId());
        
        // Collect all project IDs
        Set<UUID> projectIds = new HashSet<>();
        ownedProjects.forEach(p -> projectIds.add(p.getId()));
        memberProjects.forEach(p -> projectIds.add(p.getId()));
        
        // Get all tasks from these projects
        List<Task> allUserTasks = new ArrayList<>();
        for (UUID projectId : projectIds) {
            allUserTasks.addAll(taskRepository.findByProjectId(projectId));
        }
        
        // Convert to Page (simple implementation)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allUserTasks.size());
        List<TaskDTO> taskDTOs = allUserTasks.subList(start, end).stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(taskDTOs, pageable, allUserTasks.size());
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> getAllTasksForAdmin(Pageable pageable) {
        // Admin can see all tasks
        return taskRepository.findAll(pageable)
                .map(taskMapper::toDTO);
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
    
    private boolean isOwnerOrMember(Project project, User user) {
        return project.getOwner().getId().equals(user.getId()) ||
               project.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
    }
}
