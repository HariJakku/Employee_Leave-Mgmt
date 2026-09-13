package com.erp.controller;

import com.erp.dto.request.EmployeeCreateDto;
import com.erp.dto.request.EmployeeUpdateDto;
import com.erp.dto.response.EmployeeResponseDto;
import com.erp.dto.response.LeaveBalanceDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.enums.EmployeeStatus;
import com.erp.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Endpoints for employee CRUD, search, filter, and pagination")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get all employees with pagination and filters")
    public ResponseEntity<PaginatedResponse<EmployeeResponseDto>> getEmployees(
        @Parameter(description = "Search by name, email, or code")
        @RequestParam(required = false) String search,
        @Parameter(description = "Filter by department ID")
        @RequestParam(required = false) Long department,
        @Parameter(description = "Filter by status: ACTIVE, INACTIVE, TERMINATED")
        @RequestParam(required = false) EmployeeStatus status,
        @Parameter(description = "Filter by designation")
        @RequestParam(required = false) String designation,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "Sort field")
        @RequestParam(defaultValue = "id") String sortBy,
        @Parameter(description = "Sort direction (asc/desc)")
        @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(employeeService.getEmployees(search, department, status, designation, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER') or @securityUtils.currentUser.employee?.id == #id")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create a new employee")
    public ResponseEntity<EmployeeResponseDto> createEmployee(@Valid @RequestBody EmployeeCreateDto dto) {
        EmployeeResponseDto created = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update an existing employee")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
        @PathVariable Long id,
        @Valid @RequestBody EmployeeUpdateDto dto
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate/Terminate an employee")
    public ResponseEntity<Map<String, String>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(Map.of("message", "Employee deactivated successfully"));
    }

    @GetMapping("/{id}/leave-balances")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER') or @securityUtils.currentUser.employee?.id == #id")
    @Operation(summary = "Get leave balances for an employee")
    public ResponseEntity<List<LeaveBalanceDto>> getEmployeeLeaveBalances(
        @PathVariable Long id,
        @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(employeeService.getEmployeeLeaveBalances(id, year));
    }

    @GetMapping("/manager/{managerId}/team")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get all direct reports for a manager")
    public ResponseEntity<List<EmployeeResponseDto>> getSubordinates(@PathVariable Long managerId) {
        return ResponseEntity.ok(employeeService.getSubordinates(managerId));
    }
}
