package com.erp.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Organizational department.
 * The department head references an Employee (self-owning side in a bidirectional
 * relationship — we use the Employee ID to avoid circular loading issues).
 */
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_dept_name", columnList = "name")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * Department head — optional FK to employees.id.
     * Stored as a plain Long to avoid complex bi-directional issues;
     * resolved in the service layer.
     */
    @Column(name = "head_employee_id")
    private Long headEmployeeId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
