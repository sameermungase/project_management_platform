package com.smartproject.platform.repository;

import com.smartproject.platform.model.Approval;
import com.smartproject.platform.model.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, UUID> {
    List<Approval> findByRequestedById(UUID requestedById);
    List<Approval> findByApprovedById(UUID approvedById);
    List<Approval> findByStatus(ApprovalStatus status);
    Page<Approval> findByStatus(ApprovalStatus status, Pageable pageable);
    Optional<Approval> findByEntityTypeAndEntityId(String entityType, UUID entityId);
    List<Approval> findByEntityTypeAndEntityIdAndStatus(String entityType, UUID entityId, ApprovalStatus status);
}
