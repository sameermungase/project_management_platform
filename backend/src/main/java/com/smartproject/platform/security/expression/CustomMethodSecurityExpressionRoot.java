package com.smartproject.platform.security.expression;

import com.smartproject.platform.model.Project;
import com.smartproject.platform.model.Role;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.security.services.UserDetailsImpl;
import com.smartproject.platform.service.PermissionService;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private UserRepository userRepository;
    private ProjectRepository projectRepository;
    private PermissionService permissionService;
    private Object filterObject;
    private Object returnObject;
    private Object target;

    public CustomMethodSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Check if user has permission in a project context
     * Usage: @PreAuthorize("hasProjectPermission(#projectId, 'TASK_CREATE')")
     */
    public boolean hasProjectPermission(UUID projectId, String permissionName) {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (userOpt.isEmpty() || projectOpt.isEmpty()) {
            return false;
        }

        return permissionService.hasPermission(userOpt.get(), projectOpt.get(), permissionName);
    }

    /**
     * Check if user has at least the minimum role in a project
     * Usage: @PreAuthorize("hasProjectRoleAtLeast(#projectId, 'SENIOR')")
     */
    public boolean hasProjectRoleAtLeast(UUID projectId, String minimumRoleName) {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (userOpt.isEmpty() || projectOpt.isEmpty()) {
            return false;
        }

        try {
            Role minimumRole = Role.valueOf(minimumRoleName);
            return permissionService.isAtLeast(userOpt.get(), projectOpt.get(), minimumRole);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if user can perform action in a project
     * Usage: @PreAuthorize("canPerformAction(#projectId, 'CREATE_TASK')")
     */
    public boolean canPerformAction(UUID projectId, String action) {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (userOpt.isEmpty() || projectOpt.isEmpty()) {
            return false;
        }

        return permissionService.canPerformAction(userOpt.get(), projectOpt.get(), action);
    }

    /**
     * Check if user has any of the specified roles in a project
     * Usage: @PreAuthorize("hasAnyProjectRole(#projectId, 'SENIOR', 'STAFF', 'PRINCIPAL')")
     */
    public boolean hasAnyProjectRole(UUID projectId, String... roleNames) {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (userOpt.isEmpty() || projectOpt.isEmpty()) {
            return false;
        }

        Role effectiveRole = permissionService.getEffectiveRole(userOpt.get(), projectOpt.get());
        
        for (String roleName : roleNames) {
            try {
                Role role = Role.valueOf(roleName);
                if (effectiveRole == role) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                // Skip invalid role names
            }
        }
        return false;
    }

    // Standard Spring Security expression methods are inherited from SecurityExpressionRoot

    @Override
    public boolean hasPermission(Object target, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Object targetId, String targetType, Object permission) {
        return false;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getThis() {
        return target;
    }

    public void setThis(Object target) {
        this.target = target;
    }
}
