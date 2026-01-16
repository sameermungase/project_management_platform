package com.smartproject.platform.dto;

import com.smartproject.platform.model.Priority;
import com.smartproject.platform.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryDTO {
    private UUID id;
    private String title;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;
    private String projectName;
}
