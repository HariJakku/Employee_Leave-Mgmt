package com.erp.controller;

import com.erp.dto.request.LeaveApplicationRequestDto;
import com.erp.dto.response.LeaveBalanceDto;
import com.erp.dto.response.LeaveRequestResponseDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.enums.LeaveStatus;
import com.erp.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Endpoints for applying for leave, viewing history, and cancelling requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    @Operation(summary = "Apply for leave", description = "Validates balance and overlapping dates, deducts pending days, and submits for approval")
    public ResponseEntity<LeaveRequestResponseDto> applyLeave(@Valid @RequestBody LeaveApplicationRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.applyLeave(dto));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current employee leave history")
    public ResponseEntity<PaginatedResponse<LeaveRequestResponseDto>> getMyLeaves(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        return ResponseEntity.ok(leaveRequestService.getMyLeaves(pageable));
    }

    @GetMapping("/my/balances")
    @Operation(summary = "Get current employee leave balances")
    public ResponseEntity<List<LeaveBalanceDto>> getMyLeaveBalances() {
        return ResponseEntity.ok(leaveRequestService.getMyLeaveBalances());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get all leaves with filters (Admin, HR, Manager)")
    public ResponseEntity<PaginatedResponse<LeaveRequestResponseDto>> getAllLeaves(
        @RequestParam(required = false) Long employeeId,
        @RequestParam(required = false) LeaveStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        return ResponseEntity.ok(leaveRequestService.getAllLeaves(employeeId, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave request details by ID")
    public ResponseEntity<LeaveRequestResponseDto> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getLeaveById(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a leave request", description = "Cancels a pending or approved leave and restores balance")
    public ResponseEntity<LeaveRequestResponseDto> cancelLeave(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.cancelLeave(id));
    }
}
