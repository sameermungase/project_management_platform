package com.smartproject.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class ProjectDTO {
    private UUID id;
    private String name;
    private String description;
    private UUID ownerId;
    private String ownerUsername;
    private Set<UUID> memberIds;
    private List<MemberInfo> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class MemberInfo {
        private UUID id;
        private String username;
        private String email;
    }
}
