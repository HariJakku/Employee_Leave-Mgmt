package com.erp.service.impl;

import com.erp.dto.response.AttendanceResponseDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.entity.Attendance;
import com.erp.entity.Employee;
import com.erp.entity.User;
import com.erp.enums.AttendanceStatus;
import com.erp.exception.BadRequestException;
import com.erp.exception.ResourceNotFoundException;
import com.erp.repository.AttendanceRepository;
import com.erp.repository.EmployeeRepository;
import com.erp.security.SecurityUtils;
import com.erp.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public AttendanceResponseDto checkIn(String notes) {
        User currentUser = securityUtils.getCurrentUser();
        Employee employee = currentUser.getEmployee();
        if (employee == null) {
            throw new BadRequestException("Current user account is not associated with an employee profile.");
        }

        LocalDate today = LocalDate.now();

        // Check if already checked in today
        if (attendanceRepository.existsByEmployeeIdAndDate(employee.getId(), today)) {
            Attendance existing = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today).get();
            throw new BadRequestException("Already checked in today at " + existing.getCheckIn().toLocalTime().toString().substring(0, 5));
        }

        Attendance attendance = Attendance.builder()
            .employee(employee)
            .date(today)
            .checkIn(LocalDateTime.now())
            .status(AttendanceStatus.PRESENT)
            .notes(notes)
            .build();

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Employee {} checked in at {}", employee.getEmployeeCode(), saved.getCheckIn());
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public AttendanceResponseDto checkOut(String notes) {
        User currentUser = securityUtils.getCurrentUser();
        Employee employee = currentUser.getEmployee();
        if (employee == null) {
            throw new BadRequestException("Current user account is not associated with an employee profile.");
        }

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
            .orElseThrow(() -> new BadRequestException("You have not checked in today. Please check in first."));

        if (attendance.getCheckOut() != null) {
            throw new BadRequestException("Already checked out today at " + attendance.getCheckOut().toLocalTime().toString().substring(0, 5));
        }

        LocalDateTime checkOutTime = LocalDateTime.now();
        attendance.setCheckOut(checkOutTime);

        // Calculate working hours
        Duration duration = Duration.between(attendance.getCheckIn(), checkOutTime);
        double hours = duration.toMinutes() / 60.0;
        BigDecimal workingHours = BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
        attendance.setWorkingHours(workingHours);

        // Half day threshold
        if (hours < 4.0) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        } else {
            attendance.setStatus(AttendanceStatus.PRESENT);
        }

        if (notes != null && !notes.isBlank()) {
            attendance.setNotes((attendance.getNotes() != null ? attendance.getNotes() + " | " : "") + notes);
        }

        Attendance updated = attendanceRepository.save(attendance);
        log.info("Employee {} checked out. Working hours: {}", employee.getEmployeeCode(), workingHours);
        return mapToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponseDto getTodayAttendance() {
        User currentUser = securityUtils.getCurrentUser();
        Employee employee = currentUser.getEmployee();
        if (employee == null) return null;

        LocalDate today = LocalDate.now();
        return attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
            .map(this::mapToDto)
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getMyAttendance(Integer month, Integer year) {
        User currentUser = securityUtils.getCurrentUser();
        Employee employee = currentUser.getEmployee();
        if (employee == null) return List.of();

        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();

        YearMonth ym = YearMonth.of(y, m);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(employee.getId(), start, end)
            .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<AttendanceResponseDto> getMyAttendancePaginated(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Employee employee = currentUser.getEmployee();
        if (employee == null) {
            return PaginatedResponse.fromPage(Page.empty(pageable));
        }

        Page<Attendance> page = attendanceRepository.findByEmployeeIdOrderByDateDesc(employee.getId(), pageable);
        return PaginatedResponse.fromPage(page.map(this::mapToDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getEmployeeAttendance(Long employeeId, Integer month, Integer year) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();

        YearMonth ym = YearMonth.of(y, m);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(employeeId, start, end)
            .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getTeamAttendance(LocalDate date) {
        User currentUser = securityUtils.getCurrentUser();
        Employee manager = currentUser.getEmployee();
        if (manager == null) return List.of();

        LocalDate targetDate = date != null ? date : LocalDate.now();
        return attendanceRepository.findTeamAttendanceByDate(manager.getId(), targetDate)
            .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private AttendanceResponseDto mapToDto(Attendance a) {
        return AttendanceResponseDto.builder()
            .id(a.getId())
            .employeeId(a.getEmployee().getId())
            .employeeName(a.getEmployee().getFullName())
            .employeeCode(a.getEmployee().getEmployeeCode())
            .date(a.getDate())
            .checkIn(a.getCheckIn())
            .checkOut(a.getCheckOut())
            .workingHours(a.getWorkingHours())
            .status(a.getStatus())
            .notes(a.getNotes())
            .build();
    }
}
