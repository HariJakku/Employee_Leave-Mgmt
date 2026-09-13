package com.erp.repository;

import com.erp.entity.Attendance;
import com.erp.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateDesc(
        Long employeeId, LocalDate from, LocalDate to
    );

    Page<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId, Pageable pageable);

    @Query("""
        SELECT a FROM Attendance a
        JOIN a.employee e
        WHERE e.manager.id = :managerId AND a.date = :date
        """)
    List<Attendance> findTeamAttendanceByDate(
        @Param("managerId") Long managerId,
        @Param("date")      LocalDate date
    );

    long countByEmployeeIdAndStatusAndDateBetween(
        Long employeeId, AttendanceStatus status, LocalDate from, LocalDate to
    );

    long countByDateAndStatus(LocalDate date, AttendanceStatus status);
}
