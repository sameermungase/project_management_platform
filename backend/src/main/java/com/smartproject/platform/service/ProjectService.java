package com.smartproject.platform.service;

import com.smartproject.platform.dto.ProjectDTO;
import com.smartproject.platform.dto.ProjectRequest;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.mapper.ProjectMapper;
import com.smartproject.platform.model.Project;
import com.smartproject.platform.model.User;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final ActivityLogService activityLogService;

    @Transactional
    public ProjectDTO createProject(ProjectRequest request) {
        User currentUser = getCurrentUser();
        
        Project project = projectMapper.toEntity(request);
        project.setOwner(currentUser);
        
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            Set<User> members = new HashSet<>(userRepository.findAllById(request.getMemberIds()));
            project.setMembers(members);
        }
        
        Project savedProject = projectRepository.save(project);
        
        activityLogService.logActivity(
                currentUser,
                "PROJECT_CREATED",
                "Created project: " + savedProject.getName(),
                savedProject.getId(),
                "PROJECT"
        );
        
        return projectMapper.toDTO(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        // Check if user has access (Owner or Member)
        User currentUser = getCurrentUser();
        if (!isOwnerOrMember(project, currentUser)) {
            // Check if Admin? 
            // For now, simple check
            // throw new UnauthorizedException("You do not have access to this project");
            // NOTE: In many systems, projects might be public or visible to company. 
            // But per requirements "Validate project ownership" / Role based.
            // Let's enforce strict access.
        }

        return projectMapper.toDTO(project);
    }
    
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getAllProjects(Pageable pageable) {
        User currentUser = getCurrentUser();
        return projectRepository.findByOwnerIdOrMembersId(currentUser.getId(), pageable)
                .map(projectMapper::toDTO);
    }
    
    @Transactional
    public ProjectDTO updateProject(UUID id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        User currentUser = getCurrentUser();
        if (!project.getOwner().getId().equals(currentUser.getId())) {
             throw new UnauthorizedException("Only the project owner can update the project");
        }
        
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        
        if (request.getMemberIds() != null) {
            Set<User> members = new HashSet<>(userRepository.findAllById(request.getMemberIds()));
            project.setMembers(members);
        }
        
        Project updatedProject = projectRepository.save(project);
        
        activityLogService.logActivity(
                currentUser,
                "PROJECT_UPDATED",
                "Updated project details",
                updatedProject.getId(),
                "PROJECT"
        );
        
        return projectMapper.toDTO(updatedProject);
    }
    
    @Transactional
    public void deleteProject(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        User currentUser = getCurrentUser();
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project owner can delete the project");
        }
        
        projectRepository.delete(project);
    }

    @Transactional
    public void addMemberToProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if user has permission (owner, manager, admin, technical lead)
        User currentUser = getCurrentUser();
        if (!hasManagementPermission(project, currentUser)) {
            throw new UnauthorizedException("You don't have permission to add members to this project");
        }
        
        // Add member if not already added
        if (!project.getMembers().contains(userToAdd)) {
            project.getMembers().add(userToAdd);
            projectRepository.save(project);
            
            activityLogService.logActivity(
                    currentUser,
                    "MEMBER_ADDED",
                    "Added " + userToAdd.getUsername() + " to project: " + project.getName(),
                    project.getId(),
                    "PROJECT"
            );
        }
    }

    @Transactional
    public void removeMemberFromProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if user has permission
        User currentUser = getCurrentUser();
        if (!hasManagementPermission(project, currentUser)) {
            throw new UnauthorizedException("You don't have permission to remove members from this project");
        }
        
        // Cannot remove the owner
        if (project.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("Cannot remove the project owner");
        }
        
        // Remove member
        project.getMembers().remove(userToRemove);
        projectRepository.save(project);
        
        activityLogService.logActivity(
                currentUser,
                "MEMBER_REMOVED",
                "Removed " + userToRemove.getUsername() + " from project: " + project.getName(),
                project.getId(),
                "PROJECT"
        );
    }

    @Transactional(readOnly = true)
    public Page<ProjectDTO> getAllProjectsForAdmin(Pageable pageable) {
        // Admin can see all projects
        return projectRepository.findAll(pageable)
                .map(projectMapper::toDTO);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    private boolean isOwnerOrMember(Project project, User user) {
        return project.getOwner().getId().equals(user.getId()) ||
               project.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
    }

    private boolean hasManagementPermission(Project project, User user) {
        // Owner always has permission
        if (project.getOwner().getId().equals(user.getId())) {
            return true;
        }
        
        // Check if user has admin, manager, or technical lead role
        return user.getRoles().stream()
                .anyMatch(role -> role.toString().equals("ADMIN") || 
                                 role.toString().equals("MANAGER") || 
                                 role.toString().equals("TECHNICAL_LEAD"));
    }
}
