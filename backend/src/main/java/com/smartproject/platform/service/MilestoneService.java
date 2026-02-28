package com.smartproject.platform.service;

import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.model.*;
import com.smartproject.platform.repository.EpicRepository;
import com.smartproject.platform.repository.MilestoneRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MilestoneService {
    
    private final MilestoneRepository milestoneRepository;
    private final EpicRepository epicRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final ActivityLogService activityLogService;
    
    @Transactional
    public Milestone createMilestone(UUID projectId, UUID epicId, String title, 
                                     String description, LocalDate targetDate) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        User currentUser = getCurrentUser();
        
        // Check permission - only SENIOR+ can create milestones
        if (!permissionService.canPerformAction(currentUser, project, "CREATE_MILESTONE")) {
            throw new UnauthorizedException("You don't have permission to create milestones");
        }
        
        Epic epic = null;
        if (epicId != null) {
            epic = epicRepository.findById(epicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
        }
        
        Milestone milestone = Milestone.builder()
                .project(project)
                .epic(epic)
                .title(title)
                .description(description)
                .targetDate(targetDate)
                .status(MilestoneStatus.PLANNED)
                .createdBy(currentUser)
                .build();
        
        Milestone saved = milestoneRepository.save(milestone);
        
        activityLogService.logActivity(
                currentUser,
                "MILESTONE_CREATED",
                String.format("Created milestone: %s", title),
                saved.getId(),
                "MILESTONE"
        );
        
        return saved;
    }
    
    public List<Milestone> getMilestonesByProject(UUID projectId) {
        return milestoneRepository.findByProjectId(projectId);
    }
    
    public Page<Milestone> getMilestonesByProject(UUID projectId, Pageable pageable) {
        return milestoneRepository.findByProjectId(projectId, pageable);
    }
    
    public Milestone getMilestoneById(UUID milestoneId) {
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));
    }
    
    @Transactional
    public Milestone updateMilestone(UUID milestoneId, String title, String description, 
                                     LocalDate targetDate, MilestoneStatus status) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));
        
        User currentUser = getCurrentUser();
        if (!permissionService.isAtLeast(currentUser, milestone.getProject(), Role.SENIOR)) {
            throw new UnauthorizedException("You don't have permission to update milestones");
        }
        
        if (title != null) milestone.setTitle(title);
        if (description != null) milestone.setDescription(description);
        if (targetDate != null) milestone.setTargetDate(targetDate);
        if (status != null) milestone.setStatus(status);
        
        return milestoneRepository.save(milestone);
    }
    
    @Transactional
    public void deleteMilestone(UUID milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));
        
        User currentUser = getCurrentUser();
        if (!permissionService.isAtLeast(currentUser, milestone.getProject(), Role.SENIOR)) {
            throw new UnauthorizedException("You don't have permission to delete milestones");
        }
        
        milestoneRepository.delete(milestone);
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
