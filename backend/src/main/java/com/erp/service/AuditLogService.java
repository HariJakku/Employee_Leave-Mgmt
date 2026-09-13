package com.erp.service;

import com.erp.dto.response.AuditLogResponseDto;
import com.erp.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void logAction(String action, String entityType, Long entityId, String description);
    PaginatedResponse<AuditLogResponseDto> getAuditLogs(String action, Long userId, Pageable pageable);
}
