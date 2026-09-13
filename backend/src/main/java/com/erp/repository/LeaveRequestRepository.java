package com.erp.repository;

import com.erp.entity.LeaveRequest;
import com.erp.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    /**
     * Check for overlapping leave requests (overlap detection business rule).
     * Excludes CANCELLED and REJECTED to allow re-application after rejection.
     */
    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
          AND lr.status NOT IN ('CANCELLED', 'REJECTED')
          AND lr.startDate <= :endDate
          AND lr.endDate   >= :startDate
        """)
    List<LeaveRequest> findOverlappingLeaves(
        @Param("employeeId") Long employeeId,
        @Param("startDate")  LocalDate startDate,
        @Param("endDate")    LocalDate endDate
    );

    /**
     * Pending requests for manager approval — manager sees only their team's requests.
     */
    @Query("""
        SELECT lr FROM LeaveRequest lr
        JOIN lr.employee e
        WHERE e.manager.id = :managerId
          AND lr.status = 'PENDING'
        """)
    Page<LeaveRequest> findPendingForManager(
        @Param("managerId") Long managerId,
        Pageable pageable
    );

    /**
     * All MANAGER_APPROVED requests waiting for HR/Admin final approval.
     */
    
    long countByStatus(LeaveStatus status);

    @Query("""
        SELECT COUNT(lr) FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId AND lr.status NOT IN ('CANCELLED', 'REJECTED')
          AND lr.startDate <= CURRENT_DATE AND lr.endDate >= CURRENT_DATE
        """)
    long countCurrentLeavesByEmployee(@Param("employeeId") Long employeeId);

    /**
     * Employees currently on leave (approved + today falls within range).
     */
    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.status = 'APPROVED'
          AND lr.startDate <= :today
          AND lr.endDate   >= :today
        """)
    List<LeaveRequest> findCurrentlyOnLeave(@Param("today") LocalDate today);
}
