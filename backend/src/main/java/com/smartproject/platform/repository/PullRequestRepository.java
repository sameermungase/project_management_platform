package com.smartproject.platform.repository;

import com.smartproject.platform.model.PullRequest;
import com.smartproject.platform.model.PRStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, UUID> {
    List<PullRequest> findByProjectId(UUID projectId);
    Page<PullRequest> findByProjectId(UUID projectId, Pageable pageable);
    List<PullRequest> findByProjectIdAndStatus(UUID projectId, PRStatus status);
    List<PullRequest> findByCreatedById(UUID createdById);
}
