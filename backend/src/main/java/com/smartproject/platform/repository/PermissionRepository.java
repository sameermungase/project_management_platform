package com.smartproject.platform.repository;

import com.smartproject.platform.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
    
    @Query(value = "SELECT p.* FROM permissions p " +
           "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
           "WHERE rp.role = :role", nativeQuery = true)
    List<Permission> findByRole(@Param("role") String role);
}
