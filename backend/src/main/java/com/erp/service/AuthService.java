package com.erp.service;

import com.erp.dto.request.ChangePasswordRequest;
import com.erp.dto.request.LoginRequest;
import com.erp.dto.request.RegisterRequest;
import com.erp.dto.response.AuthResponse;
import com.erp.dto.response.UserResponseDto;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    UserResponseDto register(RegisterRequest request);
    void changePassword(ChangePasswordRequest request);
    UserResponseDto getCurrentUserProfile();
}
