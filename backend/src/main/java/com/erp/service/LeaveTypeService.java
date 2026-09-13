package com.erp.service;

import com.erp.dto.request.LeaveTypeRequestDto;
import com.erp.dto.response.LeaveTypeResponseDto;

import java.util.List;

public interface LeaveTypeService {
    List<LeaveTypeResponseDto> getAllLeaveTypes();
    List<LeaveTypeResponseDto> getActiveLeaveTypes();
    LeaveTypeResponseDto getLeaveTypeById(Long id);
    LeaveTypeResponseDto createLeaveType(LeaveTypeRequestDto dto);
    LeaveTypeResponseDto updateLeaveType(Long id, LeaveTypeRequestDto dto);
    void deleteLeaveType(Long id);
}
