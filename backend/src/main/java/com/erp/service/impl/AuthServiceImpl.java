package com.erp.service.impl;

import com.erp.dto.request.ChangePasswordRequest;
import com.erp.dto.request.LoginRequest;
import com.erp.dto.request.RegisterRequest;
import com.erp.dto.response.AuthResponse;
import com.erp.dto.response.UserResponseDto;
import com.erp.entity.*;
import com.erp.enums.EmployeeStatus;
import com.erp.enums.NotificationType;
import com.erp.exception.BadRequestException;
import com.erp.exception.DuplicateEmailException;
import com.erp.exception.ResourceNotFoundException;
import com.erp.repository.*;
import com.erp.security.JwtTokenProvider;
import com.erp.security.SecurityUtils;
import com.erp.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getEmail());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BadRequestException("Account has been deactivated. Please contact your administrator.");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole());

        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .user(mapToUserResponseDto(user))
            .build();
    }

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail()) || employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email " + request.getEmail() + " is already in use");
        }

        // 1. Create User
        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .isActive(true)
            .build();
        user = userRepository.save(user);

        // 2. Generate Next Employee Code
        int maxCode = employeeRepository.findMaxEmployeeCodeNumber();
        String nextEmployeeCode = String.format("EMP-%04d", maxCode + 1);

        // 3. Resolve Department & Manager
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", request.getManagerId()));
        }

        // 4. Create Employee Record
        Employee employee = Employee.builder()
            .employeeCode(nextEmployeeCode)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .dateOfBirth(request.getDateOfBirth())
            .gender(request.getGender())
            .address(request.getAddress())
            .department(department)
            .designation(request.getDesignation())
            .joiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : LocalDate.now())
            .manager(manager)
            .employmentType(request.getEmploymentType())
            .salary(request.getSalary())
            .status(EmployeeStatus.ACTIVE)
            .user(user)
            .build();
        employee = employeeRepository.save(employee);
        user.setEmployee(employee);

        // 5. Initialize Leave Balances for the current year
        int currentYear = LocalDate.now().getYear();
        List<LeaveType> activeLeaveTypes = leaveTypeRepository.findByIsActiveTrue();
        for (LeaveType lt : activeLeaveTypes) {
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

        // 6. Create Welcome Notification
        Notification notification = Notification.builder()
            .recipient(user)
            .title("Welcome to ERP System")
            .message(String.format("Welcome %s! Your employee profile has been created with code %s.", employee.getFullName(), nextEmployeeCode))
            .type(NotificationType.GENERAL)
            .isRead(false)
            .build();
        notificationRepository.save(notification);

        return mapToUserResponseDto(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation password do not match");
        }

        User currentUser = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        currentUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
        log.info("Password successfully changed for user: {}", currentUser.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUserProfile() {
        User user = securityUtils.getCurrentUser();
        return mapToUserResponseDto(user);
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        Employee emp = user.getEmployee();
        return UserResponseDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .isActive(user.getIsActive())
            .employeeId(emp != null ? emp.getId() : null)
            .employeeCode(emp != null ? emp.getEmployeeCode() : null)
            .firstName(emp != null ? emp.getFirstName() : "Admin")
            .lastName(emp != null ? emp.getLastName() : "User")
            .designation(emp != null ? emp.getDesignation() : null)
            .departmentName(emp != null && emp.getDepartment() != null ? emp.getDepartment().getName() : null)
            .build();
    }
}
