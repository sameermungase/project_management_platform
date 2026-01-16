package com.smartproject.platform.service;

import com.smartproject.platform.dto.ProjectDTO;
import com.smartproject.platform.dto.ProjectRequest;
import com.smartproject.platform.dto.ProjectTemplateDTO;
import com.smartproject.platform.dto.ProjectTemplateRequest;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.mapper.ProjectMapper;
import com.smartproject.platform.model.*;
import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.ProjectTemplateRepository;
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
public class ProjectTemplateService {

    private final ProjectTemplateRepository templateRepository;
    private final ProjectRepository projectRepository;
    private final com.smartproject.platform.repository.TaskRepository taskRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public ProjectTemplateDTO createTemplate(ProjectTemplateRequest request) {
        User currentUser = getCurrentUser();
        
        ProjectTemplate template = ProjectTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .templateType(request.getTemplateType())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .createdBy(currentUser)
                .build();
        
        if (request.getTemplateTasks() != null) {
            List<TemplateTask> templateTasks = request.getTemplateTasks().stream()
                    .map(tt -> {
                        Priority priority = Priority.MEDIUM;
                        if (tt.getPriority() != null) {
                            try {
                                priority = Priority.valueOf(tt.getPriority().toUpperCase());
                            } catch (IllegalArgumentException e) {
                                // Keep default
                            }
                        }
                        return TemplateTask.builder()
                                .template(template)
                                .title(tt.getTitle())
                                .description(tt.getDescription())
                                .priority(priority)
                                .orderIndex(tt.getOrderIndex() != null ? tt.getOrderIndex() : 0)
                                .build();
                    })
                    .collect(Collectors.toList());
            template.setTemplateTasks(templateTasks);
        }
        
        ProjectTemplate saved = templateRepository.save(template);
        
        activityLogService.logActivity(currentUser, "TEMPLATE_CREATED", 
                "Created template: " + saved.getName(), saved.getId(), "TEMPLATE");
        
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectTemplateDTO> getAllTemplates() {
        User currentUser = getCurrentUser();
        List<ProjectTemplate> templates = templateRepository.findByIsPublicTrueOrCreatedById(currentUser.getId());
        return templates.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectTemplateDTO> getPublicTemplates() {
        List<ProjectTemplate> templates = templateRepository.findByIsPublicTrue();
        return templates.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectTemplateDTO getTemplateById(UUID templateId) {
        ProjectTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        return toDTO(template);
    }

    @Transactional
    public ProjectDTO createProjectFromTemplate(UUID templateId, ProjectRequest projectRequest) {
        User currentUser = getCurrentUser();
        ProjectTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        
        // Create project from request
        Project project = projectMapper.toEntity(projectRequest);
        project.setOwner(currentUser);
        
        Project savedProject = projectRepository.save(project);
        
        // Create tasks from template
        if (template.getTemplateTasks() != null && !template.getTemplateTasks().isEmpty()) {
            for (TemplateTask templateTask : template.getTemplateTasks()) {
                Task task = Task.builder()
                        .title(templateTask.getTitle())
                        .description(templateTask.getDescription())
                        .priority(templateTask.getPriority())
                        .status(TaskStatus.TO_DO)
                        .project(savedProject)
                        .build();
                taskRepository.save(task);
            }
        }
        
        activityLogService.logActivity(currentUser, "PROJECT_CREATED_FROM_TEMPLATE", 
                "Created project from template: " + template.getName(), savedProject.getId(), "PROJECT");
        
        return projectMapper.toDTO(savedProject);
    }

    @Transactional
    public void deleteTemplate(UUID templateId) {
        User currentUser = getCurrentUser();
        ProjectTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        
        if (!template.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only delete your own templates");
        }
        
        templateRepository.delete(template);
        
        activityLogService.logActivity(currentUser, "TEMPLATE_DELETED", 
                "Deleted template: " + template.getName(), templateId, "TEMPLATE");
    }

    private ProjectTemplateDTO toDTO(ProjectTemplate template) {
        ProjectTemplateDTO dto = ProjectTemplateDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .templateType(template.getTemplateType())
                .isPublic(template.getIsPublic())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
        
        if (template.getCreatedBy() != null) {
            dto.setCreatedById(template.getCreatedBy().getId());
            dto.setCreatedByUsername(template.getCreatedBy().getUsername());
        }
        
        if (template.getTemplateTasks() != null) {
            dto.setTemplateTasks(template.getTemplateTasks().stream()
                    .map(tt -> {
                        com.smartproject.platform.dto.TemplateTaskDTO taskDTO = 
                                com.smartproject.platform.dto.TemplateTaskDTO.builder()
                                        .id(tt.getId())
                                        .title(tt.getTitle())
                                        .description(tt.getDescription())
                                        .priority(tt.getPriority().name())
                                        .orderIndex(tt.getOrderIndex())
                                        .build();
                        return taskDTO;
                    })
                    .collect(Collectors.toList()));
        }
        
        return dto;
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
