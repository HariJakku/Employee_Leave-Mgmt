-- ====================================================================
-- Employee Management & Leave Management ERP System
-- Database Schema Definition (MySQL 8.0+)
-- ====================================================================

CREATE DATABASE IF NOT EXISTS `employee_erp`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `employee_erp`;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  INDEX `idx_users_email` (`email`)
) ENGINE=InnoDB;

-- 2. Departments Table
CREATE TABLE IF NOT EXISTS `departments` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  `description` VARCHAR(500) NULL,
  `head_employee_id` BIGINT NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  INDEX `idx_dept_name` (`name`)
) ENGINE=InnoDB;

-- 3. Employees Table
CREATE TABLE IF NOT EXISTS `employees` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `employee_code` VARCHAR(20) NOT NULL UNIQUE,
  `first_name` VARCHAR(50) NOT NULL,
  `last_name` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `phone` VARCHAR(20) NULL,
  `date_of_birth` DATE NULL,
  `gender` VARCHAR(10) NULL,
  `address` VARCHAR(300) NULL,
  `department_id` BIGINT NULL,
  `designation` VARCHAR(100) NULL,
  `joining_date` DATE NULL,
  `manager_id` BIGINT NULL,
  `employment_type` VARCHAR(20) NULL,
  `salary` DECIMAL(12,2) NULL,
  `status` VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
  `profile_picture_url` VARCHAR(500) NULL,
  `user_id` BIGINT UNIQUE NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  INDEX `idx_emp_email` (`email`),
  INDEX `idx_emp_dept` (`department_id`),
  INDEX `idx_emp_status` (`status`),
  INDEX `idx_emp_manager` (`manager_id`),
  INDEX `idx_emp_code` (`employee_code`),
  CONSTRAINT `fk_emp_dept` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_emp_manager` FOREIGN KEY (`manager_id`) REFERENCES `employees` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_emp_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 4. Leave Types Table
CREATE TABLE IF NOT EXISTS `leave_types` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(50) NOT NULL UNIQUE,
  `description` VARCHAR(300) NULL,
  `max_days_per_year` INT NOT NULL,
  `is_paid` BOOLEAN NOT NULL DEFAULT TRUE,
  `applicable_gender` VARCHAR(10) NULL,
  `requires_document` BOOLEAN NOT NULL DEFAULT FALSE,
  `min_notice_days` INT NOT NULL DEFAULT 0,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL
) ENGINE=InnoDB;

-- 5. Leave Balances Table
CREATE TABLE IF NOT EXISTS `leave_balances` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `employee_id` BIGINT NOT NULL,
  `leave_type_id` BIGINT NOT NULL,
  `year` INT NOT NULL,
  `allocated_days` DECIMAL(5,1) NOT NULL,
  `used_days` DECIMAL(5,1) NOT NULL DEFAULT 0.0,
  `pending_days` DECIMAL(5,1) NOT NULL DEFAULT 0.0,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  UNIQUE KEY `uk_leave_balance` (`employee_id`, `leave_type_id`, `year`),
  INDEX `idx_lb_employee` (`employee_id`),
  INDEX `idx_lb_year` (`year`),
  CONSTRAINT `fk_lb_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_lb_type` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_types` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Leave Requests Table
CREATE TABLE IF NOT EXISTS `leave_requests` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `employee_id` BIGINT NOT NULL,
  `leave_type_id` BIGINT NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `total_days` DECIMAL(5,1) NOT NULL,
  `reason` VARCHAR(1000) NULL,
  `document_url` VARCHAR(500) NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `applied_at` DATETIME NOT NULL,
  `rejection_reason` VARCHAR(500) NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  INDEX `idx_lr_employee` (`employee_id`),
  INDEX `idx_lr_status` (`status`),
  INDEX `idx_lr_dates` (`start_date`, `end_date`),
  CONSTRAINT `fk_lr_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_lr_type` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_types` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 7. Leave Approvals Table
CREATE TABLE IF NOT EXISTS `leave_approvals` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `leave_request_id` BIGINT NOT NULL,
  `approver_id` BIGINT NOT NULL,
  `approver_role` VARCHAR(20) NOT NULL,
  `action` VARCHAR(10) NOT NULL,
  `comments` VARCHAR(500) NULL,
  `approved_at` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  INDEX `idx_la_request` (`leave_request_id`),
  INDEX `idx_la_approver` (`approver_id`),
  CONSTRAINT `fk_la_request` FOREIGN KEY (`leave_request_id`) REFERENCES `leave_requests` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_la_approver` FOREIGN KEY (`approver_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 8. Attendance Table
CREATE TABLE IF NOT EXISTS `attendance` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `employee_id` BIGINT NOT NULL,
  `date` DATE NOT NULL,
  `check_in` DATETIME NULL,
  `check_out` DATETIME NULL,
  `working_hours` DECIMAL(4,2) NULL,
  `status` VARCHAR(15) NOT NULL DEFAULT 'PRESENT',
  `notes` VARCHAR(300) NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  UNIQUE KEY `uk_attendance_employee_date` (`employee_id`, `date`),
  INDEX `idx_att_employee` (`employee_id`),
  INDEX `idx_att_date` (`date`),
  CONSTRAINT `fk_att_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. Notifications Table
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `recipient_id` BIGINT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `message` VARCHAR(1000) NOT NULL,
  `type` VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
  `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
  `entity_id` BIGINT NULL,
  `entity_type` VARCHAR(50) NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  INDEX `idx_notif_recipient` (`recipient_id`),
  INDEX `idx_notif_read` (`is_read`),
  CONSTRAINT `fk_notif_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Audit Logs Table (Immutable, no updated_at)
CREATE TABLE IF NOT EXISTS `audit_logs` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NULL,
  `action` VARCHAR(50) NOT NULL,
  `entity_type` VARCHAR(50) NULL,
  `entity_id` BIGINT NULL,
  `description` VARCHAR(1000) NULL,
  `ip_address` VARCHAR(45) NULL,
  `created_at` DATETIME NOT NULL,
  INDEX `idx_audit_user` (`user_id`),
  INDEX `idx_audit_action` (`action`),
  INDEX `idx_audit_entity` (`entity_type`, `entity_id`),
  INDEX `idx_audit_created` (`created_at`),
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;
