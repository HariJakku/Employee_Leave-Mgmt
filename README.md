# Employee Management & Leave Management ERP System

An enterprise-grade, full-stack ERP web application engineered with **Spring Boot 3 (Java 21)**, **React 18 + Vite**, **MySQL 8**, and **Google Gemini AI**. Designed and architected to mirror real-world corporate intranet ERP software with multi-tier role-based access control, hierarchical leave approval workflows, daily attendance tracking, and AI-assisted human resource operations.

---

## 🏗️ Architecture & Technology Stack

### Backend Architecture
```
Controller (REST Endpoints & OpenAPI Documentation)
   │
   ▼
Service Layer (Business Rules, Approval State Machine & LLM Integration)
   │
   ▼
Repository Layer (Spring Data JPA & Dynamic Specifications)
   │
   ▼
MySQL Database (Relational schema with indexing and auditing)
```

- **Runtime & Framework**: Java 21 LTS, Spring Boot 3.2.5
- **Security**: Spring Security 6, Stateless JWT Authentication (JJWT 0.12.3), BCrypt hashing (strength 12), Method-level security (`@PreAuthorize`)
- **Database & ORM**: MySQL 8.x, Spring Data JPA, Hibernate, Automatic JPA Auditing (`@CreatedDate`, `@LastModifiedDate`)
- **AI / LLM**: Google Gemini 1.5 Flash via REST WebFlux `WebClient` with ground-truth DB context injection and offline rule-based fallback
- **API Documentation**: SpringDoc OpenAPI 2.3.0, Swagger UI
- **Testing**: JUnit 5, Mockito

### Frontend Architecture
- **Framework & Tooling**: React 18, Vite 5, React Router v6
- **Styling**: Tailwind CSS 3 with custom enterprise color palette and UI component tokens
- **Data Visualization**: Recharts (Bar charts, Donut charts)
- **HTTP Client**: Axios with automatic JWT Bearer token injection and global 401/403/500 interceptors
- **Icons & Notifications**: Lucide React, react-hot-toast, date-fns

---

## 👥 Role-Based Access Control (RBAC)

The system enforces 4 distinct organizational roles across both backend endpoints and frontend navigation:

| Role | Responsibilities | Dashboard Features |
|------|-----------------|--------------------|
| **ADMIN** | Full system control, employee management, department creation, leave policy quotas, system audit logs | Total workforce headcount, department distribution bar chart, attendance breakdown, leave pipeline |
| **HR** | Workforce lifecycle, onboarding, policy compliance, final stage leave authorization | HR operations, new hires, stage-2 leave approvals, department rosters, AI HR Copilot |
| **MANAGER** | Team capacity management, stage-1 team leave approval/rejection, team attendance monitoring | Direct reports capacity, instant approve/reject team leave actions, live shift tracking |
| **EMPLOYEE** | Self-service portal, apply for leaves, view balances, daily clock-in/out, notification inbox | One-click clock in/out widget, annual leave balance breakdown, recent application tracker |

---

## 📐 Core Business Rules Implemented

1. **Overlap Prevention**: An employee cannot apply for overlapping pending or approved leave requests.
2. **Post-Approval Deduction**: Leave balance is **never** deducted while a request is in `PENDING` or `MANAGER_APPROVED` state. Only when final approval is granted are days moved from `pendingDays` to `usedDays`.
3. **Quota Protection on Rejection**: Rejected leave requests immediately revert reserved `pendingDays` without touching the employee's remaining quota.
4. **Cancellation Balance Restoration**: Employees cancelling an approved leave will automatically have their quota restored back to their available balance.
5. **Anti-Self-Approval**: An employee or manager cannot approve or reject their own leave requests.
6. **Hierarchical Team Scoping**: Managers are strictly limited to reviewing and approving leave requests submitted by their direct subordinates.
7. **Single Daily Attendance**: Employees can only clock in once per calendar day. Clocking out automatically calculates working hours; shifts under 4 hours are classified as `HALF_DAY`.
8. **Account Deactivation Enforced**: Deactivating/terminating an employee profile immediately locks their user account and blocks further logins.

