package com.smartproject.platform.repository;

import com.smartproject.platform.model.Epic;
import com.smartproject.platform.model.EpicStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EpicRepository extends JpaRepository<Epic, UUID> {
    List<Epic> findByProjectId(UUID projectId);
    Page<Epic> findByProjectId(UUID projectId, Pageable pageable);
    List<Epic> findByProjectIdAndStatus(UUID projectId, EpicStatus status);
}
