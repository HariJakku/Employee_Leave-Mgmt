package com.erp.service.impl;

import com.erp.dto.response.AuditLogResponseDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.entity.AuditLog;
import com.erp.entity.User;
import com.erp.repository.AuditLogRepository;
import com.erp.security.SecurityUtils;
import com.erp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityType, Long entityId, String description) {
        try {
            User user = null;
            try {
                user = securityUtils.getCurrentUser();
            } catch (Exception e) {
                // Background or unauthenticated action
            }

            AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Recorded audit log: action={}, entity={}", action, entityType);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<AuditLogResponseDto> getAuditLogs(String action, Long userId, Pageable pageable) {
        Page<AuditLog> page;
        if (action != null && !action.isBlank()) {
            page = auditLogRepository.findByAction(action, pageable);
        } else if (userId != null) {
            page = auditLogRepository.findByUserId(userId, pageable);
        } else {
            page = auditLogRepository.findByOrderByCreatedAtDesc(pageable);
        }

        return PaginatedResponse.fromPage(page.map(a -> AuditLogResponseDto.builder()
            .id(a.getId())
            .userId(a.getUser() != null ? a.getUser().getId() : null)
            .userEmail(a.getUser() != null ? a.getUser().getEmail() : "System")
            .action(a.getAction())
            .entityType(a.getEntityType())
            .entityId(a.getEntityId())
            .description(a.getDescription())
            .ipAddress(a.getIpAddress())
            .createdAt(a.getCreatedAt())
            .build()));
    }
}
