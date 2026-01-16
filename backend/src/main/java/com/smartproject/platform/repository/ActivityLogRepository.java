package com.smartproject.platform.repository;

import com.smartproject.platform.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    Page<ActivityLog> findByEntityId(UUID entityId, Pageable pageable);
    Page<ActivityLog> findByUserId(UUID userId, Pageable pageable);
}
