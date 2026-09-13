package com.erp.service.impl;

import com.erp.dto.request.LeaveTypeRequestDto;
import com.erp.dto.response.LeaveTypeResponseDto;
import com.erp.entity.Employee;
import com.erp.entity.LeaveBalance;
import com.erp.entity.LeaveType;
import com.erp.exception.BadRequestException;
import com.erp.exception.ResourceNotFoundException;
import com.erp.repository.EmployeeRepository;
import com.erp.repository.LeaveBalanceRepository;
import com.erp.repository.LeaveTypeRepository;
import com.erp.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeResponseDto> getAllLeaveTypes() {
        return leaveTypeRepository.findAll().stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeResponseDto> getActiveLeaveTypes() {
        return leaveTypeRepository.findByIsActiveTrue().stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveTypeResponseDto getLeaveTypeById(Long id) {
        LeaveType lt = leaveTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));
        return mapToResponseDto(lt);
    }

    @Override
    @Transactional
    public LeaveTypeResponseDto createLeaveType(LeaveTypeRequestDto dto) {
        if (leaveTypeRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Leave type with name '" + dto.getName() + "' already exists");
        }

        LeaveType leaveType = LeaveType.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .maxDaysPerYear(dto.getMaxDaysPerYear())
            .isPaid(dto.getIsPaid() != null ? dto.getIsPaid() : true)
            .applicableGender(dto.getApplicableGender())
            .requiresDocument(dto.getRequiresDocument() != null ? dto.getRequiresDocument() : false)
            .minNoticeDays(dto.getMinNoticeDays() != null ? dto.getMinNoticeDays() : 0)
            .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
            .build();

        LeaveType saved = leaveTypeRepository.save(leaveType);

        // Also retroactively create leave balances for all active employees for current year
        int currentYear = LocalDate.now().getYear();
        List<Employee> activeEmployees = employeeRepository.findAll();
        for (Employee emp : activeEmployees) {
            boolean exists = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(emp.getId(), saved.getId(), currentYear)
                .isPresent();
            if (!exists) {
                LeaveBalance balance = LeaveBalance.builder()
                    .employee(emp)
                    .leaveType(saved)
                    .year(currentYear)
                    .allocatedDays(BigDecimal.valueOf(saved.getMaxDaysPerYear()))
                    .usedDays(BigDecimal.ZERO)
                    .pendingDays(BigDecimal.ZERO)
                    .build();
                leaveBalanceRepository.save(balance);
            }
        }

        log.info("Created leave type: {}", saved.getName());
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public LeaveTypeResponseDto updateLeaveType(Long id, LeaveTypeRequestDto dto) {
        LeaveType lt = leaveTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));

        if (leaveTypeRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new BadRequestException("Leave type name '" + dto.getName() + "' is already in use");
        }

        lt.setName(dto.getName());
        lt.setDescription(dto.getDescription());
        lt.setMaxDaysPerYear(dto.getMaxDaysPerYear());
        if (dto.getIsPaid() != null) lt.setIsPaid(dto.getIsPaid());
        lt.setApplicableGender(dto.getApplicableGender());
        if (dto.getRequiresDocument() != null) lt.setRequiresDocument(dto.getRequiresDocument());
        if (dto.getMinNoticeDays() != null) lt.setMinNoticeDays(dto.getMinNoticeDays());
        if (dto.getIsActive() != null) lt.setIsActive(dto.getIsActive());

        LeaveType updated = leaveTypeRepository.save(lt);
        log.info("Updated leave type ID: {}", id);
        return mapToResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteLeaveType(Long id) {
        LeaveType lt = leaveTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));
        lt.setIsActive(false);
        leaveTypeRepository.save(lt);
        log.info("Deactivated leave type ID: {}", id);
    }

    private LeaveTypeResponseDto mapToResponseDto(LeaveType lt) {
        return LeaveTypeResponseDto.builder()
            .id(lt.getId())
            .name(lt.getName())
            .description(lt.getDescription())
            .maxDaysPerYear(lt.getMaxDaysPerYear())
            .isPaid(lt.getIsPaid())
            .applicableGender(lt.getApplicableGender())
            .requiresDocument(lt.getRequiresDocument())
            .minNoticeDays(lt.getMinNoticeDays())
            .isActive(lt.getIsActive())
            .createdAt(lt.getCreatedAt())
            .updatedAt(lt.getUpdatedAt())
            .build();
    }
}
