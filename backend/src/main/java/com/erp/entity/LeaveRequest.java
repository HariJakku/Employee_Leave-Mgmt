package com.erp.entity;

import com.erp.enums.LeaveStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A leave request submitted by an employee.
 *
 * Status lifecycle (see LeaveStatus enum for full diagram):
 *   PENDING → MANAGER_APPROVED → APPROVED
 *   PENDING / MANAGER_APPROVED → REJECTED
 *   PENDING / MANAGER_APPROVED → CANCELLED
 */
@Entity
@Table(
    name = "leave_requests",
    indexes = {
        @Index(name = "idx_lr_employee",   columnList = "employee_id"),
        @Index(name = "idx_lr_status",     columnList = "status"),
        @Index(name = "idx_lr_dates",      columnList = "start_date, end_date")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal totalDays;

    @Column(length = 1000)
    private String reason;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "applied_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // One leave request can have multiple approval records (manager stage + HR stage)
    @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LeaveApproval> approvals = new ArrayList<>();
}
