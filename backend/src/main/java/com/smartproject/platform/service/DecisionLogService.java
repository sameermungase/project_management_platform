package com.smartproject.platform.service;

import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.model.*;
import com.smartproject.platform.repository.DecisionLogRepository;
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
public class DecisionLogService {
    
    private final DecisionLogRepository decisionLogRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final NotificationService notificationService;
    
    @Transactional
    public DecisionLog createDecision(UUID projectId, String title, String description, 
                                     DecisionType decisionType, VisibilityLevel visibilityLevel) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        User currentUser = getCurrentUser();
        
        DecisionLog decision = DecisionLog.builder()
                .project(project)
                .title(title)
                .description(description)
                .decisionType(decisionType)
                .decidedBy(currentUser)
                .status(DecisionStatus.PROPOSED)
                .visibilityLevel(visibilityLevel != null ? visibilityLevel : VisibilityLevel.SENIOR_PLUS)
                .build();
        
        DecisionLog saved = decisionLogRepository.save(decision);
        
        // If decision requires approval, create approval request
        if (requiresApproval(decisionType)) {
            // Would trigger approval workflow here
        }
        
        return saved;
    }
    
    @Transactional
    public DecisionLog approveDecision(UUID decisionId, User approver) {
        if (approver == null) {
            approver = getCurrentUser();
        }
        
        DecisionLog decision = decisionLogRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));
        
        // Check if already approved
        if (decision.getStatus() == DecisionStatus.APPROVED) {
            throw new UnauthorizedException("This decision has already been approved");
        }
        
        // Check if approver has permission
        if (!permissionService.canPerformAction(approver, decision.getProject(), "APPROVE_ARCHITECTURE")) {
            throw new UnauthorizedException("You don't have permission to approve this decision");
        }
        
        decision.setStatus(DecisionStatus.APPROVED);
        decision.setApprovedBy(approver);
        
        DecisionLog saved = decisionLogRepository.save(decision);
        
        // Notify decision maker
        notificationService.notifyDecisionApproval(decision.getDecidedBy(), saved);
        
        return saved;
    }
    
    public List<DecisionLog> getDecisionsByProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        User currentUser = getCurrentUser();
        
        List<DecisionLog> decisions = decisionLogRepository.findByProjectId(projectId);
        return decisions.stream()
                .filter(decision -> permissionService.canViewEntity(
                        currentUser, project, decision.getVisibilityLevel()))
                .toList();
    }
    
    public Page<DecisionLog> getDecisionsByProject(UUID projectId, Pageable pageable) {
        return decisionLogRepository.findByProjectId(projectId, pageable);
    }
    
    public DecisionLog getDecisionById(UUID decisionId) {
        DecisionLog decision = decisionLogRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));
        
        User currentUser = getCurrentUser();
        if (!permissionService.canViewEntity(
                currentUser, decision.getProject(), decision.getVisibilityLevel())) {
            throw new UnauthorizedException("You don't have permission to view this decision");
        }
        
        return decision;
    }
    
    private boolean requiresApproval(DecisionType decisionType) {
        return decisionType == DecisionType.ARCHITECTURE || 
               decisionType == DecisionType.TECH_STACK;
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
