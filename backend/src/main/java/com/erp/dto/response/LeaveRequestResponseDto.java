package com.erp.dto.response;

import com.erp.enums.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String departmentName;

    private Long leaveTypeId;
    private String leaveTypeName;

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private String reason;
    private String documentUrl;
    private LeaveStatus status;
    private String rejectionReason;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    private List<LeaveApprovalResponseDto> approvals;
}
