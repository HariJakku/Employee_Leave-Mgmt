package com.erp.service;

import com.erp.dto.request.LeaveApplicationRequestDto;
import com.erp.dto.response.LeaveRequestResponseDto;
import com.erp.entity.*;
import com.erp.enums.LeaveStatus;
import com.erp.enums.Role;
import com.erp.exception.InsufficientLeaveBalanceException;
import com.erp.exception.OverlappingLeaveException;
import com.erp.repository.*;
import com.erp.security.SecurityUtils;
import com.erp.service.impl.LeaveRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private LeaveTypeRepository leaveTypeRepository;
    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private LeaveRequestServiceImpl leaveRequestService;

    private User testUser;
    private Employee testEmployee;
    private LeaveType testLeaveType;
    private LeaveBalance testBalance;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("john@erp.com").role(Role.EMPLOYEE).build();
        testEmployee = Employee.builder().id(10L).firstName("John").lastName("Doe").user(testUser).build();
        testUser.setEmployee(testEmployee);

        testLeaveType = LeaveType.builder()
            .id(2L)
            .name("Casual Leave")
            .maxDaysPerYear(12)
            .isActive(true)
            .build();

        testBalance = LeaveBalance.builder()
            .id(100L)
            .employee(testEmployee)
            .leaveType(testLeaveType)
            .year(LocalDate.now().getYear())
            .allocatedDays(BigDecimal.valueOf(12))
            .usedDays(BigDecimal.ZERO)
            .pendingDays(BigDecimal.ZERO)
            .build();
    }

    @Test
    @DisplayName("Apply Leave: Should succeed when dates do not overlap and balance is sufficient")
    void testApplyLeave_Success() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(7); // 3 days

        LeaveApplicationRequestDto dto = LeaveApplicationRequestDto.builder()
            .leaveTypeId(2L)
            .startDate(start)
            .endDate(end)
            .reason("Family event")
            .build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(leaveRequestRepository.findOverlappingLeaves(10L, start, end)).thenReturn(List.of());
        when(leaveTypeRepository.findById(2L)).thenReturn(Optional.of(testLeaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(10L, 2L, start.getYear()))
            .thenReturn(Optional.of(testBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> {
            LeaveRequest lr = i.getArgument(0);
            lr.setId(50L);
            return lr;
        });

        LeaveRequestResponseDto response = leaveRequestService.applyLeave(dto);

        assertNotNull(response);
        assertEquals(LeaveStatus.PENDING, response.getStatus());
        assertEquals(BigDecimal.valueOf(3), response.getTotalDays());
        // Verify pending days incremented
        assertEquals(BigDecimal.valueOf(3), testBalance.getPendingDays());
    }

    @Test
    @DisplayName("Apply Leave: Should throw OverlappingLeaveException when dates conflict")
    void testApplyLeave_OverlappingDates() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(7);

        LeaveApplicationRequestDto dto = LeaveApplicationRequestDto.builder()
            .leaveTypeId(2L)
            .startDate(start)
            .endDate(end)
            .reason("Holiday")
            .build();

        LeaveRequest existingLeave = LeaveRequest.builder()
            .id(99L)
            .startDate(start)
            .endDate(end)
            .build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(leaveRequestRepository.findOverlappingLeaves(10L, start, end))
            .thenReturn(List.of(existingLeave));

        assertThrows(OverlappingLeaveException.class, () -> leaveRequestService.applyLeave(dto));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Apply Leave: Should throw InsufficientLeaveBalanceException when days exceed remaining quota")
    void testApplyLeave_InsufficientBalance() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(20); // 16 days > 12 days quota

        LeaveApplicationRequestDto dto = LeaveApplicationRequestDto.builder()
            .leaveTypeId(2L)
            .startDate(start)
            .endDate(end)
            .reason("Extended trip")
            .build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(leaveRequestRepository.findOverlappingLeaves(10L, start, end)).thenReturn(List.of());
        when(leaveTypeRepository.findById(2L)).thenReturn(Optional.of(testLeaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(10L, 2L, start.getYear()))
            .thenReturn(Optional.of(testBalance));

        assertThrows(InsufficientLeaveBalanceException.class, () -> leaveRequestService.applyLeave(dto));
    }
}
