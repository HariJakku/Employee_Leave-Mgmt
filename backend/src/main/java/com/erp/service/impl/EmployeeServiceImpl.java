package com.erp.service.impl;

import com.erp.dto.request.EmployeeCreateDto;
import com.erp.dto.request.EmployeeUpdateDto;
import com.erp.dto.response.EmployeeResponseDto;
import com.erp.dto.response.LeaveBalanceDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.entity.*;
import com.erp.enums.EmployeeStatus;
import com.erp.enums.Role;
import com.erp.exception.DuplicateEmailException;
import com.erp.exception.ResourceNotFoundException;
import com.erp.repository.*;
import com.erp.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<EmployeeResponseDto> getEmployees(
        String search, Long departmentId, EmployeeStatus status, String designation, Pageable pageable
    ) {
        Page<Employee> page = employeeRepository.searchEmployees(
            search, departmentId, status, designation, pageable
        );
        List<EmployeeResponseDto> dtos = page.getContent().stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());

        return PaginatedResponse.<EmployeeResponseDto>builder()
            .content(dtos)
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .isLast(page.isLast())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapToResponseDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));
        return mapToResponseDto(employee);
    }

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeCreateDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail()) || userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email " + dto.getEmail() + " is already registered.");
        }

        // 1. Create linked User
        String rawPassword = dto.getPassword() != null && !dto.getPassword().isBlank()
            ? dto.getPassword() : "Employee@123";

        User user = User.builder()
            .email(dto.getEmail())
            .passwordHash(passwordEncoder.encode(rawPassword))
            .role(dto.getRole() != null ? dto.getRole() : Role.EMPLOYEE)
            .isActive(dto.getStatus() == null || dto.getStatus() == EmployeeStatus.ACTIVE)
            .build();
        user = userRepository.save(user);

        // 2. Generate Employee Code
        int maxCode = employeeRepository.findMaxEmployeeCodeNumber();
        String employeeCode = String.format("EMP-%04d", maxCode + 1);

        // 3. Department & Manager
        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
        }

        Employee manager = null;
        if (dto.getManagerId() != null) {
            manager = employeeRepository.findById(dto.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", dto.getManagerId()));
        }

        // 4. Create Employee
        Employee employee = Employee.builder()
            .employeeCode(employeeCode)
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .dateOfBirth(dto.getDateOfBirth())
            .gender(dto.getGender())
            .address(dto.getAddress())
            .department(department)
            .designation(dto.getDesignation())
            .joiningDate(dto.getJoiningDate() != null ? dto.getJoiningDate() : LocalDate.now())
            .manager(manager)
            .employmentType(dto.getEmploymentType())
            .salary(dto.getSalary())
            .status(dto.getStatus() != null ? dto.getStatus() : EmployeeStatus.ACTIVE)
            .user(user)
            .build();

        employee = employeeRepository.save(employee);
        user.setEmployee(employee);

        // 5. Seed Leave Balances for active leave types
        int currentYear = LocalDate.now().getYear();
        List<LeaveType> leaveTypes = leaveTypeRepository.findByIsActiveTrue();
        for (LeaveType lt : leaveTypes) {
            LeaveBalance balance = LeaveBalance.builder()
                .employee(employee)
                .leaveType(lt)
                .year(currentYear)
                .allocatedDays(BigDecimal.valueOf(lt.getMaxDaysPerYear()))
                .usedDays(BigDecimal.ZERO)
                .pendingDays(BigDecimal.ZERO)
                .build();
            leaveBalanceRepository.save(balance);
        }

        log.info("Employee created successfully with code: {}", employeeCode);
        return mapToResponseDto(employee);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateDto dto) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (employeeRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new DuplicateEmailException("Email " + dto.getEmail() + " is already taken by another employee.");
        }

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setGender(dto.getGender());
        employee.setAddress(dto.getAddress());
        employee.setDesignation(dto.getDesignation());
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setEmploymentType(dto.getEmploymentType());
        employee.setSalary(dto.getSalary());

        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
            if (employee.getUser() != null) {
                employee.getUser().setIsActive(dto.getStatus() == EmployeeStatus.ACTIVE);
            }
        }

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
            employee.setDepartment(dept);
        } else {
            employee.setDepartment(null);
        }

        if (dto.getManagerId() != null) {
            if (dto.getManagerId().equals(id)) {
                throw new IllegalArgumentException("An employee cannot be their own manager.");
            }
            Employee mgr = employeeRepository.findById(dto.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", dto.getManagerId()));
            employee.setManager(mgr);
        } else {
            employee.setManager(null);
        }

        if (employee.getUser() != null) {
            employee.getUser().setEmail(dto.getEmail());
            if (dto.getRole() != null) {
                employee.getUser().setRole(dto.getRole());
            }
            userRepository.save(employee.getUser());
        }

        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated successfully: {}", updated.getEmployeeCode());
        return mapToResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Soft-delete / Deactivate
        employee.setStatus(EmployeeStatus.TERMINATED);
        if (employee.getUser() != null) {
            employee.getUser().setIsActive(false);
            userRepository.save(employee.getUser());
        }
        employeeRepository.save(employee);
        log.info("Employee deactivated (terminated) ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getEmployeeLeaveBalances(Long employeeId, Integer year) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        List<LeaveBalance> balances = leaveBalanceRepository.findActiveBalancesByEmployeeAndYear(employeeId, targetYear);
        return balances.stream().map(LeaveBalanceDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getSubordinates(Long managerId) {
        return employeeRepository.findByManagerId(managerId).stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    private EmployeeResponseDto mapToResponseDto(Employee emp) {
        return EmployeeResponseDto.builder()
            .id(emp.getId())
            .employeeCode(emp.getEmployeeCode())
            .firstName(emp.getFirstName())
            .lastName(emp.getLastName())
            .fullName(emp.getFullName())
            .email(emp.getEmail())
            .phone(emp.getPhone())
            .dateOfBirth(emp.getDateOfBirth())
            .gender(emp.getGender())
            .address(emp.getAddress())
            .departmentId(emp.getDepartment() != null ? emp.getDepartment().getId() : null)
            .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : "Unassigned")
            .designation(emp.getDesignation())
            .joiningDate(emp.getJoiningDate())
            .managerId(emp.getManager() != null ? emp.getManager().getId() : null)
            .managerName(emp.getManager() != null ? emp.getManager().getFullName() : null)
            .employmentType(emp.getEmploymentType())
            .salary(emp.getSalary())
            .status(emp.getStatus())
            .profilePictureUrl(emp.getProfilePictureUrl())
            .userId(emp.getUser() != null ? emp.getUser().getId() : null)
            .role(emp.getUser() != null ? emp.getUser().getRole() : null)
            .createdAt(emp.getCreatedAt())
            .updatedAt(emp.getUpdatedAt())
            .build();
    }
}
