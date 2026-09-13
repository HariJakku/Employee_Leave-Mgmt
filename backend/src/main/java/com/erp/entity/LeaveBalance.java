package com.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Tracks leave balance per employee per leave type per year.
 * Unique constraint: one balance record per (employee, leave_type, year).
 *
 * Business rules:
 *   - remaining_days = allocated_days - used_days - pending_days
 *   - balance NOT deducted when leave is PENDING
 *   - pending_days incremented when applied; moved to used_days on APPROVED
 *   - balance restored on CANCELLED (if was APPROVED)
 */
@Entity
@Table(
    name = "leave_balances",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_leave_balance",
        columnNames = {"employee_id", "leave_type_id", "year"}
    ),
    indexes = {
        @Index(name = "idx_lb_employee", columnList = "employee_id"),
        @Index(name = "idx_lb_year",     columnList = "year")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "allocated_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal allocatedDays;

    @Column(name = "used_days", nullable = false, precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal usedDays = BigDecimal.ZERO;

    @Column(name = "pending_days", nullable = false, precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal pendingDays = BigDecimal.ZERO;

    /** Derived: not stored — computed in Java to avoid stale reads. */
    @Transient
    public BigDecimal getRemainingDays() {
        return allocatedDays
            .subtract(usedDays)
            .subtract(pendingDays);
    }
}
