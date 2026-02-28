package com.smartproject.platform.repository;

import com.smartproject.platform.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByTaskId(UUID taskId);
    List<Attachment> findByEpicId(UUID epicId);
    List<Attachment> findByCreatedById(UUID userId);
}