---

## 🤖 AI / LLM Capabilities (Gemini 1.5 Flash)

1. **AI Leave Policy Advisor**: Employees can ask natural questions (*"What is my remaining sick leave balance?", "Can I take leave next Monday?"*). The backend injects the employee's live DB balances and company leave rules into the prompt for grounded answers.
2. **Natural Language Leave Request Generator**: Employees can describe leave plans casually (*"I have a family wedding next Monday to Wednesday"*). The LLM extracts the leave type, start/end dates, and days count into a structured draft for review.
3. **AI HR Analytics Copilot**: HR and leadership can query company-wide workforce presence (*"How many employees are on leave today?", "Department leave activity summary"*).
4. **AI Employee Profiler**: Generates an executive tenure, department contribution, and leave history summary for employee reviews.

---

## 🔑 Pre-Seeded Demo Accounts

The application automatically seeds standard accounts on first run:

| Role | Email | Password |
|------|-------|----------|
| **ADMIN** | `admin@erp.com` | `Admin@123` |
| **HR** | `hr@erp.com` | `Hr@123` |
| **MANAGER** | `manager@erp.com` | `Manager@123` |
| **EMPLOYEE** | `john.doe@erp.com` | `Employee@123` |

---

## 🚀 Getting Started

### Option 1: Using Docker Compose (Recommended)

Run the entire system (MySQL 8, Spring Boot backend, and React frontend) with a single command:

```bash
docker-compose up -d
```

- **Frontend Application**: `http://localhost:3000` (or `http://localhost:5173` in local dev)
- **Backend REST API**: `http://localhost:8080`
- **Swagger OpenAPI Docs**: `http://localhost:8080/swagger-ui.html`
- **MySQL Database**: `localhost:3306` (`employee_erp` / `root` / `root`)

---

### Option 2: Running Locally

#### 1. Prerequisites
- **Java 21 LTS**
- **Maven 3.8+**
- **Node.js 18+ & npm**
- **MySQL 8.x** running on port 3306

#### 2. Database Setup
Create the MySQL database:
```sql
CREATE DATABASE employee_erp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 3. Backend Setup
```bash
cd backend

# Optional: Set Gemini API key for AI features (rule fallback used if empty)
export AI_API_KEY="your-gemini-api-key_"

# Run Spring Boot application
mvn spring-boot:run
```
Backend will start on `http://localhost:8080`. Demo seed data is automatically populated on first run.

#### 4. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Start Vite development server with proxy to backend
npm run dev
```
Open `http://localhost:5173` in your browser.

---

## 🧪 Running Automated Tests

Run backend unit and integration test suites:

```bash
cd backend
mvn test
```

Test coverage includes:
- `AuthServiceTest`: Login verification, JWT issuance, bad credential handling
- `EmployeeServiceTest`: Creation, duplicate email prevention, employee lookup
- `LeaveRequestServiceTest`: Balance validation, date boundaries, overlapping leaves
- `LeaveApprovalServiceTest`: Manager approval, self-approval prevention, rejection balance restoration
- `AttendanceServiceTest`: Check-in, duplicate check-in prevention, checkout duration calculation

---

## 📬 Postman API Collection

A complete Postman collection is included in:
`postman/Employee-Leave-ERP.postman_collection.json`

### Key features:
- **Automatic Token Handling**: Logging in automatically sets the `{{token}}` collection variable.
- **Over 40 Pre-configured Requests**: Covering Auth, Employees, Departments, Leave Types, Leaves, Approvals, Attendance, Dashboards, and AI endpoints.

To import:
1. Open Postman.
2. Click **Import** -> Select `postman/Employee-Leave-ERP.postman_collection.json`.
3. Run `1. Authentication` -> `Login - Admin`.
4. All subsequent requests will automatically use the captured Bearer token!

---

## 📖 API Documentation (Swagger)

Interactive Swagger UI documentation is available at:
`http://localhost:8080/swagger-ui.html`

OpenAPI JSON specification:
`http://localhost:8080/v3/api-docs`
