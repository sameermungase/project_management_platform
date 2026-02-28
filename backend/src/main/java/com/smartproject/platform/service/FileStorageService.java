package com.smartproject.platform.service;

import com.smartproject.platform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {
    
    @Value("${file.storage.location:uploads}")
    private String storageLocation;
    
    @Value("${file.max-size:10485760}")  // 10MB default
    private long maxFileSize;
    
    @Value("${file.allowed-types:application/pdf,image/jpeg,image/png,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.openxmlformats-officedocument.presentationml.presentation}")
    private String allowedMimeTypes;
    
    
    /**
     * Store a file and return the storage path
     */
    public String storeFile(MultipartFile file) {
        try {
            validateFile(file);
            
            Path uploadDir = Paths.get(storageLocation).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            
            String filename = generateUniqueFilename(file.getOriginalFilename());
            Path targetPath = uploadDir.resolve(filename);
            
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("File stored successfully: {}", filename);
            return filename;
            
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }
    
    /**
     * Retrieve a file as byte array
     */
    public byte[] loadFile(String filename) {
        try {
            Path filePath = Paths.get(storageLocation).toAbsolutePath().normalize().resolve(filename);
            
            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("File not found: " + filename);
            }
            
            return Files.readAllBytes(filePath);
            
        } catch (IOException e) {
            log.error("Failed to load file: {}", filename, e);
            throw new RuntimeException("Failed to load file: " + e.getMessage());
        }
    }
    
    /**
     * Delete a file
     */
    public void deleteFile(String filename) {
        try {
            Path filePath = Paths.get(storageLocation).toAbsolutePath().normalize().resolve(filename);
            
            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("File not found: " + filename);
            }
            
            Files.delete(filePath);
            log.info("File deleted successfully: {}", filename);
            
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filename, e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }
    
    /**
     * Validate file before storage
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }
        
        String mimeType = file.getContentType();
        if (!isAllowedMimeType(mimeType)) {
            throw new IllegalArgumentException("File type not allowed: " + mimeType);
        }
    }
    
    /**
     * Check if MIME type is allowed
     */
    private boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        String[] types = allowedMimeTypes.split(",");
        for (String type : types) {
            if (type.trim().equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Generate unique filename to avoid collisions
     */
    private String generateUniqueFilename(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFilename);
        return uuid + (extension.isEmpty() ? "" : "." + extension);
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Get original filename from stored filename (metadata purposes)
     */
    public String extractOriginalFilename(String storedFilename) {
        return storedFilename;  // In real implementation, this could be retrieved from metadata
    }
}
