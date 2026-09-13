package com.erp.controller;

import com.erp.dto.request.LeaveApprovalRequestDto;
import com.erp.dto.response.LeaveRequestResponseDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.service.LeaveApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Approvals", description = "Endpoints for multi-tier leave approval workflow (Manager -> HR/Admin)")
public class LeaveApprovalController {

    private final LeaveApprovalService leaveApprovalService;

    @GetMapping("/approvals/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get pending leave approvals", description = "Managers see team's pending requests; HR/Admin see requests awaiting final approval")
    public ResponseEntity<PaginatedResponse<LeaveRequestResponseDto>> getPendingApprovals(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").ascending());
        return ResponseEntity.ok(leaveApprovalService.getPendingApprovals(pageable));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Approve leave request", description = "Advances leave from PENDING -> MANAGER_APPROVED or MANAGER_APPROVED -> APPROVED")
    public ResponseEntity<LeaveRequestResponseDto> approveLeave(
        @PathVariable Long id,
        @RequestBody(required = false) LeaveApprovalRequestDto dto
    ) {
        if (dto == null) dto = new LeaveApprovalRequestDto();
        return ResponseEntity.ok(leaveApprovalService.approveLeave(id, dto));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Reject leave request", description = "Rejects leave request and restores pending balance")
    public ResponseEntity<LeaveRequestResponseDto> rejectLeave(
        @PathVariable Long id,
        @RequestBody(required = false) LeaveApprovalRequestDto dto
    ) {
        if (dto == null) dto = new LeaveApprovalRequestDto();
        return ResponseEntity.ok(leaveApprovalService.rejectLeave(id, dto));
    }
}
