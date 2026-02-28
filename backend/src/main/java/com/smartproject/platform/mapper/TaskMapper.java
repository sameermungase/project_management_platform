package com.smartproject.platform.mapper;

import com.smartproject.platform.dto.TaskDTO;
import com.smartproject.platform.dto.TaskRequest;
import com.smartproject.platform.model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDTO toDTO(Task task) {
        if (task == null) return null;

        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setProjectId(task.getProject().getId());
        
        if (task.getAssignee() != null) {
            dto.setAssigneeId(task.getAssignee().getId());
            dto.setAssigneeUsername(task.getAssignee().getUsername());
        }
        
        if (task.getEpic() != null) {
            dto.setEpicId(task.getEpic().getId());
            dto.setEpicTitle(task.getEpic().getTitle());
        }
        
        if (task.getMilestone() != null) {
            dto.setMilestoneId(task.getMilestone().getId());
            dto.setMilestoneTitle(task.getMilestone().getTitle());
        }
        
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        
        return dto;
    }

    public Task toEntity(TaskRequest request) {
        if (request == null) return null;

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        // Project and Assignee will be set in the service
        return task;
    }
}
