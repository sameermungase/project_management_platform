package com.smartproject.platform.controller;

import com.smartproject.platform.dto.ProjectDTO;
import com.smartproject.platform.dto.ProjectRequest;
import com.smartproject.platform.dto.ProjectTemplateDTO;
import com.smartproject.platform.dto.ProjectTemplateRequest;
import com.smartproject.platform.service.ProjectTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates")
@Tag(name = "Project Templates", description = "Project template management endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class ProjectTemplateController {

    private final ProjectTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Create project template", description = "Create a new project template")
    public ResponseEntity<ProjectTemplateDTO> createTemplate(@Valid @RequestBody ProjectTemplateRequest request) {
        return new ResponseEntity<>(templateService.createTemplate(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get all templates", description = "Get all available templates (public and own)")
    public ResponseEntity<List<ProjectTemplateDTO>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/public")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get public templates", description = "Get all public project templates")
    public ResponseEntity<List<ProjectTemplateDTO>> getPublicTemplates() {
        return ResponseEntity.ok(templateService.getPublicTemplates());
    }

    @GetMapping("/{templateId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get template by ID", description = "Get a specific project template")
    public ResponseEntity<ProjectTemplateDTO> getTemplateById(@PathVariable UUID templateId) {
        return ResponseEntity.ok(templateService.getTemplateById(templateId));
    }

    @PostMapping("/{templateId}/create-project")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Create project from template", description = "Create a new project using a template")
    public ResponseEntity<ProjectDTO> createProjectFromTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody ProjectRequest projectRequest) {
        return new ResponseEntity<>(
                templateService.createProjectFromTemplate(templateId, projectRequest),
                HttpStatus.CREATED);
    }

    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Delete template", description = "Delete a project template (only own templates)")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
}
