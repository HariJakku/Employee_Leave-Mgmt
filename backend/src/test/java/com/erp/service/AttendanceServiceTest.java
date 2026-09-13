package com.erp.service;

import com.erp.dto.response.AttendanceResponseDto;
import com.erp.entity.Attendance;
import com.erp.entity.Employee;
import com.erp.entity.User;
import com.erp.enums.AttendanceStatus;
import com.erp.exception.BadRequestException;
import com.erp.repository.AttendanceRepository;
import com.erp.security.SecurityUtils;
import com.erp.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private User testUser;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("worker@erp.com").build();
        testEmployee = Employee.builder().id(10L).firstName("Sam").lastName("Worker").user(testUser).build();
        testUser.setEmployee(testEmployee);
    }

    @Test
    @DisplayName("Check In: Should record attendance when not already checked in today")
    void testCheckIn_Success() {
        LocalDate today = LocalDate.now();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(attendanceRepository.existsByEmployeeIdAndDate(10L, today)).thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> {
            Attendance a = i.getArgument(0);
            a.setId(1L);
            return a;
        });

        AttendanceResponseDto res = attendanceService.checkIn("Office check-in");

        assertNotNull(res);
        assertEquals(today, res.getDate());
        assertEquals(AttendanceStatus.PRESENT, res.getStatus());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Check In: Should throw BadRequestException when already checked in today")
    void testCheckIn_AlreadyCheckedIn() {
        LocalDate today = LocalDate.now();
        Attendance existing = Attendance.builder()
            .id(1L)
            .checkIn(LocalDateTime.now().minusHours(2))
            .build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(attendanceRepository.existsByEmployeeIdAndDate(10L, today)).thenReturn(true);
        when(attendanceRepository.findByEmployeeIdAndDate(10L, today)).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () -> attendanceService.checkIn("Second check-in"));
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Check Out: Should calculate working hours and complete today's log")
    void testCheckOut_Success() {
        LocalDate today = LocalDate.now();
        Attendance existing = Attendance.builder()
            .id(1L)
            .employee(testEmployee)
            .date(today)
            .checkIn(LocalDateTime.now().minusHours(8))
            .status(AttendanceStatus.PRESENT)
            .build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(attendanceRepository.findByEmployeeIdAndDate(10L, today)).thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        AttendanceResponseDto res = attendanceService.checkOut("Day end");

        assertNotNull(res);
        assertNotNull(res.getCheckOut());
        assertNotNull(res.getWorkingHours());
        assertTrue(res.getWorkingHours().doubleValue() >= 7.9);
    }
}
