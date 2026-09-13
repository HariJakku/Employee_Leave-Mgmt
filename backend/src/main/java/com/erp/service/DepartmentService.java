package com.erp.service;

import com.erp.dto.request.DepartmentRequestDto;
import com.erp.dto.response.DepartmentResponseDto;
import com.erp.dto.response.EmployeeResponseDto;

import java.util.List;

public interface DepartmentService {
    List<DepartmentResponseDto> getAllDepartments();
    DepartmentResponseDto getDepartmentById(Long id);
    DepartmentResponseDto createDepartment(DepartmentRequestDto dto);
    DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto dto);
    void deleteDepartment(Long id);
    List<EmployeeResponseDto> getDepartmentEmployees(Long id);
}
