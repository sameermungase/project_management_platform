package com.smartproject.platform.service;

import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.model.*;
import com.smartproject.platform.repository.EpicRepository;
import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EpicService {
    
    private final EpicRepository epicRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final ActivityLogService activityLogService;
    
    @Transactional
    public Epic createEpic(UUID projectId, String title, String description, 
                           VisibilityLevel visibilityLevel) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        User currentUser = getCurrentUser();
        
        // Check permission - only SENIOR+ can create epics
        if (!permissionService.canPerformAction(currentUser, project, "CREATE_EPIC")) {
            throw new UnauthorizedException("You don't have permission to create epics");
        }
        
        Epic epic = Epic.builder()
                .project(project)
                .title(title)
                .description(description)
                .status(EpicStatus.PLANNING)
                .visibilityLevel(visibilityLevel != null ? visibilityLevel : VisibilityLevel.TEAM)
                .createdBy(currentUser)
                .build();
        
        Epic saved = epicRepository.save(epic);
        
        activityLogService.logActivity(
                currentUser,
                "EPIC_CREATED",
                String.format("Created epic: %s", title),
                saved.getId(),
                "EPIC"
        );
        
        return saved;
    }
    
    public List<Epic> getEpicsByProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        User currentUser = getCurrentUser();
        
        // Filter by visibility
        List<Epic> epics = epicRepository.findByProjectId(projectId);
        return epics.stream()
                .filter(epic -> permissionService.canViewEntity(currentUser, project, epic.getVisibilityLevel()))
                .toList();
    }
    
    public Page<Epic> getEpicsByProject(UUID projectId, Pageable pageable) {
        // Verify project exists
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        return epicRepository.findByProjectId(projectId, pageable);
    }
    
    public Epic getEpicById(UUID epicId) {
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
        
        User currentUser = getCurrentUser();
        if (!permissionService.canViewEntity(currentUser, epic.getProject(), epic.getVisibilityLevel())) {
            throw new UnauthorizedException("You don't have permission to view this epic");
        }
        
        return epic;
    }
    
    @Transactional
    public Epic updateEpic(UUID epicId, String title, String description, EpicStatus status) {
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
        
        User currentUser = getCurrentUser();
        if (!permissionService.isAtLeast(currentUser, epic.getProject(), Role.SENIOR)) {
            throw new UnauthorizedException("You don't have permission to update epics");
        }
        
        if (title != null) epic.setTitle(title);
        if (description != null) epic.setDescription(description);
        if (status != null) epic.setStatus(status);
        
        return epicRepository.save(epic);
    }
    
    @Transactional
    public void deleteEpic(UUID epicId) {
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
        
        User currentUser = getCurrentUser();
        if (!permissionService.isAtLeast(currentUser, epic.getProject(), Role.SENIOR)) {
            throw new UnauthorizedException("You don't have permission to delete epics");
        }
        
        epicRepository.delete(epic);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        throw new UnauthorizedException("User not authenticated");
    }
}
