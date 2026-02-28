package com.smartproject.platform.repository;

import com.smartproject.platform.model.TaskRequiredSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRequiredSkillRepository extends JpaRepository<TaskRequiredSkill, UUID> {
    List<TaskRequiredSkill> findByTaskId(UUID taskId);
    List<TaskRequiredSkill> findBySkillName(String skillName);
}
