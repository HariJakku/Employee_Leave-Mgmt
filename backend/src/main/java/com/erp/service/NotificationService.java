package com.erp.service;

import com.erp.dto.response.NotificationResponseDto;
import com.erp.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    PaginatedResponse<NotificationResponseDto> getMyNotifications(Pageable pageable);
    long getUnreadCount();
    void markAsRead(Long id);
    void markAllAsRead();
}
