package com.erp.service.impl;

import com.erp.dto.request.DepartmentRequestDto;
import com.erp.dto.response.DepartmentResponseDto;
import com.erp.dto.response.EmployeeResponseDto;
import com.erp.entity.Department;
import com.erp.entity.Employee;
import com.erp.exception.BadRequestException;
import com.erp.exception.ResourceNotFoundException;
import com.erp.repository.DepartmentRepository;
import com.erp.repository.EmployeeRepository;
import com.erp.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponseDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDto getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return mapToResponseDto(dept);
    }

    @Override
    @Transactional
    public DepartmentResponseDto createDepartment(DepartmentRequestDto dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Department with name '" + dto.getName() + "' already exists");
        }

        if (dto.getHeadEmployeeId() != null && !employeeRepository.existsById(dto.getHeadEmployeeId())) {
            throw new ResourceNotFoundException("Employee", "id", dto.getHeadEmployeeId());
        }

        Department department = Department.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .headEmployeeId(dto.getHeadEmployeeId())
            .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
            .build();

        Department saved = departmentRepository.save(department);
        log.info("Created department: {}", saved.getName());
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto dto) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        if (departmentRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new BadRequestException("Department name '" + dto.getName() + "' is already in use");
        }

        if (dto.getHeadEmployeeId() != null && !employeeRepository.existsById(dto.getHeadEmployeeId())) {
            throw new ResourceNotFoundException("Employee", "id", dto.getHeadEmployeeId());
        }

        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setHeadEmployeeId(dto.getHeadEmployeeId());
        if (dto.getIsActive() != null) {
            department.setIsActive(dto.getIsActive());
        }

        Department updated = departmentRepository.save(department);
        log.info("Updated department ID: {}", id);
        return mapToResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        long employeeCount = departmentRepository.countEmployeesByDepartmentId(id);
        if (employeeCount > 0) {
            // Cannot hard-delete if employees assigned; soft delete instead
            department.setIsActive(false);
            departmentRepository.save(department);
            log.info("Department ID {} has {} active employees. Soft-deactivated instead.", id, employeeCount);
        } else {
            departmentRepository.delete(department);
            log.info("Hard-deleted department ID: {}", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getDepartmentEmployees(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department", "id", id);
        }

        return employeeRepository.findByDepartmentId(id).stream()
            .map(emp -> EmployeeResponseDto.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .fullName(emp.getFullName())
                .email(emp.getEmail())
                .phone(emp.getPhone())
                .designation(emp.getDesignation())
                .departmentId(id)
                .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null)
                .joiningDate(emp.getJoiningDate())
                .status(emp.getStatus())
                .build())
            .collect(Collectors.toList());
    }

    private DepartmentResponseDto mapToResponseDto(Department dept) {
        String headName = null;
        if (dept.getHeadEmployeeId() != null) {
            Optional<Employee> head = employeeRepository.findById(dept.getHeadEmployeeId());
            headName = head.map(Employee::getFullName).orElse(null);
        }

        long count = departmentRepository.countEmployeesByDepartmentId(dept.getId());

        return DepartmentResponseDto.builder()
            .id(dept.getId())
            .name(dept.getName())
            .description(dept.getDescription())
            .headEmployeeId(dept.getHeadEmployeeId())
            .headEmployeeName(headName)
            .employeeCount(count)
            .isActive(dept.getIsActive())
            .createdAt(dept.getCreatedAt())
            .updatedAt(dept.getUpdatedAt())
            .build();
    }
}
