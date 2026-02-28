package com.smartproject.platform.controller;

import com.smartproject.platform.model.Approval;
import com.smartproject.platform.service.ApprovalService;
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

@Tag(name = "Approvals", description = "Approval workflow endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {
    
    private final ApprovalService approvalService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Request approval", description = "Request approval for an entity")
    public ResponseEntity<Approval> requestApproval(
            @RequestParam UUID entityId,
            @RequestParam String entityType,
            @RequestParam(required = false) String comments) {
        // Service will get current user automatically
        return new ResponseEntity<>(
                approvalService.requestApproval(entityId, entityType, null, comments),
                HttpStatus.CREATED);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get pending approvals")
    public ResponseEntity<List<Approval>> getPendingApprovals() {
        return ResponseEntity.ok(approvalService.getPendingApprovalsForUser(null));
    }
    
    @GetMapping("/pending/page")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get pending approvals (paginated)")
    public ResponseEntity<Page<Approval>> getPendingApprovalsPaginated(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(approvalService.getPendingApprovals(pageable));
    }
    
    @PostMapping("/{approvalId}/approve")
    @PreAuthorize("hasAuthority('SENIOR') or hasAuthority('STAFF') or hasAuthority('PRINCIPAL') or hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Approve request", description = "Approve an approval request (SENIOR+ required)")
    public ResponseEntity<Approval> approve(
            @PathVariable UUID approvalId,
            @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(approvalService.approve(approvalId, null, comments));
    }
    
    @PostMapping("/{approvalId}/reject")
    @PreAuthorize("hasAuthority('SENIOR') or hasAuthority('STAFF') or hasAuthority('PRINCIPAL') or hasAuthority('ARCHITECT') or hasAuthority('ADMIN')")
    @Operation(summary = "Reject request", description = "Reject an approval request (SENIOR+ required)")
    public ResponseEntity<Approval> reject(
            @PathVariable UUID approvalId,
            @RequestParam String reason) {
        return ResponseEntity.ok(approvalService.reject(approvalId, null, reason));
    }
}
