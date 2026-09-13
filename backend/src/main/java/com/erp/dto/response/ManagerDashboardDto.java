package com.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardDto {
    private long teamSize;
    private long teamMembersOnLeaveToday;
    private long pendingApprovalsCount;
    private long teamPresentToday;

    private List<LeaveRequestResponseDto> pendingApprovals;
    private List<AttendanceResponseDto> todayTeamAttendance;
    private List<EmployeeResponseDto> teamMembers;
}
