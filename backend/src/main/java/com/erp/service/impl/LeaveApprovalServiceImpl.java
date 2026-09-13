package com.erp.service.impl;

import com.erp.dto.request.LeaveApprovalRequestDto;
import com.erp.dto.response.LeaveApprovalResponseDto;
import com.erp.dto.response.LeaveRequestResponseDto;
import com.erp.dto.response.PaginatedResponse;
import com.erp.entity.*;
import com.erp.enums.ApprovalAction;
import com.erp.enums.LeaveStatus;
import com.erp.enums.NotificationType;
import com.erp.enums.Role;
import com.erp.exception.BadRequestException;
import com.erp.exception.ResourceNotFoundException;
import com.erp.exception.UnauthorizedException;
import com.erp.repository.*;
import com.erp.security.SecurityUtils;
import com.erp.service.LeaveApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveApprovalServiceImpl implements LeaveApprovalService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveApprovalRepository leaveApprovalRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<LeaveRequestResponseDto> getPendingApprovals(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<LeaveRequest> page;

        if (currentUser.getRole() == Role.MANAGER) {
            Employee managerEmp = currentUser.getEmployee();
            if (managerEmp == null) {
                return PaginatedResponse.fromPage(Page.empty(pageable));
            }
            // Manager sees team leaves in PENDING state
            page = leaveRequestRepository.findPendingForManager(managerEmp.getId(), pageable);
        } else {
            // HR & Admin see both PENDING and MANAGER_APPROVED
            page = leaveRequestRepository.findByStatus(LeaveStatus.MANAGER_APPROVED, pageable);
            if (page.isEmpty()) {
                page = leaveRequestRepository.findByStatus(LeaveStatus.PENDING, pageable);
            }
        }

        return PaginatedResponse.fromPage(page.map(this::mapToDto));
    }

    @Override
    @Transactional
    public LeaveRequestResponseDto approveLeave(Long leaveRequestId, LeaveApprovalRequestDto dto) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", leaveRequestId));

        User currentUser = securityUtils.getCurrentUser();
        Employee applicant = leave.getEmployee();

        // Business Rule #5: Employees cannot approve their own leave
        if (applicant.getUser() != null && applicant.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You cannot approve your own leave request.");
        }

        Role userRole = currentUser.getRole();

        if (userRole == Role.MANAGER) {
            // Business Rule #6: Managers can approve only their team's leave requests
            if (applicant.getManager() == null ||
                applicant.getManager().getUser() == null ||
                !applicant.getManager().getUser().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("You can only approve leave requests for your direct team members.");
            }

            if (leave.getStatus() != LeaveStatus.PENDING) {
                throw new BadRequestException("Leave request is not in PENDING status.");
            }

            // Record approval
            LeaveApproval approval = LeaveApproval.builder()
                .leaveRequest(leave)
                .approver(currentUser)
                .approverRole(Role.MANAGER)
                .action(ApprovalAction.APPROVED)
                .comments(dto.getComments())
                .approvedAt(LocalDateTime.now())
                .build();
            leaveApprovalRepository.save(approval);

            // Transition: PENDING -> MANAGER_APPROVED
            leave.setStatus(LeaveStatus.MANAGER_APPROVED);
            leaveRequestRepository.save(leave);

            // Notify Employee
            sendNotification(applicant.getUser(),
                "Leave Approved by Manager",
                "Your manager approved your leave request. It is now awaiting final HR approval.",
                NotificationType.LEAVE_APPROVED, leave.getId());

            log.info("Leave request #{} approved by manager {}", leaveRequestId, currentUser.getEmail());

        } else if (userRole == Role.HR || userRole == Role.ADMIN) {
            // HR/Admin final approval
            if (leave.getStatus() != LeaveStatus.MANAGER_APPROVED && leave.getStatus() != LeaveStatus.PENDING) {
                throw new BadRequestException("Leave request cannot be approved from status: " + leave.getStatus());
            }

            // Record approval
            LeaveApproval approval = LeaveApproval.builder()
                .leaveRequest(leave)
                .approver(currentUser)
                .approverRole(userRole)
                .action(ApprovalAction.APPROVED)
                .comments(dto.getComments())
                .approvedAt(LocalDateTime.now())
                .build();
            leaveApprovalRepository.save(approval);

            // Transition to final APPROVED
            leave.setStatus(LeaveStatus.APPROVED);
            leaveRequestRepository.save(leave);

            // Business Rule #2: Deduct leave balance only after final approval!
            int year = leave.getStartDate().getYear();
            LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(applicant.getId(), leave.getLeaveType().getId(), year)
                .orElseThrow(() -> new BadRequestException("Leave balance record not found"));

            // Move days from pending to used
            balance.setPendingDays(balance.getPendingDays().subtract(leave.getTotalDays()).max(BigDecimal.ZERO));
            balance.setUsedDays(balance.getUsedDays().add(leave.getTotalDays()));
            leaveBalanceRepository.save(balance);

            // Notify Employee
            sendNotification(applicant.getUser(),
                "Leave Request Approved",
                String.format("Congratulations! Your leave request for %s (%s to %s) has been officially approved.",
                    leave.getLeaveType().getName(), leave.getStartDate(), leave.getEndDate()),
                NotificationType.LEAVE_APPROVED, leave.getId());

            log.info("Leave request #{} fully approved by HR/Admin {}", leaveRequestId, currentUser.getEmail());
        } else {
            throw new UnauthorizedException("You do not have permission to approve leaves.");
        }

        return mapToDto(leave);
    }

    @Override
    @Transactional
    public LeaveRequestResponseDto rejectLeave(Long leaveRequestId, LeaveApprovalRequestDto dto) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", leaveRequestId));

        User currentUser = securityUtils.getCurrentUser();
        Employee applicant = leave.getEmployee();

        // Business Rule #5: Employees cannot approve/reject their own leave
        if (applicant.getUser() != null && applicant.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You cannot reject your own leave request.");
        }

        if (leave.getStatus() == LeaveStatus.REJECTED || leave.getStatus() == LeaveStatus.CANCELLED) {
            throw new BadRequestException("Leave is already " + leave.getStatus());
        }

        Role userRole = currentUser.getRole();
        if (userRole == Role.MANAGER) {
            if (applicant.getManager() == null ||
                applicant.getManager().getUser() == null ||
                !applicant.getManager().getUser().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("You can only reject leave requests for your direct team members.");
            }
        }

        // Record rejection
        LeaveApproval approval = LeaveApproval.builder()
            .leaveRequest(leave)
            .approver(currentUser)
            .approverRole(userRole)
            .action(ApprovalAction.REJECTED)
            .comments(dto.getComments())
            .approvedAt(LocalDateTime.now())
            .build();
        leaveApprovalRepository.save(approval);

        // Transition to REJECTED
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setRejectionReason(dto.getRejectionReason() != null ? dto.getRejectionReason() : dto.getComments());
        leaveRequestRepository.save(leave);

        // Business Rule #3: Rejected leave does not reduce leave balance! Revert pendingDays!
        int year = leave.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
            .findByEmployeeIdAndLeaveTypeIdAndYear(applicant.getId(), leave.getLeaveType().getId(), year)
            .orElse(null);

        if (balance != null) {
            balance.setPendingDays(balance.getPendingDays().subtract(leave.getTotalDays()).max(BigDecimal.ZERO));
            leaveBalanceRepository.save(balance);
        }

        // Notify Employee
        String reasonStr = leave.getRejectionReason() != null ? " Reason: " + leave.getRejectionReason() : "";
        sendNotification(applicant.getUser(),
            "Leave Request Rejected",
            String.format("Your leave request for %s (%s to %s) was rejected.%s",
                leave.getLeaveType().getName(), leave.getStartDate(), leave.getEndDate(), reasonStr),
            NotificationType.LEAVE_REJECTED, leave.getId());

        log.info("Leave request #{} rejected by {}", leaveRequestId, currentUser.getEmail());
        return mapToDto(leave);
    }

    private void sendNotification(User recipient, String title, String msg, NotificationType type, Long entityId) {
        if (recipient == null) return;
        Notification n = Notification.builder()
            .recipient(recipient)
            .title(title)
            .message(msg)
            .type(type)
            .entityType("LeaveRequest")
            .entityId(entityId)
            .isRead(false)
            .build();
        notificationRepository.save(n);
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
