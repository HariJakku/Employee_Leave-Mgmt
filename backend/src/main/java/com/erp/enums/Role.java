package com.erp.enums;

/**
 * User roles — controls access across the entire system.
 * Must match role names in Spring Security @PreAuthorize expressions
 * and React ROLES constants.
 */
public enum Role {
    ADMIN,
    HR,
    MANAGER,
    EMPLOYEE
}
