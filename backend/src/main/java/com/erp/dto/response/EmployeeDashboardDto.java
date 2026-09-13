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
public class EmployeeDashboardDto {
    private EmployeeResponseDto profile;
    private List<LeaveBalanceDto> leaveBalances;
    private AttendanceResponseDto todayAttendance;
    private List<LeaveRequestResponseDto> recentLeaves;
    private List<NotificationResponseDto> recentNotifications;
}
