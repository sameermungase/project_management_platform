package com.smartproject.platform.repository;

import com.smartproject.platform.model.Milestone;
import com.smartproject.platform.model.MilestoneStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {
    List<Milestone> findByProjectId(UUID projectId);
    Page<Milestone> findByProjectId(UUID projectId, Pageable pageable);
    List<Milestone> findByProjectIdAndStatus(UUID projectId, MilestoneStatus status);
    List<Milestone> findByEpicId(UUID epicId);
}
