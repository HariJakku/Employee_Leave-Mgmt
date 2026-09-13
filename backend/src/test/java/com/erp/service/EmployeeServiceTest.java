package com.erp.service;

import com.erp.dto.request.EmployeeCreateDto;
import com.erp.dto.response.EmployeeResponseDto;
import com.erp.entity.Department;
import com.erp.entity.Employee;
import com.erp.entity.User;
import com.erp.enums.EmployeeStatus;
import com.erp.enums.Role;
import com.erp.exception.DuplicateEmailException;
import com.erp.exception.ResourceNotFoundException;
import com.erp.repository.*;
import com.erp.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private LeaveTypeRepository leaveTypeRepository;
    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Department testDept;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testDept = Department.builder().id(1L).name("Engineering").build();

        testEmployee = Employee.builder()
            .id(1L)
            .employeeCode("EMP-0001")
            .firstName("Alice")
            .lastName("Smith")
            .email("alice.smith@erp.com")
            .department(testDept)
            .designation("Staff Engineer")
            .status(EmployeeStatus.ACTIVE)
            .build();
    }

    @Test
    @DisplayName("Create Employee: Should succeed when email is unique")
    void testCreateEmployee_Success() {
        EmployeeCreateDto dto = EmployeeCreateDto.builder()
            .firstName("Alice")
            .lastName("Smith")
            .email("alice.smith@erp.com")
            .role(Role.EMPLOYEE)
            .departmentId(1L)
            .designation("Staff Engineer")
            .salary(BigDecimal.valueOf(95000))
            .build();

        when(employeeRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed-pwd");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeRepository.findMaxEmployeeCodeNumber()).thenReturn(0);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDept));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> {
            Employee e = i.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(leaveTypeRepository.findByIsActiveTrue()).thenReturn(List.of());

        EmployeeResponseDto result = employeeService.createEmployee(dto);

        assertNotNull(result);
        assertEquals("EMP-0001", result.getEmployeeCode());
        assertEquals("Alice Smith", result.getFullName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create Employee: Should throw DuplicateEmailException when email already exists")
    void testCreateEmployee_DuplicateEmail() {
        EmployeeCreateDto dto = EmployeeCreateDto.builder()
            .firstName("Alice")
            .lastName("Smith")
            .email("alice.smith@erp.com")
            .build();

        when(employeeRepository.existsByEmail("alice.smith@erp.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> employeeService.createEmployee(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get Employee By ID: Should return employee when found")
    void testGetEmployeeById_Found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        EmployeeResponseDto result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals("EMP-0001", result.getEmployeeCode());
        assertEquals("Engineering", result.getDepartmentName());
    }

    @Test
    @DisplayName("Get Employee By ID: Should throw ResourceNotFoundException when not found")
    void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }
}
