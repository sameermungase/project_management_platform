package com.smartproject.platform.repository;

import com.smartproject.platform.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByTaskIdOrderByCreatedAtAsc(UUID taskId);
    List<Comment> findByProjectIdOrderByCreatedAtAsc(UUID projectId);
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);
    long countByTaskId(UUID taskId);
    long countByProjectId(UUID projectId);
}
