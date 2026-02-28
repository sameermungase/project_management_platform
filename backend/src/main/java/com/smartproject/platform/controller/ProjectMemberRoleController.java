package com.smartproject.platform.controller;

import com.smartproject.platform.model.ProjectMemberRole;
import com.smartproject.platform.model.Role;
import com.smartproject.platform.repository.ProjectMemberRoleRepository;
import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.service.NotificationService;
import com.smartproject.platform.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.smartproject.platform.security.services.UserDetailsImpl;
import com.smartproject.platform.model.Project;
import com.smartproject.platform.model.User;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;

import java.util.List;
import java.util.UUID;

@Tag(name = "Project Member Roles", description = "Project-specific role assignment endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
public class ProjectMemberRoleController {
    
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final NotificationService notificationService;
    
    // Endpoint to get all member roles for a project
    @GetMapping("/api/projects/{projectId}/members/all/roles")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get all project member roles", description = "Get all role assignments for a project")
    public ResponseEntity<List<ProjectMemberRole>> getAllMemberRoles(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectMemberRoleRepository.findByProjectId(projectId));
    }
    
    @PostMapping("/api/projects/{projectId}/members/{userId}/role")
    @PreAuthorize("hasAuthority('SENIOR') or hasAuthority('STAFF') or hasAuthority('PRINCIPAL') or hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Assign project role", description = "Assign a role to a user in a project (SENIOR+ required)")
    public ResponseEntity<ProjectMemberRole> assignRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @RequestParam Role role) {
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User currentUser = getCurrentUser();
        
        // Check if current user can assign this role
        if (!permissionService.isAtLeast(currentUser, project, Role.SENIOR)) {
            throw new UnauthorizedException("You don't have permission to assign roles");
        }
        
        // Check if role already exists
        projectMemberRoleRepository.findByProjectIdAndUserId(projectId, userId)
                .ifPresent(existing -> projectMemberRoleRepository.delete(existing));
        
        ProjectMemberRole projectRole = ProjectMemberRole.builder()
                .project(project)
                .user(user)
                .role(role)
                .assignedBy(currentUser)
                .build();
        
        ProjectMemberRole saved = projectMemberRoleRepository.save(projectRole);
        
        // Notify user of role change
        notificationService.notifyRoleChange(user, role, project);
        
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    
    @GetMapping("/api/projects/{projectId}/members/{userId}/role")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get project role", description = "Get a user's role in a project")
    public ResponseEntity<ProjectMemberRole> getRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        return projectMemberRoleRepository.findByProjectIdAndUserId(projectId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/api/projects/{projectId}/members/{userId}/role")
    @PreAuthorize("hasAuthority('SENIOR') or hasAuthority('STAFF') or hasAuthority('PRINCIPAL') or hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Remove project role", description = "Remove a role assignment (SENIOR+ required)")
    public ResponseEntity<Void> removeRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        User currentUser = getCurrentUser();
        
        if (!permissionService.isAtLeast(currentUser, project, Role.SENIOR)) {
            throw new UnauthorizedException("You don't have permission to remove roles");
        }
        
        projectMemberRoleRepository.findByProjectIdAndUserId(projectId, userId)
                .ifPresent(projectMemberRoleRepository::delete);
        
        return ResponseEntity.noContent().build();
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