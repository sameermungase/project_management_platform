package com.smartproject.platform.controller;

import com.smartproject.platform.dto.MemberManagementRequest;
import com.smartproject.platform.dto.MessageResponse;
import com.smartproject.platform.dto.ProjectDTO;
import com.smartproject.platform.dto.ProjectRequest;
import com.smartproject.platform.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Create a new project", description = "Creates a new project with the authenticated user as owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectRequest request) {
        return new ResponseEntity<>(projectService.createProject(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all projects", description = "Returns paginated list of projects the user owns or is a member of")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Page<ProjectDTO>> getAllProjects(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable));
    }

    @Operation(summary = "Get project by ID", description = "Returns project details if user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<ProjectDTO> getProjectById(
            @Parameter(description = "Project UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @Operation(summary = "Update project", description = "Updates project details - only owner can update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only owner can update"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<ProjectDTO> updateProject(
            @Parameter(description = "Project UUID") @PathVariable UUID id, 
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @Operation(summary = "Delete project", description = "Deletes a project - only owner can delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only owner can delete"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project UUID") @PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add member to project", description = "Add a user as a member to the project (Owner, Manager, Admin, TL can add)")
    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN') or hasAuthority('TECHNICAL_LEAD')")
    public ResponseEntity<MessageResponse> addMemberToProject(
            @Parameter(description = "Project UUID") @PathVariable UUID id,
            @Valid @RequestBody MemberManagementRequest request) {
        projectService.addMemberToProject(id, request.getUserId());
        return ResponseEntity.ok(new MessageResponse("Member added successfully"));
    }

    @Operation(summary = "Remove member from project", description = "Remove a user from project members (Owner, Manager, Admin, TL can remove)")
    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN') or hasAuthority('TECHNICAL_LEAD')")
    public ResponseEntity<MessageResponse> removeMemberFromProject(
            @Parameter(description = "Project UUID") @PathVariable UUID id,
            @Parameter(description = "User UUID") @PathVariable UUID userId) {
        projectService.removeMemberFromProject(id, userId);
        return ResponseEntity.ok(new MessageResponse("Member removed successfully"));
    }

    @Operation(summary = "Get all projects (Admin)", description = "Get all projects in the organization (Admin only)")
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<ProjectDTO>> getAllProjectsAdmin(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjectsForAdmin(pageable));
    }
}
