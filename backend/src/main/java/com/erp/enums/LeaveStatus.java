package com.erp.enums;

/**
 * Leave request lifecycle states.
 *
 * Flow:
 *   PENDING → MANAGER_APPROVED → APPROVED
 *   PENDING → REJECTED (by manager or HR)
 *   MANAGER_APPROVED → REJECTED (by HR)
 *   PENDING / MANAGER_APPROVED → CANCELLED (by employee)
 */
public enum LeaveStatus {
    PENDING,
    MANAGER_APPROVED,
    APPROVED,
    REJECTED,
    CANCELLED
}
