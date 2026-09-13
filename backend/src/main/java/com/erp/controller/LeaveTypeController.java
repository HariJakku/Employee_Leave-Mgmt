package com.erp.controller;

import com.erp.dto.request.LeaveTypeRequestDto;
import com.erp.dto.response.LeaveTypeResponseDto;
import com.erp.service.LeaveTypeService;
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
@RequestMapping("/api/leave-types")
@RequiredArgsConstructor
@Tag(name = "Leave Types", description = "Endpoints for configuring organizational leave types and quotas")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping
    @Operation(summary = "Get all leave types")
    public ResponseEntity<List<LeaveTypeResponseDto>> getAllLeaveTypes(
        @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        return ResponseEntity.ok(activeOnly ? leaveTypeService.getActiveLeaveTypes() : leaveTypeService.getAllLeaveTypes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave type by ID")
    public ResponseEntity<LeaveTypeResponseDto> getLeaveTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveTypeService.getLeaveTypeById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create leave type", description = "Creates a new leave category and allocates balance to employees")
    public ResponseEntity<LeaveTypeResponseDto> createLeaveType(@Valid @RequestBody LeaveTypeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveTypeService.createLeaveType(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update leave type")
    public ResponseEntity<LeaveTypeResponseDto> updateLeaveType(
        @PathVariable Long id,
        @Valid @RequestBody LeaveTypeRequestDto dto
    ) {
        return ResponseEntity.ok(leaveTypeService.updateLeaveType(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate leave type")
    public ResponseEntity<Map<String, String>> deleteLeaveType(@PathVariable Long id) {
        leaveTypeService.deleteLeaveType(id);
        return ResponseEntity.ok(Map.of("message", "Leave type deactivated successfully"));
    }
}
