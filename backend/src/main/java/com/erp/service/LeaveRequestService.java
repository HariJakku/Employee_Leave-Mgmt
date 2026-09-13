package com.erp.service;

import com.erp.dto.request.LeaveApplicationRequestDto;
import com.erp.dto.response.LeaveBalanceDto;
import com.erp.dto.response.LeaveRequestResponseDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.enums.LeaveStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequestResponseDto applyLeave(LeaveApplicationRequestDto dto);
    PaginatedResponse<LeaveRequestResponseDto> getMyLeaves(Pageable pageable);
    PaginatedResponse<LeaveRequestResponseDto> getAllLeaves(Long employeeId, LeaveStatus status, Pageable pageable);
    LeaveRequestResponseDto getLeaveById(Long id);
    LeaveRequestResponseDto cancelLeave(Long id);
    List<LeaveBalanceDto> getMyLeaveBalances();
}
