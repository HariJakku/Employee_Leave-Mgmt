package com.erp.dto.response;

import com.erp.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeResponseDto {
    private Long id;
    private String name;
    private String description;
    private Integer maxDaysPerYear;
    private Boolean isPaid;
    private Gender applicableGender;
    private Boolean requiresDocument;
    private Integer minNoticeDays;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
