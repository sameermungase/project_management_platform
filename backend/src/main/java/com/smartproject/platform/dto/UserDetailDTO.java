package com.smartproject.platform.dto;

import com.smartproject.platform.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {
    private UUID id;
    private String username;
    private String email;
    private Set<Role> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<ProjectSummaryDTO> projects; // Projects user is involved in
    private Set<TaskSummaryDTO> assignedTasks; // Tasks assigned to user
}
