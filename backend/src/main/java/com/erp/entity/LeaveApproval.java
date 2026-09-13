package com.erp.entity;

import com.erp.enums.ApprovalAction;
import com.erp.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A single approval action taken on a leave request (manager stage or HR stage).
 * Multiple records can exist per leave request, one per approval stage.
 */
@Entity
@Table(
    name = "leave_approvals",
    indexes = {
        @Index(name = "idx_la_request", columnList = "leave_request_id"),
        @Index(name = "idx_la_approver", columnList = "approver_id")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveApproval extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaveRequest leaveRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    /** Role of the approver at the time of approval for auditing purposes. */
    @Enumerated(EnumType.STRING)
    @Column(name = "approver_role", nullable = false, length = 20)
    private Role approverRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ApprovalAction action;

    @Column(length = 500)
    private String comments;

    @Column(name = "approved_at", nullable = false)
    @Builder.Default
    private LocalDateTime approvedAt = LocalDateTime.now();
}
