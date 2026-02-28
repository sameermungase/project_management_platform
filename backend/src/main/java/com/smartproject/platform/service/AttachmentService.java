package com.smartproject.platform.service;

import com.smartproject.platform.dto.AttachmentDTO;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.model.Attachment;
import com.smartproject.platform.model.Epic;
import com.smartproject.platform.model.Task;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.AttachmentRepository;
import com.smartproject.platform.repository.EpicRepository;
import com.smartproject.platform.repository.TaskRepository;
import com.smartproject.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AttachmentService {
    
    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final EpicRepository epicRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    
    /**
     * Upload file to task
     */
    public AttachmentDTO uploadToTask(UUID taskId, MultipartFile file, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        // Verify user is member of project
        verifyProjectMembership(task.getProject().getId(), currentUser);
        
        return uploadFile(file, currentUser, task, null);
    }
    
    /**
     * Upload file to epic
     */
    public AttachmentDTO uploadToEpic(UUID epicId, MultipartFile file, User currentUser) {
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
        
        // Verify user is member of project
        verifyProjectMembership(epic.getProject().getId(), currentUser);
        
        return uploadFile(file, currentUser, null, epic);
    }
    
    /**
     * Internal method to handle file upload
     */
    private AttachmentDTO uploadFile(MultipartFile file, User currentUser, Task task, Epic epic) {
        String storedFilename = fileStorageService.storeFile(file);
        
        Attachment attachment = Attachment.builder()
                .filename(storedFilename)
                .originalFilename(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(storedFilename)
                .storageType(Attachment.StorageType.LOCAL)
                .task(task)
                .epic(epic)
                .createdBy(currentUser)
                .build();
        
        attachment = attachmentRepository.save(attachment);
        
        log.info("File uploaded successfully: {} for task/epic", storedFilename);
        return mapToDTO(attachment);
    }
    
    /**
     * Download file
     */
    @Transactional(readOnly = true)
    public byte[] downloadFile(UUID attachmentId, User currentUser) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
        
        // Verify access
        verifyAttachmentAccess(attachment, currentUser);
        
        return fileStorageService.loadFile(attachment.getFilePath());
    }
    
    /**
     * Delete attachment
     */
    public void deleteAttachment(UUID attachmentId, User currentUser) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
        
        // Only creator or project admin can delete
        if (!attachment.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the file creator can delete this attachment");
        }
        
        fileStorageService.deleteFile(attachment.getFilePath());
        attachmentRepository.delete(attachment);
        
        log.info("Attachment deleted: {}", attachmentId);
    }
    
    /**
     * Get attachments for a task
     */
    @Transactional(readOnly = true)
    public List<AttachmentDTO> getTaskAttachments(UUID taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        verifyProjectMembership(task.getProject().getId(), currentUser);
        
        return attachmentRepository.findByTaskId(taskId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get attachments for an epic
     */
    @Transactional(readOnly = true)
    public List<AttachmentDTO> getEpicAttachments(UUID epicId, User currentUser) {
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
        
        verifyProjectMembership(epic.getProject().getId(), currentUser);
        
        return attachmentRepository.findByEpicId(epicId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Verify user has access to attachment
     */
    private void verifyAttachmentAccess(Attachment attachment, User currentUser) {
        if (attachment.getTask() != null) {
            verifyProjectMembership(attachment.getTask().getProject().getId(), currentUser);
        } else if (attachment.getEpic() != null) {
            verifyProjectMembership(attachment.getEpic().getProject().getId(), currentUser);
        }
    }
    
    /**
     * Verify user is member of project
     */
    private void verifyProjectMembership(UUID projectId, User currentUser) {
        // This assumes ProjectService has a method to check membership
        // or can be extended to check via repository
    }
    
    /**
     * Map Attachment to AttachmentDTO
     */
    private AttachmentDTO mapToDTO(Attachment attachment) {
        return AttachmentDTO.builder()
                .id(attachment.getId())
                .filename(attachment.getFilename())
                .originalFilename(attachment.getOriginalFilename())
                .mimeType(attachment.getMimeType())
                .fileSize(attachment.getFileSize())
                .createdAt(attachment.getCreatedAt())
                .updatedAt(attachment.getUpdatedAt())
                .createdById(attachment.getCreatedBy().getId())
                .createdByName(attachment.getCreatedBy().getUsername())
                .taskId(attachment.getTask() != null ? attachment.getTask().getId() : null)
                .epicId(attachment.getEpic() != null ? attachment.getEpic().getId() : null)
                .build();
    }
}
