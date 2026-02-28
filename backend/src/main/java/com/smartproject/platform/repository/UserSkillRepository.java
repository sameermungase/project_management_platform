package com.smartproject.platform.repository;

import com.smartproject.platform.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {
    List<UserSkill> findByUserId(UUID userId);
    List<UserSkill> findBySkillName(String skillName);
    boolean existsByUserIdAndSkillName(UUID userId, String skillName);
}
