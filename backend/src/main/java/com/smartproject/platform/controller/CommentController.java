package com.smartproject.platform.controller;

import com.smartproject.platform.dto.CommentDTO;
import com.smartproject.platform.dto.CommentRequest;
import com.smartproject.platform.service.CommentService;
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
@RequestMapping("/api/comments")
@Tag(name = "Comments", description = "Comment management endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Create a comment", description = "Create a new comment on a task or project")
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentRequest request) {
        return new ResponseEntity<>(commentService.createComment(request), HttpStatus.CREATED);
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get comments by task", description = "Get all comments for a specific task")
    public ResponseEntity<List<CommentDTO>> getCommentsByTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(commentService.getCommentsByTask(taskId));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get comments by project", description = "Get all comments for a specific project")
    public ResponseEntity<List<CommentDTO>> getCommentsByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(commentService.getCommentsByProject(projectId));
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Update comment", description = "Update an existing comment (only own comments)")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Delete comment", description = "Delete a comment (only own comments)")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/reactions")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Add reaction to comment", description = "Add an emoji reaction to a comment")
    public ResponseEntity<CommentDTO> addReaction(
            @PathVariable UUID commentId,
            @RequestParam String emoji) {
        return ResponseEntity.ok(commentService.addReaction(commentId, emoji));
    }

    @DeleteMapping("/{commentId}/reactions")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Remove reaction from comment", description = "Remove an emoji reaction from a comment")
    public ResponseEntity<CommentDTO> removeReaction(
            @PathVariable UUID commentId,
            @RequestParam String emoji) {
        return ResponseEntity.ok(commentService.removeReaction(commentId, emoji));
    }
}
