package com.erp.service;

import com.erp.dto.request.LeaveApprovalRequestDto;
import com.erp.dto.response.LeaveRequestResponseDto;
import com.erp.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface LeaveApprovalService {
    PaginatedResponse<LeaveRequestResponseDto> getPendingApprovals(Pageable pageable);
    LeaveRequestResponseDto approveLeave(Long leaveRequestId, LeaveApprovalRequestDto dto);
    LeaveRequestResponseDto rejectLeave(Long leaveRequestId, LeaveApprovalRequestDto dto);
}
