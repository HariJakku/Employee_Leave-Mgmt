package com.erp.entity;

import com.erp.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily attendance record per employee.
 * UNIQUE constraint prevents multiple check-ins on the same day.
 */
@Entity
@Table(
    name = "attendance",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_employee_date",
        columnNames = {"employee_id", "date"}
    ),
    indexes = {
        @Index(name = "idx_att_employee", columnList = "employee_id"),
        @Index(name = "idx_att_date",     columnList = "date")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    /**
     * Working hours calculated on check-out.
     * Stored as DECIMAL(4,2) — e.g., 8.5 means 8 hours 30 minutes.
     */
    @Column(name = "working_hours", precision = 4, scale = 2)
    private BigDecimal workingHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Column(length = 300)
    private String notes;
}
