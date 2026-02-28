package com.smartproject.platform.controller;

import com.smartproject.platform.dto.AttachmentDTO;
import com.smartproject.platform.model.User;
import com.smartproject.platform.security.services.UserDetailsImpl;
import com.smartproject.platform.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Attachments", description = "File attachment management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    
    private final AttachmentService attachmentService;
    
    @Operation(summary = "Upload file to task", description = "Upload a file attachment to a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a project member"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PostMapping("/task/{taskId}/upload")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<AttachmentDTO> uploadToTask(
            @Parameter(description = "Task UUID") @PathVariable UUID taskId,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        AttachmentDTO attachment = attachmentService.uploadToTask(taskId, file, currentUser);
        return new ResponseEntity<>(attachment, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Upload file to epic", description = "Upload a file attachment to an epic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a project member"),
            @ApiResponse(responseCode = "404", description = "Epic not found")
    })
    @PostMapping("/epic/{epicId}/upload")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<AttachmentDTO> uploadToEpic(
            @Parameter(description = "Epic UUID") @PathVariable UUID epicId,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        AttachmentDTO attachment = attachmentService.uploadToEpic(epicId, file, currentUser);
        return new ResponseEntity<>(attachment, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Download file", description = "Download an attachment file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - No access to attachment")
    })
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "Attachment UUID") @PathVariable UUID id,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        byte[] fileContent = attachmentService.downloadFile(id, currentUser);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=file")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileContent);
    }
    
    @Operation(summary = "Delete attachment", description = "Delete an attachment file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot delete other user's attachment")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "Attachment UUID") @PathVariable UUID id,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        attachmentService.deleteAttachment(id, currentUser);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Get task attachments", description = "Get all attachments for a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a project member")
    })
    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<AttachmentDTO>> getTaskAttachments(
            @Parameter(description = "Task UUID") @PathVariable UUID taskId,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        List<AttachmentDTO> attachments = attachmentService.getTaskAttachments(taskId, currentUser);
        return ResponseEntity.ok(attachments);
    }
    
    @Operation(summary = "Get epic attachments", description = "Get all attachments for an epic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Epic not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a project member")
    })
    @GetMapping("/epic/{epicId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<AttachmentDTO>> getEpicAttachments(
            @Parameter(description = "Epic UUID") @PathVariable UUID epicId,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        List<AttachmentDTO> attachments = attachmentService.getEpicAttachments(epicId, currentUser);
        return ResponseEntity.ok(attachments);
    }
    
    /**
     * Extract current user from Authentication
     */
    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());
        user.setUsername(userDetails.getUsername());
        return user;
    }
}
