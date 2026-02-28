package com.smartproject.platform.repository;

import com.smartproject.platform.model.TaskEstimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskEstimateRepository extends JpaRepository<TaskEstimate, UUID> {
    List<TaskEstimate> findByTaskId(UUID taskId);
    List<TaskEstimate> findByEstimatedById(UUID estimatedById);
}
