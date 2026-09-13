package com.erp.service;

import com.erp.dto.response.AttendanceResponseDto;
import com.erp.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceResponseDto checkIn(String notes);
    AttendanceResponseDto checkOut(String notes);
    AttendanceResponseDto getTodayAttendance();
    List<AttendanceResponseDto> getMyAttendance(Integer month, Integer year);
    PaginatedResponse<AttendanceResponseDto> getMyAttendancePaginated(Pageable pageable);
    List<AttendanceResponseDto> getEmployeeAttendance(Long employeeId, Integer month, Integer year);
    List<AttendanceResponseDto> getTeamAttendance(LocalDate date);
}
