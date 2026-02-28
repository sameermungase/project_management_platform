package com.smartproject.platform.repository;

import com.smartproject.platform.model.ProjectMemberRole;
import com.smartproject.platform.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRoleRepository extends JpaRepository<ProjectMemberRole, UUID> {
    Optional<ProjectMemberRole> findByProjectIdAndUserId(UUID projectId, UUID userId);
    List<ProjectMemberRole> findByProjectId(UUID projectId);
    List<ProjectMemberRole> findByUserId(UUID userId);
    List<ProjectMemberRole> findByProjectIdAndRole(UUID projectId, Role role);
    boolean existsByProjectIdAndUserIdAndRole(UUID projectId, UUID userId, Role role);
}
