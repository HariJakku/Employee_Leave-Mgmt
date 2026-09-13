package com.erp.repository;

import com.erp.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
        Long employeeId, Long leaveTypeId, Integer year
    );

    List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, Integer year);

    @Query("""
        SELECT lb FROM LeaveBalance lb
        JOIN FETCH lb.leaveType lt
        WHERE lb.employee.id = :employeeId AND lb.year = :year AND lt.isActive = true
        """)
    List<LeaveBalance> findActiveBalancesByEmployeeAndYear(
        @Param("employeeId") Long employeeId,
        @Param("year") Integer year
    );
}
