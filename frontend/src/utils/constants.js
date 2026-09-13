/**
 * Application-wide constants.
 * API_BASE_URL is empty string in dev — Vite proxy forwards /api to :8080.
 */

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

// User roles — must match backend Role enum exactly
export const ROLES = {
  ADMIN:    'ADMIN',
  HR:       'HR',
  MANAGER:  'MANAGER',
  EMPLOYEE: 'EMPLOYEE',
}

// Leave statuses — must match backend LeaveStatus enum
export const LEAVE_STATUS = {
  PENDING:          'PENDING',
  MANAGER_APPROVED: 'MANAGER_APPROVED',
  APPROVED:         'APPROVED',
  REJECTED:         'REJECTED',
  CANCELLED:        'CANCELLED',
}

// Attendance statuses
export const ATTENDANCE_STATUS = {
  PRESENT:  'PRESENT',
  ABSENT:   'ABSENT',
  HALF_DAY: 'HALF_DAY',
  ON_LEAVE: 'ON_LEAVE',
}

// Employee statuses
export const EMPLOYEE_STATUS = {
  ACTIVE:     'ACTIVE',
  INACTIVE:   'INACTIVE',
  TERMINATED: 'TERMINATED',
}

// Employment types
export const EMPLOYMENT_TYPE = {
  FULL_TIME: 'FULL_TIME',
  PART_TIME: 'PART_TIME',
  CONTRACT:  'CONTRACT',
  INTERN:    'INTERN',
}

// Pagination defaults
export const PAGE_SIZE = 10

// Local storage keys
export const STORAGE_KEYS = {
  TOKEN:     'erp_token',
  USER:      'erp_user',
  ROLE:      'erp_role',
}
