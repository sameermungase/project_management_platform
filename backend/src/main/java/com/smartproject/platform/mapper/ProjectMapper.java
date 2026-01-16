package com.smartproject.platform.mapper;

import com.smartproject.platform.dto.ProjectDTO;
import com.smartproject.platform.dto.ProjectRequest;
import com.smartproject.platform.model.Project;
import com.smartproject.platform.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public ProjectDTO toDTO(Project project) {
        if (project == null) return null;

        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setOwnerId(project.getOwner().getId());
        dto.setOwnerUsername(project.getOwner().getUsername());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        
        if (project.getMembers() != null) {
            Set<UUID> memberIds = project.getMembers().stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
            dto.setMemberIds(memberIds);
        }
        
        return dto;
    }

    public Project toEntity(ProjectRequest request) {
        if (request == null) return null;

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        // Members and Owner will be set in the service
        return project;
    }
}
