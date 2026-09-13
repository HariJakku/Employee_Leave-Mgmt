package com.erp.service.impl;

import com.erp.dto.request.LeaveApplicationRequestDto;
import com.erp.dto.response.LeaveApprovalResponseDto;
import com.erp.dto.response.LeaveBalanceDto;
import com.erp.dto.response.LeaveRequestResponseDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.entity.*;
import com.erp.enums.LeaveStatus;
import com.erp.enums.NotificationType;
import com.erp.enums.Role;
import com.erp.exception.*;
import com.erp.repository.*;
import com.erp.security.SecurityUtils;
import com.erp.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public LeaveRequestResponseDto applyLeave(LeaveApplicationRequestDto dto) {
        User currentUser = securityUtils.getCurrentUser();
        Employee employee = currentUser.getEmployee();

        if (employee == null) {
            throw new BadRequestException("Current user account is not associated with an employee profile.");
        }

        // 1. Date Validation
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new InvalidLeaveDateException("Leave start date cannot be after end date.");
        }

        // Calculate days
        long daysCount = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        BigDecimal totalDays = BigDecimal.valueOf(daysCount);

        // 2. Overlapping Leaves Check (Business Rule #1)
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
            employee.getId(), dto.getStartDate(), dto.getEndDate()
        );
        if (!overlapping.isEmpty()) {
            throw new OverlappingLeaveException(
                "You already have a pending or approved leave request overlapping with the selected dates (" +
                overlapping.get(0).getStartDate() + " to " + overlapping.get(0).getEndDate() + ")."
            );
        }

        // 3. Leave Type & Balance Check
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", dto.getLeaveTypeId()));

        if (!leaveType.getIsActive()) {
            throw new BadRequestException("Leave type '" + leaveType.getName() + "' is currently inactive.");
        }

        int year = dto.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
            .findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), leaveType.getId(), year)
            .orElseThrow(() -> new BadRequestException(
                "No leave balance allocated for " + leaveType.getName() + " in year " + year
            ));

        if (balance.getRemainingDays().compareTo(totalDays) < 0) {
            throw new InsufficientLeaveBalanceException(
                String.format("Insufficient %s balance. Required: %s days, Remaining available: %s days.",
                    leaveType.getName(), totalDays, balance.getRemainingDays())
            );
        }

        // 4. Save Leave Request in PENDING status
        LeaveRequest leaveRequest = LeaveRequest.builder()
            .employee(employee)
            .leaveType(leaveType)
            .startDate(dto.getStartDate())
            .endDate(dto.getEndDate())
            .totalDays(totalDays)
            .reason(dto.getReason())
            .documentUrl(dto.getDocumentUrl())
            .status(LeaveStatus.PENDING)
            .appliedAt(LocalDateTime.now())
            .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        // 5. Update LeaveBalance: Increment pending_days (Do not deduct from usedDays yet!)
        balance.setPendingDays(balance.getPendingDays().add(totalDays));
        leaveBalanceRepository.save(balance);

        // 6. Notifications: Notify employee & manager
        Notification empNotif = Notification.builder()
            .recipient(currentUser)
            .title("Leave Request Submitted")
            .message(String.format("Your leave request for %s (%s to %s, %s days) has been submitted for approval.",
                leaveType.getName(), dto.getStartDate(), dto.getEndDate(), totalDays))
            .type(NotificationType.LEAVE_APPLIED)
            .entityType("LeaveRequest")
            .entityId(saved.getId())
            .isRead(false)
            .build();
        notificationRepository.save(empNotif);

        if (employee.getManager() != null && employee.getManager().getUser() != null) {
            Notification mgrNotif = Notification.builder()
                .recipient(employee.getManager().getUser())
                .title("New Leave Approval Request")
                .message(String.format("%s has requested %s days of %s (%s to %s).",
                    employee.getFullName(), totalDays, leaveType.getName(), dto.getStartDate(), dto.getEndDate()))
                .type(NotificationType.LEAVE_APPLIED)
                .entityType("LeaveRequest")
                .entityId(saved.getId())
                .isRead(false)
                .build();
            notificationRepository.save(mgrNotif);
        }

        log.info("Leave request #{} submitted by employee {}", saved.getId(), employee.getEmployeeCode());
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<LeaveRequestResponseDto> getMyLeaves(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Employee employee = currentUser.getEmployee();
        if (employee == null) {
            throw new BadRequestException("Current user account is not associated with an employee profile.");
        }

        Page<LeaveRequest> page = leaveRequestRepository.findByEmployeeId(employee.getId(), pageable);
        return PaginatedResponse.fromPage(page.map(this::mapToDto));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<LeaveRequestResponseDto> getAllLeaves(Long employeeId, LeaveStatus status, Pageable pageable) {
        Page<LeaveRequest> page;
        if (employeeId != null) {
            page = leaveRequestRepository.findByEmployeeId(employeeId, pageable);
        } else if (status != null) {
            page = leaveRequestRepository.findByStatus(status, pageable);
        } else {
            page = leaveRequestRepository.findAll(pageable);
        }
        return PaginatedResponse.fromPage(page.map(this::mapToDto));
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestResponseDto getLeaveById(Long id) {
        LeaveRequest lr = leaveRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        return mapToDto(lr);
    }

    @Override
    @Transactional
    public LeaveRequestResponseDto cancelLeave(Long id) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        User currentUser = securityUtils.getCurrentUser();
        boolean isOwner = leave.getEmployee().getUser() != null &&
            leave.getEmployee().getUser().getId().equals(currentUser.getId());
        boolean isPrivileged = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.HR;

        if (!isOwner && !isPrivileged) {
            throw new UnauthorizedException("You are not authorized to cancel this leave request.");
        }

        if (leave.getStatus() == LeaveStatus.CANCELLED || leave.getStatus() == LeaveStatus.REJECTED) {
            throw new BadRequestException("Cannot cancel a leave request that is already " + leave.getStatus());
        }

        int year = leave.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
            .findByEmployeeIdAndLeaveTypeIdAndYear(leave.getEmployee().getId(), leave.getLeaveType().getId(), year)
            .orElse(null);

        // Restore leave balances (Business Rule #4: Cancelled approved leave restores balance)
        if (balance != null) {
            if (leave.getStatus() == LeaveStatus.PENDING || leave.getStatus() == LeaveStatus.MANAGER_APPROVED) {
                // If pending/manager-approved, revert pendingDays
                balance.setPendingDays(balance.getPendingDays().subtract(leave.getTotalDays()).max(BigDecimal.ZERO));
            } else if (leave.getStatus() == LeaveStatus.APPROVED) {
                // If already approved, restore usedDays back to balance!
                balance.setUsedDays(balance.getUsedDays().subtract(leave.getTotalDays()).max(BigDecimal.ZERO));
            }
            leaveBalanceRepository.save(balance);
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest updated = leaveRequestRepository.save(leave);

        // Notify employee
        if (leave.getEmployee().getUser() != null) {
            Notification notif = Notification.builder()
                .recipient(leave.getEmployee().getUser())
                .title("Leave Request Cancelled")
                .message(String.format("Your leave request for %s (%s to %s) has been cancelled.",
                    leave.getLeaveType().getName(), leave.getStartDate(), leave.getEndDate()))
                .type(NotificationType.LEAVE_CANCELLED)
                .entityType("LeaveRequest")
                .entityId(leave.getId())
                .isRead(false)
                .build();
            notificationRepository.save(notif);
        }

        log.info("Leave request #{} cancelled", id);
        return mapToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getMyLeaveBalances() {
        User currentUser = securityUtils.getCurrentUser();
        Employee employee = currentUser.getEmployee();
        if (employee == null) {
            throw new BadRequestException("No employee profile linked to current user.");
        }
        int currentYear = LocalDate.now().getYear();
        return leaveBalanceRepository.findActiveBalancesByEmployeeAndYear(employee.getId(), currentYear).stream()
            .map(LeaveBalanceDto::fromEntity)
            .collect(Collectors.toList());
    }

    private LeaveRequestResponseDto mapToDto(LeaveRequest lr) {
        List<LeaveApprovalResponseDto> approvalDtos = lr.getApprovals().stream()
            .map(a -> LeaveApprovalResponseDto.builder()
                .id(a.getId())
                .leaveRequestId(lr.getId())
                .approverId(a.getApprover().getId())
                .approverName(a.getApprover().getEmail())
                .approverRole(a.getApproverRole())
                .action(a.getAction())
                .comments(a.getComments())
                .approvedAt(a.getApprovedAt())
                .build())
            .collect(Collectors.toList());

        return LeaveRequestResponseDto.builder()
            .id(lr.getId())
            .employeeId(lr.getEmployee().getId())
            .employeeName(lr.getEmployee().getFullName())
            .employeeCode(lr.getEmployee().getEmployeeCode())
            .departmentName(lr.getEmployee().getDepartment() != null ? lr.getEmployee().getDepartment().getName() : null)
            .leaveTypeId(lr.getLeaveType().getId())
            .leaveTypeName(lr.getLeaveType().getName())
            .startDate(lr.getStartDate())
            .endDate(lr.getEndDate())
            .totalDays(lr.getTotalDays())
            .reason(lr.getReason())
            .documentUrl(lr.getDocumentUrl())
            .status(lr.getStatus())
            .rejectionReason(lr.getRejectionReason())
            .appliedAt(lr.getAppliedAt())
            .updatedAt(lr.getUpdatedAt())
            .approvals(approvalDtos)
            .build();
    }
}
