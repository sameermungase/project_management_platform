package com.smartproject.platform.controller;

import com.smartproject.platform.model.DecisionLog;
import com.smartproject.platform.model.DecisionType;
import com.smartproject.platform.model.VisibilityLevel;
import com.smartproject.platform.service.DecisionLogService;
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

@Tag(name = "Decision Logs", description = "Technical decision logging endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/projects/{projectId}/decisions")
@RequiredArgsConstructor
public class DecisionLogController {
    
    private final DecisionLogService decisionLogService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Create decision log", description = "Log a technical decision")
    public ResponseEntity<DecisionLog> createDecision(
            @PathVariable UUID projectId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) DecisionType decisionType,
            @RequestParam(required = false) VisibilityLevel visibilityLevel) {
        return new ResponseEntity<>(
                decisionLogService.createDecision(projectId, title, description, decisionType, visibilityLevel),
                HttpStatus.CREATED);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get decisions by project")
    public ResponseEntity<List<DecisionLog>> getDecisions(@PathVariable UUID projectId) {
        return ResponseEntity.ok(decisionLogService.getDecisionsByProject(projectId));
    }
    
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get decisions by project (paginated)")
    public ResponseEntity<Page<DecisionLog>> getDecisionsPaginated(
            @PathVariable UUID projectId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(decisionLogService.getDecisionsByProject(projectId, pageable));
    }
    
    @GetMapping("/{decisionId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get decision by ID")
    public ResponseEntity<DecisionLog> getDecision(@PathVariable UUID projectId, @PathVariable UUID decisionId) {
        return ResponseEntity.ok(decisionLogService.getDecisionById(decisionId));
    }
    
    @PostMapping("/{decisionId}/approve")
    @PreAuthorize("hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Approve decision", description = "Approve a decision (ARCHITECT+ required)")
    public ResponseEntity<DecisionLog> approveDecision(
            @PathVariable UUID projectId,
            @PathVariable UUID decisionId) {
        return ResponseEntity.ok(decisionLogService.approveDecision(decisionId, null));
    }
}
