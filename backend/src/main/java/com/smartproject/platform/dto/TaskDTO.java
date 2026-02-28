package com.smartproject.platform.dto;

import com.smartproject.platform.model.Priority;
import com.smartproject.platform.model.TaskStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskDTO {
    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;
    private UUID projectId;
    private UUID assigneeId;
    private String assigneeUsername;
    private UUID epicId;
    private String epicTitle;
    private UUID milestoneId;
    private String milestoneTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
