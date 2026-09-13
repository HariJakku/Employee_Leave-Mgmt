package com.erp.service;

import com.erp.dto.request.LoginRequest;
import com.erp.dto.response.AuthResponse;
import com.erp.entity.Employee;
import com.erp.entity.User;
import com.erp.enums.Role;
import com.erp.repository.UserRepository;
import com.erp.security.JwtTokenProvider;
import com.erp.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("john.doe@erp.com")
            .passwordHash("$2a$12$hashedPassword")
            .role(Role.EMPLOYEE)
            .isActive(true)
            .build();

        testEmployee = Employee.builder()
            .id(10L)
            .employeeCode("EMP-0001")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@erp.com")
            .user(testUser)
            .build();

        testUser.setEmployee(testEmployee);
    }

    @Test
    @DisplayName("Login: Should return JWT token on valid credentials")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("john.doe@erp.com", "Password@123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail("john.doe@erp.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken("john.doe@erp.com", 1L, Role.EMPLOYEE)).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john.doe@erp.com", response.getUser().getEmail());
        assertEquals(Role.EMPLOYEE, response.getUser().getRole());
    }

    @Test
    @DisplayName("Login: Should throw BadCredentialsException on invalid password")
    void testLogin_InvalidCredentials() {
        LoginRequest request = new LoginRequest("john.doe@erp.com", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
