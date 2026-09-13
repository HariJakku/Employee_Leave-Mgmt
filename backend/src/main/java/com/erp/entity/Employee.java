package com.erp.entity;

import com.erp.enums.EmployeeStatus;
import com.erp.enums.EmploymentType;
import com.erp.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Core employee record.
 * Has self-referential manager relationship and FK to User and Department.
 */
@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_emp_email",      columnList = "email"),
    @Index(name = "idx_emp_dept",       columnList = "department_id"),
    @Index(name = "idx_emp_status",     columnList = "status"),
    @Index(name = "idx_emp_manager",    columnList = "manager_id"),
    @Index(name = "idx_emp_code",       columnList = "employee_code")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 20)
    private String employeeCode;   // e.g., EMP-0001

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 300)
    private String address;

    // Department FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 100)
    private String designation;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    // Self-referential manager relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    // Subordinates (team members)
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Employee> subordinates = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 20)
    private EmploymentType employmentType;

    @Column(precision = 12, scale = 2)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    // 1:1 link to auth user
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // Convenience method
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
