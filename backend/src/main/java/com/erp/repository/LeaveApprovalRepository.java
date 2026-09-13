package com.erp.repository;

import com.erp.entity.LeaveApproval;
import com.erp.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, Long> {

    List<LeaveApproval> findByLeaveRequestIdOrderByApprovedAtAsc(Long leaveRequestId);

    Optional<LeaveApproval> findByLeaveRequestIdAndApproverRole(
        Long leaveRequestId, Role approverRole
    );

    boolean existsByLeaveRequestIdAndApproverRole(Long leaveRequestId, Role approverRole);
}
