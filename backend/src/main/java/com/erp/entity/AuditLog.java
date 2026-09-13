package com.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable audit trail record.
 * Created by AuditLogService and triggered via AOP in Phase 14.
 * No updatedAt — audit records must never be modified.
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_user",    columnList = "user_id"),
        @Index(name = "idx_audit_action",  columnList = "action"),
        @Index(name = "idx_audit_entity",  columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_created", columnList = "created_at")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String action;  // e.g., EMPLOYEE_CREATED, LEAVE_APPROVED

    @Column(name = "entity_type", length = 50)
    private String entityType;  // e.g., Employee, LeaveRequest

    @Column(name = "entity_id")
    private Long entityId;

    @Column(length = 1000)
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
