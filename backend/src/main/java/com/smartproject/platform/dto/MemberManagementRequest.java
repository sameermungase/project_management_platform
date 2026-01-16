package com.smartproject.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MemberManagementRequest {
    @NotNull
    private UUID userId;
}
