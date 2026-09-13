package com.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HRDashboardDto {
    private long totalEmployees;
    private long activeEmployees;
    private long newJoinersThisMonth;
    private long pendingHRApprovals;
    private long todayPresent;
    private long todayOnLeave;

    private List<Map<String, Object>> departmentStats;
    private List<LeaveRequestResponseDto> recentLeaveRequests;
}
