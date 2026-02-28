package com.smartproject.platform.service;

import com.smartproject.platform.model.*;
import com.smartproject.platform.repository.PermissionRepository;
import com.smartproject.platform.repository.ProjectMemberRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    
    /**
     * Check if user has permission in a project context
     */
    public boolean hasPermission(User user, Project project, String permissionName) {
        // 1. Check global roles first (ADMIN has all permissions)
        if (hasGlobalPermission(user, permissionName)) {
            return true;
        }
        
        // 2. Check project-specific role
        Optional<ProjectMemberRole> projectRole = projectMemberRoleRepository
                .findByProjectIdAndUserId(project.getId(), user.getId());
        
        if (projectRole.isPresent()) {
            return hasRolePermission(projectRole.get().getRole(), permissionName);
        }
        
        return false;
    }
    
    /**
     * Check if user can perform action based on their role hierarchy
     */
    public boolean canPerformAction(User user, Project project, String action) {
        Role userRole = getEffectiveRole(user, project);
        return canRolePerformAction(userRole, action);
    }
    
    /**
     * Get effective role for user in project context
     */
    public Role getEffectiveRole(User user, Project project) {
        // Check global roles first
        if (user.getRoles().contains(Role.ADMIN)) {
            return Role.ADMIN;
        }
        
        // Check project-specific role
        Optional<ProjectMemberRole> projectRole = projectMemberRoleRepository
                .findByProjectIdAndUserId(project.getId(), user.getId());
        
        if (projectRole.isPresent()) {
            return projectRole.get().getRole();
        }
        
        // Default to USER if no project role assigned
        return Role.USER;
    }
    
    /**
     * Check if user has global permission (not project-specific)
     */
    private boolean hasGlobalPermission(User user, String permissionName) {
        if (user.getRoles().contains(Role.ADMIN)) {
            return true; // Admin has all permissions
        }
        
        // Check if any of user's roles have this permission
        for (Role role : user.getRoles()) {
            if (hasRolePermission(role, permissionName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if a role has a specific permission
     */
    private boolean hasRolePermission(Role role, String permissionName) {
        List<Permission> rolePermissions = permissionRepository.findByRole(role.name());
        return rolePermissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }
    
    /**
     * Check if role can perform action based on hierarchy
     */
    private boolean canRolePerformAction(Role role, String action) {
        int roleLevel = getRoleHierarchy(role);
        
        return switch (action) {
            case "CREATE_EPIC" -> roleLevel >= getRoleHierarchy(Role.SENIOR);
            case "CREATE_MILESTONE" -> roleLevel >= getRoleHierarchy(Role.SENIOR);
            case "CREATE_TASK" -> roleLevel >= getRoleHierarchy(Role.SDE_2);
            case "ASSIGN_TASK" -> roleLevel >= getRoleHierarchy(Role.SDE_2);
            case "APPROVE_PR" -> roleLevel >= getRoleHierarchy(Role.SENIOR);
            case "APPROVE_DESIGN" -> roleLevel >= getRoleHierarchy(Role.STAFF);
            case "APPROVE_ARCHITECTURE" -> roleLevel >= getRoleHierarchy(Role.ARCHITECT);
            case "ESTIMATE_TIMELINE" -> roleLevel >= getRoleHierarchy(Role.SDE_2);
            case "OVERRIDE_DECISION" -> roleLevel >= getRoleHierarchy(Role.STAFF);
            case "UPDATE_TASK_STATUS" -> roleLevel >= getRoleHierarchy(Role.SDE_1);
            default -> false;
        };
    }
    
    /**
     * Check if user role is at least the minimum required role
     */
    public boolean isAtLeast(User user, Project project, Role minimumRole) {
        Role userRole = getEffectiveRole(user, project);
        return getRoleHierarchy(userRole) >= getRoleHierarchy(minimumRole);
    }
    
    /**
     * Get role hierarchy level
     */
    private int getRoleHierarchy(Role role) {
        return switch (role) {
            case SDE_1 -> 1;
            case SDE_2 -> 2;
            case SENIOR -> 3;
            case STAFF -> 4;
            case PRINCIPAL -> 5;
            case ARCHITECT -> 6;
            case ADMIN -> 10; // Admin has all permissions
            case TECHNICAL_LEAD -> 3; // Map to SENIOR level
            case MANAGER -> 2; // Map to SDE_2 level
            case USER -> 1; // Map to SDE_1 level
        };
    }
    
    /**
     * Check if user can view entity based on visibility level
     */
    public boolean canViewEntity(User user, Project project, VisibilityLevel visibilityLevel) {
        Role userRole = getEffectiveRole(user, project);
        
        return switch (visibilityLevel) {
            case PUBLIC -> true;
            case TEAM -> getRoleHierarchy(userRole) >= getRoleHierarchy(Role.SDE_2);
            case SENIOR_PLUS -> getRoleHierarchy(userRole) >= getRoleHierarchy(Role.SENIOR);
            case STAFF_PLUS -> getRoleHierarchy(userRole) >= getRoleHierarchy(Role.STAFF);
            case PRINCIPAL_PLUS -> getRoleHierarchy(userRole) >= getRoleHierarchy(Role.PRINCIPAL);
        };
    }
}
