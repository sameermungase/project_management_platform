package com.smartproject.platform.service;

import com.smartproject.platform.model.ActivityLog;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Async
    @Transactional
    public void logActivity(User user, String action, String details, UUID entityId, String entityType) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .entityId(entityId)
                .entityType(entityType)
                .build();
        activityLogRepository.save(log);
    }
}
