package com.smartproject.platform.service;

import com.smartproject.platform.dto.CommentDTO;
import com.smartproject.platform.dto.CommentRequest;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.model.Comment;
import com.smartproject.platform.model.Project;
import com.smartproject.platform.model.Task;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.CommentRepository;
import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.TaskRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public CommentDTO createComment(CommentRequest request) {
        User currentUser = getCurrentUser();
        
        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(currentUser)
                .build();
        
        if (request.getTaskId() != null) {
            Task task = taskRepository.findById(request.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
            comment.setTask(task);
            
            activityLogService.logActivity(currentUser, "COMMENT_CREATED", 
                    "Commented on task: " + task.getTitle(), task.getId(), "TASK");
        } else if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            comment.setProject(project);
            
            activityLogService.logActivity(currentUser, "COMMENT_CREATED", 
                    "Commented on project: " + project.getName(), project.getId(), "PROJECT");
        } else {
            throw new IllegalArgumentException("Either taskId or projectId must be provided");
        }
        
        if (request.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParentComment(parent);
        }
        
        Comment saved = commentRepository.save(comment);
        return toDTO(saved, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByTask(UUID taskId) {
        User currentUser = getCurrentUser();
        List<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        return comments.stream()
                .filter(c -> c.getParentComment() == null) // Only top-level comments
                .map(c -> toDTO(c, currentUser.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByProject(UUID projectId) {
        User currentUser = getCurrentUser();
        List<Comment> comments = commentRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
        return comments.stream()
                .filter(c -> c.getParentComment() == null)
                .map(c -> toDTO(c, currentUser.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDTO updateComment(UUID commentId, CommentRequest request) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only edit your own comments");
        }
        
        comment.setContent(request.getContent());
        Comment updated = commentRepository.save(comment);
        
        activityLogService.logActivity(currentUser, "COMMENT_UPDATED", 
                "Updated comment", commentId, "COMMENT");
        
        return toDTO(updated, currentUser.getId());
    }

    @Transactional
    public void deleteComment(UUID commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only delete your own comments");
        }
        
        commentRepository.delete(comment);
        
        activityLogService.logActivity(currentUser, "COMMENT_DELETED", 
                "Deleted comment", commentId, "COMMENT");
    }

    @Transactional
    public CommentDTO addReaction(UUID commentId, String emoji) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        // Check if reaction already exists
        boolean exists = comment.getReactions().stream()
                .anyMatch(r -> r.getUser().getId().equals(currentUser.getId()) && r.getEmoji().equals(emoji));
        
        if (!exists) {
            com.smartproject.platform.model.CommentReaction reaction = 
                    com.smartproject.platform.model.CommentReaction.builder()
                            .comment(comment)
                            .user(currentUser)
                            .emoji(emoji)
                            .build();
            comment.getReactions().add(reaction);
            commentRepository.save(comment);
        }
        
        return toDTO(comment, currentUser.getId());
    }

    @Transactional
    public CommentDTO removeReaction(UUID commentId, String emoji) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        comment.getReactions().removeIf(r -> 
                r.getUser().getId().equals(currentUser.getId()) && r.getEmoji().equals(emoji));
        commentRepository.save(comment);
        
        return toDTO(comment, currentUser.getId());
    }

    private CommentDTO toDTO(Comment comment, UUID currentUserId) {
        CommentDTO dto = CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userEmail(comment.getUser().getEmail())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .canEdit(comment.getUser().getId().equals(currentUserId))
                .canDelete(comment.getUser().getId().equals(currentUserId))
                .build();
        
        if (comment.getTask() != null) {
            dto.setTaskId(comment.getTask().getId());
        }
        if (comment.getProject() != null) {
            dto.setProjectId(comment.getProject().getId());
        }
        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }
        
        // Map replies
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(reply -> toDTO(reply, currentUserId))
                    .collect(Collectors.toList()));
        }
        
        // Map reactions
        if (comment.getReactions() != null && !comment.getReactions().isEmpty()) {
            dto.setReactions(comment.getReactions().stream()
                    .map(r -> {
                        com.smartproject.platform.dto.ReactionDTO reaction = 
                                com.smartproject.platform.dto.ReactionDTO.builder()
                                        .id(r.getId())
                                        .emoji(r.getEmoji())
                                        .userId(r.getUser().getId())
                                        .username(r.getUser().getUsername())
                                        .createdAt(r.getCreatedAt())
                                        .build();
                        return reaction;
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
