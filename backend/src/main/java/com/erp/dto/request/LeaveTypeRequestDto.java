package com.erp.dto.request;

import com.erp.enums.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeRequestDto {

    @NotBlank(message = "Leave type name is required")
    private String name;

    private String description;

    @NotNull(message = "Max days per year is required")
    @Min(value = 0, message = "Max days must be 0 or more")
    private Integer maxDaysPerYear;

    @Builder.Default
    private Boolean isPaid = true;

    private Gender applicableGender;

    @Builder.Default
    private Boolean requiresDocument = false;

    @Builder.Default
    private Integer minNoticeDays = 0;

    @Builder.Default
    private Boolean isActive = true;
}
