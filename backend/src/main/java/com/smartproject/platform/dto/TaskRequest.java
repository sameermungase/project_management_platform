package com.smartproject.platform.dto;

import com.smartproject.platform.model.Priority;
import com.smartproject.platform.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class TaskRequest {
    @NotBlank(message = "Task title is required")
    private String title;
    
    private String description;
    
    private TaskStatus status;
    
    private Priority priority;
    
    private LocalDate dueDate;
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    private UUID assigneeId;
}
