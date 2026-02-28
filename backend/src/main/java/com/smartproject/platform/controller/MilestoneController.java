package com.smartproject.platform.controller;

import com.smartproject.platform.model.Milestone;
import com.smartproject.platform.model.MilestoneStatus;
import com.smartproject.platform.service.MilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Milestones", description = "Milestone management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/projects/{projectId}/milestones")
@RequiredArgsConstructor
public class MilestoneController {
    
    private final MilestoneService milestoneService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('SENIOR') or hasAuthority('STAFF') or hasAuthority('PRINCIPAL') or hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Create milestone", description = "Create a new milestone (SENIOR+ required)")
    public ResponseEntity<Milestone> createMilestone(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID epicId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        return new ResponseEntity<>(
                milestoneService.createMilestone(projectId, epicId, title, description, targetDate),
                HttpStatus.CREATED);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get milestones by project")
    public ResponseEntity<List<Milestone>> getMilestones(@PathVariable UUID projectId) {
        return ResponseEntity.ok(milestoneService.getMilestonesByProject(projectId));
    }
    
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get milestones by project (paginated)")
    public ResponseEntity<Page<Milestone>> getMilestonesPaginated(
            @PathVariable UUID projectId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(milestoneService.getMilestonesByProject(projectId, pageable));
    }
    
    @GetMapping("/{milestoneId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get milestone by ID")
    public ResponseEntity<Milestone> getMilestone(@PathVariable UUID projectId, @PathVariable UUID milestoneId) {
        return ResponseEntity.ok(milestoneService.getMilestoneById(milestoneId));
    }
    
    @PutMapping("/{milestoneId}")
    @PreAuthorize("hasAuthority('SENIOR') or hasAuthority('STAFF') or hasAuthority('PRINCIPAL') or hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Update milestone", description = "Update milestone details (SENIOR+ required)")
    public ResponseEntity<Milestone> updateMilestone(
            @PathVariable UUID projectId,
            @PathVariable UUID milestoneId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @RequestParam(required = false) MilestoneStatus status) {
        return ResponseEntity.ok(milestoneService.updateMilestone(milestoneId, title, description, targetDate, status));
    }
    
    @DeleteMapping("/{milestoneId}")
    @PreAuthorize("hasAuthority('SENIOR') or hasAuthority('STAFF') or hasAuthority('PRINCIPAL') or hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Delete milestone", description = "Delete a milestone (SENIOR+ required)")
    public ResponseEntity<Void> deleteMilestone(@PathVariable UUID projectId, @PathVariable UUID milestoneId) {
        milestoneService.deleteMilestone(milestoneId);
        return ResponseEntity.noContent().build();
    }
}
