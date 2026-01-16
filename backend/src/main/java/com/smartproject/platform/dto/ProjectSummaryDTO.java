package com.smartproject.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryDTO {
    private UUID id;
    private String name;
    private String ownerUsername;
    private String role; // "OWNER" or "MEMBER"
}
