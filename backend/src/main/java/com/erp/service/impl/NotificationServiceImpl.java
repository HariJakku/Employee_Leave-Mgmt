package com.erp.service.impl;

import com.erp.dto.response.NotificationResponseDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.entity.Notification;
import com.erp.entity.User;
import com.erp.exception.ResourceNotFoundException;
import com.erp.exception.UnauthorizedException;
import com.erp.repository.NotificationRepository;
import com.erp.security.SecurityUtils;
import com.erp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<NotificationResponseDto> getMyNotifications(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<Notification> page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        return PaginatedResponse.fromPage(page.map(this::mapToDto));
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = securityUtils.getCurrentUser();
        return notificationRepository.countByRecipientIdAndIsReadFalse(currentUser.getId());
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Notification n = notificationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        if (!n.getRecipient().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Cannot access other user's notification");
        }

        n.setIsRead(true);
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User currentUser = securityUtils.getCurrentUser();
        notificationRepository.markAllReadByUserId(currentUser.getId());
        log.info("Marked all notifications as read for user: {}", currentUser.getEmail());
    }

    private NotificationResponseDto mapToDto(Notification n) {
        return NotificationResponseDto.builder()
            .id(n.getId())
            .title(n.getTitle())
            .message(n.getMessage())
            .type(n.getType())
            .isRead(n.getIsRead())
            .entityId(n.getEntityId())
            .entityType(n.getEntityType())
            .createdAt(n.getCreatedAt())
            .build();
    }
}
