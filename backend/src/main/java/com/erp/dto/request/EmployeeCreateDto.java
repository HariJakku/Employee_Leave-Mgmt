package com.erp.dto.request;

import com.erp.enums.EmployeeStatus;
import com.erp.enums.EmploymentType;
import com.erp.enums.Gender;
import com.erp.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String password; // Optional, default generated if null

    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;

    @NotNull(message = "Role is required")
    private Role role;

    private Long departmentId;
    private String designation;
    private LocalDate joiningDate;
    private Long managerId;
    private EmploymentType employmentType;

    @Positive(message = "Salary must be positive")
    private BigDecimal salary;

    private EmployeeStatus status;
}
