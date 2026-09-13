package com.erp.service;

import com.erp.dto.request.LeaveApprovalRequestDto;
import com.erp.dto.response.LeaveRequestResponseDto;
import com.erp.entity.*;
import com.erp.enums.LeaveStatus;
import com.erp.enums.Role;
import com.erp.exception.UnauthorizedException;
import com.erp.repository.*;
import com.erp.security.SecurityUtils;
import com.erp.service.impl.LeaveApprovalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveApprovalServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private LeaveApprovalRepository leaveApprovalRepository;
    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private LeaveApprovalServiceImpl leaveApprovalService;

    private User managerUser;
    private Employee managerEmp;
    private User employeeUser;
    private Employee applicant;
    private LeaveRequest leaveRequest;
    private LeaveBalance balance;

    @BeforeEach
    void setUp() {
        managerUser = User.builder().id(2L).email("manager@erp.com").role(Role.MANAGER).build();
        managerEmp = Employee.builder().id(20L).firstName("Bob").lastName("Manager").user(managerUser).build();
        managerUser.setEmployee(managerEmp);

        employeeUser = User.builder().id(3L).email("emp@erp.com").role(Role.EMPLOYEE).build();
        applicant = Employee.builder().id(30L).firstName("John").lastName("Worker")
            .manager(managerEmp).user(employeeUser).build();
        employeeUser.setEmployee(applicant);

        LeaveType lt = LeaveType.builder().id(1L).name("Sick Leave").build();

        leaveRequest = LeaveRequest.builder()
            .id(100L)
            .employee(applicant)
            .leaveType(lt)
            .startDate(LocalDate.now().plusDays(2))
            .endDate(LocalDate.now().plusDays(3))
            .totalDays(BigDecimal.valueOf(2))
            .status(LeaveStatus.PENDING)
            .build();

        balance = LeaveBalance.builder()
            .allocatedDays(BigDecimal.valueOf(10))
            .usedDays(BigDecimal.ZERO)
            .pendingDays(BigDecimal.valueOf(2))
            .build();
    }

    @Test
    @DisplayName("Manager Approval: Should transition PENDING to MANAGER_APPROVED")
    void testManagerApprove_Success() {
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
        when(securityUtils.getCurrentUser()).thenReturn(managerUser);

        LeaveApprovalRequestDto dto = LeaveApprovalRequestDto.builder().comments("Approved").build();
        LeaveRequestResponseDto result = leaveApprovalService.approveLeave(100L, dto);

        assertEquals(LeaveStatus.MANAGER_APPROVED, result.getStatus());
        verify(leaveApprovalRepository, times(1)).save(any(LeaveApproval.class));
    }

    @Test
    @DisplayName("Self Approval: Should prevent employees from approving their own leaves")
    void testSelfApproval_Rejected() {
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
        // Current user is applicant themselves!
        when(securityUtils.getCurrentUser()).thenReturn(employeeUser);

        LeaveApprovalRequestDto dto = LeaveApprovalRequestDto.builder().build();
        assertThrows(UnauthorizedException.class, () -> leaveApprovalService.approveLeave(100L, dto));
    }

    @Test
    @DisplayName("Leave Rejection: Should transition to REJECTED and revert pending days")
    void testRejectLeave_RestoresPendingBalance() {
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
        when(securityUtils.getCurrentUser()).thenReturn(managerUser);
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(any(), any(), any()))
            .thenReturn(Optional.of(balance));

        LeaveApprovalRequestDto dto = LeaveApprovalRequestDto.builder()
            .comments("Critical deadline")
            .rejectionReason("Critical deadline")
            .build();

        LeaveRequestResponseDto result = leaveApprovalService.rejectLeave(100L, dto);

        assertEquals(LeaveStatus.REJECTED, result.getStatus());
        // Verify pending days reverted to 0
        assertEquals(BigDecimal.ZERO, balance.getPendingDays());
        // Verify used days untouched (remains 0)
        assertEquals(BigDecimal.ZERO, balance.getUsedDays());
    }
}
