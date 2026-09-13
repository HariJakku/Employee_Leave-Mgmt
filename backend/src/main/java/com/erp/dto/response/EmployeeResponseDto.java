package com.erp.dto.response;

import com.erp.enums.EmployeeStatus;
import com.erp.enums.EmploymentType;
import com.erp.enums.Gender;
import com.erp.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDto {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;

    private Long departmentId;
    private String departmentName;

    private String designation;
    private LocalDate joiningDate;

    private Long managerId;
    private String managerName;

    private EmploymentType employmentType;
    private BigDecimal salary;
    private EmployeeStatus status;
    private String profilePictureUrl;

    private Long userId;
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
