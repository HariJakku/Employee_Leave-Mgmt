package com.erp.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalRequestDto {

    @Size(max = 500, message = "Comments cannot exceed 500 characters")
    private String comments;

    private String rejectionReason;
}
