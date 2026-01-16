package com.smartproject.platform.dto;

import com.smartproject.platform.model.TaskDependency;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TaskDependencyRequest {
    @NotNull(message = "Task ID is required")
    private UUID taskId;
    
    @NotNull(message = "Depends on task ID is required")
    private UUID dependsOnTaskId;
    
    private TaskDependency.DependencyType dependencyType = TaskDependency.DependencyType.BLOCKS;
}
