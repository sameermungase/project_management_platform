package com.smartproject.platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_estimates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEstimate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimated_by", nullable = false)
    private User estimatedBy;
    
    @Column(name = "estimated_hours", precision = 10, scale = 2)
    private BigDecimal estimatedHours;
    
    @Column(name = "estimated_days")
    private Integer estimatedDays;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level")
    private ConfidenceLevel confidenceLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estimation_type")
    @Builder.Default
    private EstimationType estimationType = EstimationType.INITIAL;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
