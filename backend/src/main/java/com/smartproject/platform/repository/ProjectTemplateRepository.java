package com.smartproject.platform.repository;

import com.smartproject.platform.model.ProjectTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, UUID> {
    List<ProjectTemplate> findByIsPublicTrue();
    List<ProjectTemplate> findByCreatedById(UUID createdById);
    List<ProjectTemplate> findByTemplateType(String templateType);
    List<ProjectTemplate> findByIsPublicTrueOrCreatedById(UUID createdById);
}
