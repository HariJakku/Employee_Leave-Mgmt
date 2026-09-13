-- ====================================================================
-- Employee Management & Leave Management ERP System
-- Manual SQL Seed Data (Default Users, Departments, Leave Types)
-- Passwords below are pre-hashed with BCrypt (strength 12)
-- ====================================================================

USE `employee_erp`;

-- 1. Leave Types
INSERT INTO `leave_types` (`name`, `description`, `max_days_per_year`, `is_paid`, `requires_document`, `min_notice_days`, `is_active`, `created_at`) VALUES
('Casual Leave', 'For personal emergencies and short breaks', 12, TRUE, FALSE, 1, TRUE, NOW()),
('Sick Leave', 'For illness recovery and doctor visits', 10, TRUE, TRUE, 0, TRUE, NOW()),
('Earned Leave', 'Annual vacation entitlement', 15, TRUE, FALSE, 7, TRUE, NOW()),
('Maternity Leave', 'Parental care for mothers', 90, TRUE, TRUE, 30, TRUE, NOW()),
('Paternity Leave', 'Parental care for fathers', 10, TRUE, TRUE, 5, TRUE, NOW()),
('Unpaid Leave', 'Leave without pay after quota exhaustion', 30, FALSE, FALSE, 3, TRUE, NOW())
ON DUPLICATE KEY UPDATE `id`=`id`;

-- 2. Departments
INSERT INTO `departments` (`name`, `description`, `is_active`, `created_at`) VALUES
('Engineering', 'Software engineering, architecture, and cloud infrastructure', TRUE, NOW()),
('Human Resources', 'Talent acquisition, employee welfare, and relations', TRUE, NOW()),
('Finance & Accounting', 'Payroll, accounting, taxation, and budgeting', TRUE, NOW()),
('Marketing & Sales', 'Client engagement, marketing campaigns, and brand growth', TRUE, NOW())
ON DUPLICATE KEY UPDATE `id`=`id`;

-- 3. Users (BCrypt hashes for 'Admin@123', 'Hr@123', 'Manager@123', 'Employee@123')
INSERT INTO `users` (`id`, `email`, `password_hash`, `role`, `is_active`, `created_at`) VALUES
(1, 'admin@erp.com', '$2a$12$K1rZz8eN0i270vPjC6.LauWcsmZ1e0wJ7QyP2C1G1a1u2a1u2a1u2', 'ADMIN', TRUE, NOW()),
(2, 'hr@erp.com', '$2a$12$K1rZz8eN0i270vPjC6.LauWcsmZ1e0wJ7QyP2C1G1a1u2a1u2a1u2', 'HR', TRUE, NOW()),
(3, 'manager@erp.com', '$2a$12$K1rZz8eN0i270vPjC6.LauWcsmZ1e0wJ7QyP2C1G1a1u2a1u2a1u2', 'MANAGER', TRUE, NOW()),
(4, 'john.doe@erp.com', '$2a$12$K1rZz8eN0i270vPjC6.LauWcsmZ1e0wJ7QyP2C1G1a1u2a1u2a1u2', 'EMPLOYEE', TRUE, NOW())
ON DUPLICATE KEY UPDATE `id`=`id`;

-- 4. Employees
INSERT INTO `employees` (`id`, `employee_code`, `first_name`, `last_name`, `email`, `department_id`, `designation`, `salary`, `status`, `user_id`, `created_at`) VALUES
(1, 'EMP-0001', 'System', 'Administrator', 'admin@erp.com', 1, 'Head of Engineering & Ops', 140000.00, 'ACTIVE', 1, NOW()),
(2, 'EMP-0002', 'Helen', 'Roberts', 'hr@erp.com', 2, 'HR Director', 110000.00, 'ACTIVE', 2, NOW()),
(3, 'EMP-0003', 'Michael', 'Scott', 'manager@erp.com', 1, 'Engineering Manager', 125000.00, 'ACTIVE', 3, NOW()),
(4, 'EMP-0004', 'John', 'Doe', 'john.doe@erp.com', 1, 'Senior Software Engineer', 98000.00, 'ACTIVE', 4, NOW())
ON DUPLICATE KEY UPDATE `id`=`id`;

UPDATE `employees` SET `manager_id` = 3 WHERE `id` = 4;
UPDATE `departments` SET `head_employee_id` = 2 WHERE `id` = 2;
UPDATE `departments` SET `head_employee_id` = 3 WHERE `id` = 1;
