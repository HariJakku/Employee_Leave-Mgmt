package com.erp.entity;

import com.erp.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

/**
 * Configurable leave type (e.g., Casual Leave, Sick Leave).
 * Admin/HR manages these; limits are applied per employee per year in LeaveBalance.
 */
@Entity
@Table(name = "leave_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;  // e.g., "Casual Leave"

    @Column(length = 300)
    private String description;

    @Column(name = "max_days_per_year", nullable = false)
    private Integer maxDaysPerYear;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private Boolean isPaid = true;

    /**
     * Gender restriction for leaves like Maternity / Paternity.
     * Null or ALL = applicable to all employees.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_gender", length = 10)
    private Gender applicableGender;  // null = ALL

    @Column(name = "requires_document", nullable = false)
    @Builder.Default
    private Boolean requiresDocument = false;

    @Column(name = "min_notice_days", nullable = false)
    @Builder.Default
    private Integer minNoticeDays = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
