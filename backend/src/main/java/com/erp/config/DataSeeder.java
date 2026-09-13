package com.erp.config;

import com.erp.entity.*;
import com.erp.enums.*;
import com.erp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds initial demo data for local development and testing.
 * Seeds default leave policies, departments, and 4 role accounts:
 * - ADMIN:    admin@erp.com / Admin@123
 * - HR:       hr@erp.com / Hr@123
 * - MANAGER:  manager@erp.com / Manager@123
 * - EMPLOYEE: john.doe@erp.com / Employee@123
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data, skipping seeder.");
            return;
        }

        log.info("--- Starting Demo Data Seeding ---");

        // 1. Leave Types
        LeaveType casual = LeaveType.builder()
            .name("Casual Leave")
            .description("For personal emergencies and leisure")
            .maxDaysPerYear(12)
            .isPaid(true)
            .minNoticeDays(1)
            .requiresDocument(false)
            .isActive(true)
            .build();

        LeaveType sick = LeaveType.builder()
            .name("Sick Leave")
            .description("For illness recovery and medical appointments")
            .maxDaysPerYear(10)
            .isPaid(true)
            .minNoticeDays(0)
            .requiresDocument(true)
            .isActive(true)
            .build();

        LeaveType earned = LeaveType.builder()
            .name("Earned Leave")
            .description("Annual paid privilege vacation")
            .maxDaysPerYear(15)
            .isPaid(true)
            .minNoticeDays(7)
            .requiresDocument(false)
            .isActive(true)
            .build();

        LeaveType maternity = LeaveType.builder()
            .name("Maternity Leave")
            .description("Paid maternal care for female employees")
            .maxDaysPerYear(90)
            .isPaid(true)
            .applicableGender(Gender.FEMALE)
            .requiresDocument(true)
            .isActive(true)
            .build();

        LeaveType paternity = LeaveType.builder()
            .name("Paternity Leave")
            .description("Paid parental support for male employees")
            .maxDaysPerYear(10)
            .isPaid(true)
            .applicableGender(Gender.MALE)
            .requiresDocument(true)
            .isActive(true)
            .build();

        LeaveType unpaid = LeaveType.builder()
            .name("Unpaid Leave (LWP)")
            .description("Leave without pay when quotas are exhausted")
            .maxDaysPerYear(30)
            .isPaid(false)
            .requiresDocument(false)
            .isActive(true)
            .build();

        leaveTypeRepository.saveAll(List.of(casual, sick, earned, maternity, paternity, unpaid));
        log.info("Seeded 6 standard enterprise leave types");

        // 2. Departments
        Department engineering = Department.builder()
            .name("Engineering")
            .description("Software architecture, product engineering, and quality assurance")
            .isActive(true)
            .build();

        Department hr = Department.builder()
            .name("Human Resources")
            .description("Talent acquisition, employee welfare, and compensation")
            .isActive(true)
            .build();

        Department finance = Department.builder()
            .name("Finance & Accounting")
            .description("Financial reporting, payroll, budgeting, and audits")
            .isActive(true)
            .build();

        Department marketing = Department.builder()
            .name("Marketing & Sales")
            .description("Enterprise growth, client engagement, and sales operations")
            .isActive(true)
            .build();

        departmentRepository.saveAll(List.of(engineering, hr, finance, marketing));
        log.info("Seeded 4 company departments");

        // 3. Admin Account
        User adminUser = User.builder()
            .email("admin@erp.com")
            .passwordHash(passwordEncoder.encode("Admin@123"))
            .role(Role.ADMIN)
            .isActive(true)
            .build();
        adminUser = userRepository.save(adminUser);

        Employee adminEmp = Employee.builder()
            .employeeCode("EMP-0001")
            .firstName("System")
            .lastName("Administrator")
            .email("admin@erp.com")
            .phone("+1 555-0100")
            .gender(Gender.OTHER)
            .department(engineering)
            .designation("Head of Technology & Ops")
            .joiningDate(LocalDate.of(2022, 1, 1))
            .employmentType(EmploymentType.FULL_TIME)
            .salary(BigDecimal.valueOf(140000))
            .status(EmployeeStatus.ACTIVE)
            .user(adminUser)
            .build();
        adminEmp = employeeRepository.save(adminEmp);
        adminUser.setEmployee(adminEmp);

        // 4. HR Account
        User hrUser = User.builder()
            .email("hr@erp.com")
            .passwordHash(passwordEncoder.encode("Hr@123"))
            .role(Role.HR)
            .isActive(true)
            .build();
        hrUser = userRepository.save(hrUser);

        Employee hrEmp = Employee.builder()
            .employeeCode("EMP-0002")
            .firstName("Helen")
            .lastName("Roberts")
            .email("hr@erp.com")
            .phone("+1 555-0101")
            .gender(Gender.FEMALE)
            .department(hr)
            .designation("HR Director")
            .joiningDate(LocalDate.of(2022, 3, 15))
            .employmentType(EmploymentType.FULL_TIME)
            .salary(BigDecimal.valueOf(110000))
            .status(EmployeeStatus.ACTIVE)
            .user(hrUser)
            .build();
        hrEmp = employeeRepository.save(hrEmp);
        hrUser.setEmployee(hrEmp);
        hr.setHeadEmployeeId(hrEmp.getId());
        departmentRepository.save(hr);

        // 5. Manager Account
        User managerUser = User.builder()
            .email("manager@erp.com")
            .passwordHash(passwordEncoder.encode("Manager@123"))
            .role(Role.MANAGER)
            .isActive(true)
            .build();
        managerUser = userRepository.save(managerUser);

        Employee managerEmp = Employee.builder()
            .employeeCode("EMP-0003")
            .firstName("Michael")
            .lastName("Scott")
            .email("manager@erp.com")
            .phone("+1 555-0102")
            .gender(Gender.MALE)
            .department(engineering)
            .designation("Engineering Manager")
            .joiningDate(LocalDate.of(2023, 2, 1))
            .employmentType(EmploymentType.FULL_TIME)
            .salary(BigDecimal.valueOf(125000))
            .status(EmployeeStatus.ACTIVE)
            .user(managerUser)
            .build();
        managerEmp = employeeRepository.save(managerEmp);
        managerUser.setEmployee(managerEmp);
        engineering.setHeadEmployeeId(managerEmp.getId());
        departmentRepository.save(engineering);

        // 6. Employee Account (Reports to Manager)
        User employeeUser = User.builder()
            .email("john.doe@erp.com")
            .passwordHash(passwordEncoder.encode("Employee@123"))
            .role(Role.EMPLOYEE)
            .isActive(true)
            .build();
        employeeUser = userRepository.save(employeeUser);

        Employee employeeEmp = Employee.builder()
            .employeeCode("EMP-0004")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@erp.com")
            .phone("+1 555-0103")
            .gender(Gender.MALE)
            .department(engineering)
            .designation("Senior Full-Stack Engineer")
            .manager(managerEmp)
            .joiningDate(LocalDate.of(2023, 6, 1))
            .employmentType(EmploymentType.FULL_TIME)
            .salary(BigDecimal.valueOf(98000))
            .status(EmployeeStatus.ACTIVE)
            .user(employeeUser)
            .build();
        employeeEmp = employeeRepository.save(employeeEmp);
        employeeUser.setEmployee(employeeEmp);

        // 7. Seed Balances for John Doe & Manager
        int currentYear = LocalDate.now().getYear();
        for (Employee emp : List.of(employeeEmp, managerEmp, hrEmp, adminEmp)) {
            for (LeaveType lt : List.of(casual, sick, earned)) {
                LeaveBalance lb = LeaveBalance.builder()
                    .employee(emp)
                    .leaveType(lt)
                    .year(currentYear)
                    .allocatedDays(BigDecimal.valueOf(lt.getMaxDaysPerYear()))
                    .usedDays(BigDecimal.ZERO)
                    .pendingDays(BigDecimal.ZERO)
                    .build();
                leaveBalanceRepository.save(lb);
            }
        }
        log.info("Seeded employee leave balances for {}", currentYear);

        // 8. Sample Leave Request for testing approval flow
        // John Doe has a pending 2-day Sick Leave request
        LocalDate nextWeekStart = LocalDate.now().plusDays(3);
        LocalDate nextWeekEnd = LocalDate.now().plusDays(4);

        LeaveBalance johnSickBalance = leaveBalanceRepository
            .findByEmployeeIdAndLeaveTypeIdAndYear(employeeEmp.getId(), sick.getId(), currentYear).orElse(null);

        if (johnSickBalance != null) {
            johnSickBalance.setPendingDays(BigDecimal.valueOf(2));
            leaveBalanceRepository.save(johnSickBalance);
        }

        LeaveRequest pendingLeave = LeaveRequest.builder()
            .employee(employeeEmp)
            .leaveType(sick)
            .startDate(nextWeekStart)
            .endDate(nextWeekEnd)
            .totalDays(BigDecimal.valueOf(2))
            .reason("Medical check-up and dental appointment")
            .status(LeaveStatus.PENDING)
            .appliedAt(LocalDateTime.now().minusHours(4))
            .build();
        leaveRequestRepository.save(pendingLeave);

        // 9. Sample Today Check-in for John Doe
        Attendance johnAtt = Attendance.builder()
            .employee(employeeEmp)
            .date(LocalDate.now())
            .checkIn(LocalDateTime.now().minusHours(3))
            .status(AttendanceStatus.PRESENT)
            .notes("Working on ERP feature branch")
            .build();
        attendanceRepository.save(johnAtt);

        // 10. Notifications
        Notification n1 = Notification.builder()
            .recipient(managerUser)
            .title("New Leave Request for Approval")
            .message("John Doe requested 2 days of Sick Leave (" + nextWeekStart + " to " + nextWeekEnd + ")")
            .type(NotificationType.LEAVE_APPLIED)
            .entityType("LeaveRequest")
            .entityId(pendingLeave.getId())
            .isRead(false)
            .build();

        Notification n2 = Notification.builder()
            .recipient(employeeUser)
            .title("Welcome to ERP System")
            .message("Welcome John! Your ERP portal account is active. Check in daily to track working hours.")
            .type(NotificationType.GENERAL)
            .isRead(false)
            .build();

        notificationRepository.saveAll(List.of(n1, n2));

        log.info("--- Data Seeding Complete Successfully! ---");
    }
}
