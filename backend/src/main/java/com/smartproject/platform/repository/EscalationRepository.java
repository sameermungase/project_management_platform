package com.smartproject.platform.repository;

import com.smartproject.platform.model.Escalation;
import com.smartproject.platform.model.EscalationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EscalationRepository extends JpaRepository<Escalation, UUID> {
    List<Escalation> findByEscalatedById(UUID escalatedById);
    List<Escalation> findByEscalatedToId(UUID escalatedToId);
    List<Escalation> findByStatus(EscalationStatus status);
    Page<Escalation> findByStatus(EscalationStatus status, Pageable pageable);
    Optional<Escalation> findByEntityTypeAndEntityId(String entityType, UUID entityId);
}
