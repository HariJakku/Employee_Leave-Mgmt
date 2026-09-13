package com.erp.controller;

import com.erp.dto.request.DepartmentRequestDto;
import com.erp.dto.response.DepartmentResponseDto;
import com.erp.dto.response.EmployeeResponseDto;
import com.erp.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "Endpoints for managing company departments and employees by department")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(summary = "Get all departments", description = "Returns all departments with active status, head, and member count")
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<DepartmentResponseDto> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create department", description = "Creates a new organizational department (Admin/HR only)")
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update department", description = "Updates department name, description, or head (Admin/HR only)")
    public ResponseEntity<DepartmentResponseDto> updateDepartment(
        @PathVariable Long id,
        @Valid @RequestBody DepartmentRequestDto dto
    ) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete department", description = "Deletes or deactivates department if employees are assigned (Admin only)")
    public ResponseEntity<Map<String, String>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(Map.of("message", "Department deleted/deactivated successfully"));
    }

    @GetMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get department employees", description = "Returns all employees belonging to this department")
    public ResponseEntity<List<EmployeeResponseDto>> getDepartmentEmployees(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentEmployees(id));
    }
}
