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
public class AdminDashboardDto {
    private long totalEmployees;
    private long activeEmployees;
    private long totalDepartments;

    private long pendingLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;

    private long todayPresent;
    private long todayOnLeave;
    private long todayAbsent;

    private List<Map<String, Object>> departmentStats; // { name, count }
    private List<Map<String, Object>> leaveTypeStats;   // { name, count }
    private List<LeaveRequestResponseDto> recentLeaves;
    private List<EmployeeResponseDto> employeesOnLeaveToday;
}
