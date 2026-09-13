package com.erp.dto.response;

import com.erp.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private Role role;
    private Boolean isActive;
    private Long employeeId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String designation;
    private String departmentName;
}
