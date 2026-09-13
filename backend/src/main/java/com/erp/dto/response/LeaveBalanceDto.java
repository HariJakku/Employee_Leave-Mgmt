package com.erp.dto.response;

import com.erp.entity.LeaveBalance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDto {
    private Long id;
    private Long leaveTypeId;
    private String leaveTypeName;
    private Integer year;
    private BigDecimal allocatedDays;
    private BigDecimal usedDays;
    private BigDecimal pendingDays;
    private BigDecimal remainingDays;

    public static LeaveBalanceDto fromEntity(LeaveBalance balance) {
        return LeaveBalanceDto.builder()
            .id(balance.getId())
            .leaveTypeId(balance.getLeaveType().getId())
            .leaveTypeName(balance.getLeaveType().getName())
            .year(balance.getYear())
            .allocatedDays(balance.getAllocatedDays())
            .usedDays(balance.getUsedDays())
            .pendingDays(balance.getPendingDays())
            .remainingDays(balance.getRemainingDays())
            .build();
    }
}
