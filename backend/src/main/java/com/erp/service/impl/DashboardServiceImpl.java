package com.erp.service.impl;

import com.erp.dto.response.*;
import com.erp.entity.*;
import com.erp.enums.AttendanceStatus;
import com.erp.enums.EmployeeStatus;
import com.erp.enums.LeaveStatus;
import com.erp.exception.BadRequestException;
import com.erp.repository.*;
import com.erp.security.SecurityUtils;
import com.erp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboard() {
        LocalDate today = LocalDate.now();

        long totalEmp = employeeRepository.count();
        long activeEmp = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        long totalDepts = departmentRepository.count();

        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.PENDING)
            + leaveRequestRepository.countByStatus(LeaveStatus.MANAGER_APPROVED);
        long approvedLeaves = leaveRequestRepository.countByStatus(LeaveStatus.APPROVED);
        long rejectedLeaves = leaveRequestRepository.countByStatus(LeaveStatus.REJECTED);

        long todayPresent = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.PRESENT);
        List<LeaveRequest> currentlyOnLeave = leaveRequestRepository.findCurrentlyOnLeave(today);
        long todayOnLeave = currentlyOnLeave.size();
        long todayAbsent = Math.max(0, activeEmp - todayPresent - todayOnLeave);

        // Department breakdown
        List<Department> departments = departmentRepository.findAll();
        List<Map<String, Object>> deptStats = new ArrayList<>();
        for (Department d : departments) {
            long count = departmentRepository.countEmployeesByDepartmentId(d.getId());
            deptStats.add(Map.of("name", d.getName(), "count", count));
        }

        // Leave type breakdown
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();
        List<Map<String, Object>> ltStats = new ArrayList<>();
        for (LeaveType lt : leaveTypes) {
            ltStats.add(Map.of("name", lt.getName(), "count", lt.getMaxDaysPerYear()));
        }

        // Recent 5 leave requests
        var recentLeavesPage = leaveRequestRepository.findAll(
            PageRequest.of(0, 5, Sort.by("appliedAt").descending())
        );
        List<LeaveRequestResponseDto> recentLeaves = recentLeavesPage.getContent().stream()
            .map(this::mapLeaveToDto)
            .collect(Collectors.toList());

        // Employees currently on leave today
        List<EmployeeResponseDto> onLeaveToday = currentlyOnLeave.stream()
            .map(l -> mapEmployeeToDto(l.getEmployee()))
            .collect(Collectors.toList());

        return AdminDashboardDto.builder()
            .totalEmployees(totalEmp)
            .activeEmployees(activeEmp)
            .totalDepartments(totalDepts)
            .pendingLeaves(pendingLeaves)
            .approvedLeaves(approvedLeaves)
            .rejectedLeaves(rejectedLeaves)
            .todayPresent(todayPresent)
            .todayOnLeave(todayOnLeave)
            .todayAbsent(todayAbsent)
            .departmentStats(deptStats)
            .leaveTypeStats(ltStats)
            .recentLeaves(recentLeaves)
            .employeesOnLeaveToday(onLeaveToday)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HRDashboardDto getHRDashboard() {
        LocalDate today = LocalDate.now();
        long totalEmp = employeeRepository.count();
        long activeEmp = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);

        long pendingApprovals = leaveRequestRepository.countByStatus(LeaveStatus.MANAGER_APPROVED);
        long todayPresent = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.PRESENT);
        List<LeaveRequest> onLeave = leaveRequestRepository.findCurrentlyOnLeave(today);

        List<Department> departments = departmentRepository.findAll();
        List<Map<String, Object>> deptStats = departments.stream()
            .map(d -> Map.<String, Object>of(
                "name", d.getName(),
                "count", departmentRepository.countEmployeesByDepartmentId(d.getId())
            ))
            .collect(Collectors.toList());

        var recentPage = leaveRequestRepository.findAll(
            PageRequest.of(0, 5, Sort.by("appliedAt").descending())
        );

        return HRDashboardDto.builder()
            .totalEmployees(totalEmp)
            .activeEmployees(activeEmp)
            .newJoinersThisMonth(3) // Sample derived metric
            .pendingHRApprovals(pendingApprovals)
            .todayPresent(todayPresent)
            .todayOnLeave(onLeave.size())
            .departmentStats(deptStats)
            .recentLeaveRequests(recentPage.getContent().stream().map(this::mapLeaveToDto).collect(Collectors.toList()))
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ManagerDashboardDto getManagerDashboard() {
        User currentUser = securityUtils.getCurrentUser();
        Employee manager = currentUser.getEmployee();
        if (manager == null) {
            throw new BadRequestException("Current account is not associated with an employee profile.");
        }

        List<Employee> team = employeeRepository.findByManagerId(manager.getId());
        long teamSize = team.size();

        var pendingPage = leaveRequestRepository.findPendingForManager(
            manager.getId(), PageRequest.of(0, 10, Sort.by("appliedAt").ascending())
        );

        LocalDate today = LocalDate.now();
        List<Attendance> teamAtt = attendanceRepository.findTeamAttendanceByDate(manager.getId(), today);
        long presentCount = teamAtt.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();

        List<LeaveRequest> onLeave = leaveRequestRepository.findCurrentlyOnLeave(today);
        Set<Long> teamIds = team.stream().map(Employee::getId).collect(Collectors.toSet());
        long teamOnLeave = onLeave.stream().filter(l -> teamIds.contains(l.getEmployee().getId())).count();

        List<AttendanceResponseDto> attDtos = teamAtt.stream().map(a -> AttendanceResponseDto.builder()
            .id(a.getId())
            .employeeId(a.getEmployee().getId())
            .employeeName(a.getEmployee().getFullName())
            .employeeCode(a.getEmployee().getEmployeeCode())
            .date(a.getDate())
            .checkIn(a.getCheckIn())
            .checkOut(a.getCheckOut())
            .workingHours(a.getWorkingHours())
            .status(a.getStatus())
            .build()).collect(Collectors.toList());

        return ManagerDashboardDto.builder()
            .teamSize(teamSize)
            .teamMembersOnLeaveToday(teamOnLeave)
            .pendingApprovalsCount(pendingPage.getTotalElements())
            .teamPresentToday(presentCount)
            .pendingApprovals(pendingPage.getContent().stream().map(this::mapLeaveToDto).collect(Collectors.toList()))
            .todayTeamAttendance(attDtos)
            .teamMembers(team.stream().map(this::mapEmployeeToDto).collect(Collectors.toList()))
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDashboardDto getEmployeeDashboard() {
        User currentUser = securityUtils.getCurrentUser();
        Employee employee = currentUser.getEmployee();
        if (employee == null) {
            throw new BadRequestException("Current account is not associated with an employee profile.");
        }

        int year = LocalDate.now().getYear();
        List<LeaveBalanceDto> balances = leaveBalanceRepository
            .findActiveBalancesByEmployeeAndYear(employee.getId(), year)
            .stream().map(LeaveBalanceDto::fromEntity).collect(Collectors.toList());

        Attendance todayAtt = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), LocalDate.now()).orElse(null);
        AttendanceResponseDto attDto = todayAtt != null ? AttendanceResponseDto.builder()
            .id(todayAtt.getId())
            .date(todayAtt.getDate())
            .checkIn(todayAtt.getCheckIn())
            .checkOut(todayAtt.getCheckOut())
            .workingHours(todayAtt.getWorkingHours())
            .status(todayAtt.getStatus())
            .build() : null;

        var leavesPage = leaveRequestRepository.findByEmployeeId(
            employee.getId(), PageRequest.of(0, 5, Sort.by("appliedAt").descending())
        );

        var notifPage = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
            currentUser.getId(), PageRequest.of(0, 5)
        );

        List<NotificationResponseDto> notifs = notifPage.getContent().stream()
            .map(n -> NotificationResponseDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build()).collect(Collectors.toList());

        return EmployeeDashboardDto.builder()
            .profile(mapEmployeeToDto(employee))
            .leaveBalances(balances)
            .todayAttendance(attDto)
            .recentLeaves(leavesPage.getContent().stream().map(this::mapLeaveToDto).collect(Collectors.toList()))
            .recentNotifications(notifs)
            .build();
    }

    private LeaveRequestResponseDto mapLeaveToDto(LeaveRequest lr) {
        return LeaveRequestResponseDto.builder()
            .id(lr.getId())
            .employeeId(lr.getEmployee().getId())
            .employeeName(lr.getEmployee().getFullName())
            .employeeCode(lr.getEmployee().getEmployeeCode())
            .departmentName(lr.getEmployee().getDepartment() != null ? lr.getEmployee().getDepartment().getName() : null)
            .leaveTypeId(lr.getLeaveType().getId())
            .leaveTypeName(lr.getLeaveType().getName())
            .startDate(lr.getStartDate())
            .endDate(lr.getEndDate())
            .totalDays(lr.getTotalDays())
            .reason(lr.getReason())
            .status(lr.getStatus())
            .appliedAt(lr.getAppliedAt())
            .build();
    }

    private EmployeeResponseDto mapEmployeeToDto(Employee emp) {
        return EmployeeResponseDto.builder()
            .id(emp.getId())
            .employeeCode(emp.getEmployeeCode())
            .firstName(emp.getFirstName())
            .lastName(emp.getLastName())
            .fullName(emp.getFullName())
            .email(emp.getEmail())
            .phone(emp.getPhone())
            .designation(emp.getDesignation())
            .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null)
            .status(emp.getStatus())
            .build();
    }
}
