package com.erp.controller;

import com.erp.dto.response.AttendanceResponseDto;
import com.erp.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "Endpoints for employee check-in, check-out, working hours tracking, and team attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    @Operation(summary = "Check in for today", description = "Records check-in time; fails if already checked in today")
    public ResponseEntity<AttendanceResponseDto> checkIn(@RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(attendanceService.checkIn(notes));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Check out for today", description = "Records check-out time, computes total hours worked, and updates status")
    public ResponseEntity<AttendanceResponseDto> checkOut(@RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(attendanceService.checkOut(notes));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's check-in status for the logged-in employee")
    public ResponseEntity<AttendanceResponseDto> getTodayAttendance() {
        return ResponseEntity.ok(attendanceService.getTodayAttendance());
    }

    @GetMapping("/my")
    @Operation(summary = "Get attendance history for the logged-in employee by month/year")
    public ResponseEntity<List<AttendanceResponseDto>> getMyAttendance(
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(month, year));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get attendance records for a specific employee (Admin, HR, Manager)")
    public ResponseEntity<List<AttendanceResponseDto>> getEmployeeAttendance(
        @PathVariable Long employeeId,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendance(employeeId, month, year));
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get attendance for manager's direct reports on a specific date")
    public ResponseEntity<List<AttendanceResponseDto>> getTeamAttendance(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(attendanceService.getTeamAttendance(date));
    }
}
