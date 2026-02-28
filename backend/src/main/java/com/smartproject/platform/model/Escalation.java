package com.smartproject.platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "escalations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Escalation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_by", nullable = false)
    private User escalatedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_to", nullable = false)
    private User escalatedTo;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EscalationStatus status = EscalationStatus.OPEN;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
}
