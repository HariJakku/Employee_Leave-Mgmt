package com.erp.service;

import com.erp.dto.request.EmployeeCreateDto;
import com.erp.dto.request.EmployeeUpdateDto;
import com.erp.dto.response.EmployeeResponseDto;
import com.erp.dto.response.LeaveBalanceDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.enums.EmployeeStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
    PaginatedResponse<EmployeeResponseDto> getEmployees(
        String search, Long departmentId, EmployeeStatus status, String designation, Pageable pageable
    );

    EmployeeResponseDto getEmployeeById(Long id);

    EmployeeResponseDto getEmployeeByEmail(String email);

    EmployeeResponseDto createEmployee(EmployeeCreateDto dto);

    EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateDto dto);

    void deleteEmployee(Long id);

    List<LeaveBalanceDto> getEmployeeLeaveBalances(Long employeeId, Integer year);

    List<EmployeeResponseDto> getSubordinates(Long managerId);
}
