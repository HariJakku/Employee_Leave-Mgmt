package com.erp.entity;

import com.erp.enums.Role;
import jakarta.persistence.*;
import lombok.*;

/**
 * System user — linked 1:1 to an Employee record.
 * Stores login credentials; never expose password in DTOs.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Note: Employee relationship is owned by Employee.user (inverse side here)
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Employee employee;
}
