package com.smartproject.platform.controller;

import com.smartproject.platform.model.Epic;
import com.smartproject.platform.model.EpicStatus;
import com.smartproject.platform.model.VisibilityLevel;
import com.smartproject.platform.service.EpicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Epics", description = "Epic management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/projects/{projectId}/epics")
@RequiredArgsConstructor
public class EpicController {
    
    private final EpicService epicService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('SENIOR') or hasAuthority('STAFF') or hasAuthority('PRINCIPAL') or hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Create epic", description = "Create a new epic (SENIOR+ required)")
    public ResponseEntity<Epic> createEpic(
            @PathVariable UUID projectId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) VisibilityLevel visibilityLevel) {
        return new ResponseEntity<>(
                epicService.createEpic(projectId, title, description, visibilityLevel),
                HttpStatus.CREATED);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get epics by project", description = "Get all epics for a project")
    public ResponseEntity<List<Epic>> getEpics(@PathVariable UUID projectId) {
        return ResponseEntity.ok(epicService.getEpicsByProject(projectId));
    }
    
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get epics by project (paginated)")
    public ResponseEntity<Page<Epic>> getEpicsPaginated(
            @PathVariable UUID projectId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(epicService.getEpicsByProject(projectId, pageable));
    }
    
    @GetMapping("/{epicId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get epic by ID")
    public ResponseEntity<Epic> getEpic(@PathVariable UUID projectId, @PathVariable UUID epicId) {
        return ResponseEntity.ok(epicService.getEpicById(epicId));
    }
    
    @PutMapping("/{epicId}")
    @PreAuthorize("hasAuthority('SENIOR') or hasAuthority('STAFF') or hasAuthority('PRINCIPAL') or hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Update epic", description = "Update epic details (SENIOR+ required)")
    public ResponseEntity<Epic> updateEpic(
            @PathVariable UUID projectId,
            @PathVariable UUID epicId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) EpicStatus status) {
        return ResponseEntity.ok(epicService.updateEpic(epicId, title, description, status));
    }
    
    @DeleteMapping("/{epicId}")
    @PreAuthorize("hasAuthority('SENIOR') or hasAuthority('STAFF') or hasAuthority('PRINCIPAL') or hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Delete epic", description = "Delete an epic (SENIOR+ required)")
    public ResponseEntity<Void> deleteEpic(@PathVariable UUID projectId, @PathVariable UUID epicId) {
        epicService.deleteEpic(epicId);
        return ResponseEntity.noContent().build();
    }
}
