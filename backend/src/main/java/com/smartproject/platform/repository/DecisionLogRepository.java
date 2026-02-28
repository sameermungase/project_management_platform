package com.smartproject.platform.repository;

import com.smartproject.platform.model.DecisionLog;
import com.smartproject.platform.model.DecisionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DecisionLogRepository extends JpaRepository<DecisionLog, UUID> {
    List<DecisionLog> findByProjectId(UUID projectId);
    Page<DecisionLog> findByProjectId(UUID projectId, Pageable pageable);
    List<DecisionLog> findByProjectIdAndStatus(UUID projectId, DecisionStatus status);
    List<DecisionLog> findByDecidedById(UUID decidedById);
}
