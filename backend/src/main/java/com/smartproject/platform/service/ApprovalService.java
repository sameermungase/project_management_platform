package com.smartproject.platform.service;

import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.model.*;
import com.smartproject.platform.repository.*;
import com.smartproject.platform.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApprovalService {
    
    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final EpicRepository epicRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final PermissionService permissionService;
    private final NotificationService notificationService;
    
    @Transactional
    public Approval requestApproval(UUID entityId, String entityType, User requester, String comments) {
        if (requester == null) {
            requester = getCurrentUser();
        }
        
        Approval approval = Approval.builder()
                .entityId(entityId)
                .entityType(entityType)
                .requestedBy(requester)
                .status(ApprovalStatus.PENDING)
                .comments(comments)
                .build();
        
        Approval saved = approvalRepository.save(approval);
        
        // Find appropriate approver based on entity type and notify
        User approver = findApproverForEntity(entityId, entityType);
        if (approver != null) {
            notificationService.notifyApprovalRequest(approver, saved);
        }
        
        return saved;
    }
    
    @Transactional
    public Approval approve(UUID approvalId, User approver, String comments) {
        if (approver == null) {
            approver = getCurrentUser();
        }
        
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found"));
        
        // Check if already processed
        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new UnauthorizedException("This approval has already been processed");
        }
        
        // Check if approver has permission
        Project project = getProjectForEntity(approval.getEntityId(), approval.getEntityType());
        if (project == null) {
            throw new ResourceNotFoundException("Project not found for entity");
        }
        
        if (!permissionService.canPerformAction(approver, project, "APPROVE_" + approval.getEntityType())) {
            throw new UnauthorizedException("You don't have permission to approve this");
        }
        
        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setApprovedBy(approver);
        approval.setApprovedAt(LocalDateTime.now());
        if (comments != null) {
            approval.setComments(comments);
        }
        
        Approval saved = approvalRepository.save(approval);
        
        // Notify requester
        notificationService.createNotification(
                approval.getRequestedBy().getId(),
                NotificationType.DECISION_APPROVED,
                "Approval Granted",
                String.format("Your %s approval request has been approved", approval.getEntityType()),
                approval.getEntityType(),
                approval.getEntityId()
        );
        
        return saved;
    }
    
    @Transactional
    public Approval reject(UUID approvalId, User approver, String reason) {
        if (approver == null) {
            approver = getCurrentUser();
        }
        
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found"));
        
        // Check if already processed
        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new UnauthorizedException("This approval has already been processed");
        }
        
        Project project = getProjectForEntity(approval.getEntityId(), approval.getEntityType());
        if (project == null) {
            throw new ResourceNotFoundException("Project not found for entity");
        }
        
        if (!permissionService.canPerformAction(approver, project, "APPROVE_" + approval.getEntityType())) {
            throw new UnauthorizedException("You don't have permission to reject this");
        }
        
        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setApprovedBy(approver);
        approval.setApprovedAt(LocalDateTime.now());
        approval.setRejectionReason(reason);
        
        Approval saved = approvalRepository.save(approval);
        
        // Notify requester
        notificationService.createNotification(
                approval.getRequestedBy().getId(),
                NotificationType.DECISION_APPROVED,
                "Approval Rejected",
                String.format("Your %s approval request has been rejected: %s", 
                        approval.getEntityType(), reason),
                approval.getEntityType(),
                approval.getEntityId()
        );
        
        return saved;
    }
    
    public List<Approval> getPendingApprovalsForUser(User user) {
        if (user == null) {
            user = getCurrentUser();
        }
        // Return approvals where user can approve (based on their role in projects)
        return approvalRepository.findByStatus(ApprovalStatus.PENDING);
    }
    
    public Page<Approval> getPendingApprovals(Pageable pageable) {
        return approvalRepository.findByStatus(ApprovalStatus.PENDING, pageable);
    }
    
    public List<Approval> getUserApprovals(UUID userId) {
        return approvalRepository.findByRequestedById(userId);
    }
    
    private User findApproverForEntity(UUID entityId, String entityType) {
        Project project = getProjectForEntity(entityId, entityType);
        if (project == null) return null;
        
        // Find a senior+ user in the project based on project member roles
        List<ProjectMemberRole> seniorRoles = projectMemberRoleRepository.findByProjectIdAndRole(
                project.getId(), Role.SENIOR);
        
        if (!seniorRoles.isEmpty()) {
            return seniorRoles.get(0).getUser();
        }
        
        // Fallback to project owner
        return project.getOwner();
    }
    
    private Project getProjectForEntity(UUID entityId, String entityType) {
        return switch (entityType.toUpperCase()) {
            case "TASK" -> {
                Optional<Task> task = taskRepository.findById(entityId);
                yield task.map(Task::getProject).orElse(null);
            }
            case "EPIC" -> {
                Optional<Epic> epic = epicRepository.findById(entityId);
                yield epic.map(Epic::getProject).orElse(null);
            }
            case "MILESTONE" -> {
                Optional<Milestone> milestone = milestoneRepository.findById(entityId);
                yield milestone.map(Milestone::getProject).orElse(null);
            }
            default -> null;
        };
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
