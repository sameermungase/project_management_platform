package com.smartproject.platform.dto;

import com.smartproject.platform.model.TaskDependency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDependencyDTO {
    private UUID id;
    private UUID taskId;
    private String taskTitle;
    private UUID dependsOnTaskId;
    private String dependsOnTaskTitle;
    private TaskDependency.DependencyType dependencyType;
    private LocalDateTime createdAt;
}
