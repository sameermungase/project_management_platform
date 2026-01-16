package com.smartproject.platform.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDTO {
    private UUID id;
    private String emoji;
    private UUID userId;
    private String username;
    private LocalDateTime createdAt;
}
