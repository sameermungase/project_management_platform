package com.smartproject.platform.repository;

import com.smartproject.platform.model.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, UUID> {
    List<TaskDependency> findByTaskId(UUID taskId);
    List<TaskDependency> findByDependsOnTaskId(UUID dependsOnTaskId);
    
    @Query("SELECT td FROM TaskDependency td WHERE td.task.id = :taskId OR td.dependsOnTask.id = :taskId")
    List<TaskDependency> findAllRelatedDependencies(UUID taskId);
    
    boolean existsByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);
}
