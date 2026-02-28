package com.smartproject.platform.repository;

import com.smartproject.platform.model.CodeReview;
import com.smartproject.platform.model.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, UUID> {
    List<CodeReview> findByPullRequestId(UUID prId);
    List<CodeReview> findByReviewerId(UUID reviewerId);
    List<CodeReview> findByPullRequestIdAndStatus(UUID prId, ReviewStatus status);
}
