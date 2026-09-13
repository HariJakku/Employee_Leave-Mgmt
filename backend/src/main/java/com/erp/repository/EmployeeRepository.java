package com.erp.repository;

import com.erp.entity.Employee;
import com.erp.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Employee> findByUserId(Long userId);

    /**
     * Full-text search across name and email with department and status filters.
     * Real backend pagination — React must send page/size params.
     */
    @Query("""
        SELECT e FROM Employee e
        LEFT JOIN FETCH e.department d
        WHERE (:search IS NULL OR :search = '' OR
               LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(e.lastName)  LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(e.email)     LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:departmentId IS NULL OR d.id = :departmentId)
          AND (:status IS NULL OR e.status = :status)
          AND (:designation IS NULL OR :designation = '' OR
               LOWER(e.designation) LIKE LOWER(CONCAT('%', :designation, '%')))
        """)
    Page<Employee> searchEmployees(
        @Param("search")       String search,
        @Param("departmentId") Long departmentId,
        @Param("status")       EmployeeStatus status,
        @Param("designation")  String designation,
        Pageable pageable
    );

    List<Employee> findByManagerId(Long managerId);

    List<Employee> findByDepartmentId(Long departmentId);

    long countByStatus(EmployeeStatus status);

    long countByDepartmentId(Long departmentId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(e.employeeCode, 5) AS int)), 0) FROM Employee e")
    int findMaxEmployeeCodeNumber();
}
