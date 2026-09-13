package com.erp.dto.response;

import com.erp.enums.ApprovalAction;
import com.erp.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalResponseDto {
    private Long id;
    private Long leaveRequestId;
    private Long approverId;
    private String approverName;
    private Role approverRole;
    private ApprovalAction action;
    private String comments;
    private LocalDateTime approvedAt;
}
