import { lazy, Suspense } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import PrivateRoute from './PrivateRoute'
import RoleRoute from './RoleRoute'
import LoadingSpinner from '../components/common/LoadingSpinner'
import MainLayout from '../layouts/MainLayout'
import { ROLES } from '../utils/constants'

// ===== LAZY-LOADED PAGES =====
// Auth
const Login          = lazy(() => import('../pages/auth/Login'))
const Register       = lazy(() => import('../pages/auth/Register'))
const ChangePassword = lazy(() => import('../pages/auth/ChangePassword'))
const Unauthorized   = lazy(() => import('../pages/auth/Unauthorized'))

// Dashboards
const AdminDashboard    = lazy(() => import('../pages/dashboard/AdminDashboard'))
const HRDashboard       = lazy(() => import('../pages/dashboard/HRDashboard'))
const ManagerDashboard  = lazy(() => import('../pages/dashboard/ManagerDashboard'))
const EmployeeDashboard = lazy(() => import('../pages/dashboard/EmployeeDashboard'))

// Employee
const EmployeeList   = lazy(() => import('../pages/employee/EmployeeList'))
const EmployeeForm   = lazy(() => import('../pages/employee/EmployeeForm'))
const EmployeeDetail = lazy(() => import('../pages/employee/EmployeeDetail'))

// Department
const DepartmentList = lazy(() => import('../pages/department/DepartmentList'))

// Leave
const LeaveTypes    = lazy(() => import('../pages/leave/LeaveTypes'))
const LeaveBalance  = lazy(() => import('../pages/leave/LeaveBalance'))
const ApplyLeave    = lazy(() => import('../pages/leave/ApplyLeave'))
const LeaveHistory  = lazy(() => import('../pages/leave/LeaveHistory'))
const LeaveApproval = lazy(() => import('../pages/leave/LeaveApproval'))

// Attendance
const AttendancePage = lazy(() => import('../pages/attendance/AttendancePage'))

// Notifications
const NotificationsPage = lazy(() => import('../pages/notifications/NotificationsPage'))

// Audit
const AuditLogsPage = lazy(() => import('../pages/audit/AuditLogsPage'))

// AI
const AIAssistantPage = lazy(() => import('../pages/ai/AIAssistantPage'))

// Profile
const ProfilePage = lazy(() => import('../pages/profile/ProfilePage'))

// ===== SMART DASHBOARD REDIRECT =====
// Routes /dashboard to the correct dashboard based on role
function DashboardRedirect() {
  const { role } = useAuth()
  const map = {
    [ROLES.ADMIN]:    '/dashboard/admin',
    [ROLES.HR]:       '/dashboard/hr',
    [ROLES.MANAGER]:  '/dashboard/manager',
    [ROLES.EMPLOYEE]: '/dashboard/employee',
  }
  return <Navigate to={map[role] || '/login'} replace />
}

// ===== SUSPENSE WRAPPER =====
function PageSuspense({ children }) {
  return (
    <Suspense
      fallback={
        <div className="flex items-center justify-center min-h-[60vh]">
          <LoadingSpinner size="lg" />
        </div>
      }
    >
      {children}
    </Suspense>
  )
}

function AppRouter() {
  return (
    <Routes>
      {/* ===== PUBLIC ROUTES ===== */}
      <Route path="/login"        element={<PageSuspense><Login /></PageSuspense>} />
      <Route path="/register"     element={<PageSuspense><Register /></PageSuspense>} />
      <Route path="/unauthorized" element={<PageSuspense><Unauthorized /></PageSuspense>} />

      {/* ===== PROTECTED ROUTES (inside MainLayout) ===== */}
      <Route
        path="/"
        element={
          <PrivateRoute>
            <MainLayout />
          </PrivateRoute>
        }
      >
        {/* Smart dashboard redirect */}
        <Route index element={<DashboardRedirect />} />
        <Route path="dashboard" element={<DashboardRedirect />} />

        {/* Role-specific dashboards */}
        <Route path="dashboard/admin"
          element={
            <RoleRoute roles={[ROLES.ADMIN]}>
              <PageSuspense><AdminDashboard /></PageSuspense>
            </RoleRoute>
          }
        />
        <Route path="dashboard/hr"
          element={
            <RoleRoute roles={[ROLES.HR]}>
              <PageSuspense><HRDashboard /></PageSuspense>
            </RoleRoute>
          }
        />
        <Route path="dashboard/manager"
          element={
            <RoleRoute roles={[ROLES.MANAGER]}>
              <PageSuspense><ManagerDashboard /></PageSuspense>
            </RoleRoute>
          }
        />
        <Route path="dashboard/employee"
          element={
            <RoleRoute roles={[ROLES.EMPLOYEE]}>
              <PageSuspense><EmployeeDashboard /></PageSuspense>
            </RoleRoute>
          }
        />

        {/* Employees */}
        <Route path="employees"
          element={
            <RoleRoute roles={[ROLES.ADMIN, ROLES.HR, ROLES.MANAGER]}>
              <PageSuspense><EmployeeList /></PageSuspense>
            </RoleRoute>
          }
        />
        <Route path="employees/new"
          element={
            <RoleRoute roles={[ROLES.ADMIN, ROLES.HR]}>
              <PageSuspense><EmployeeForm /></PageSuspense>
            </RoleRoute>
          }
        />
        <Route path="employees/:id/edit"
          element={
            <RoleRoute roles={[ROLES.ADMIN, ROLES.HR]}>
              <PageSuspense><EmployeeForm /></PageSuspense>
            </RoleRoute>
          }
        />
        <Route path="employees/:id"
          element={<PageSuspense><EmployeeDetail /></PageSuspense>}
        />

        {/* Departments */}
        <Route path="departments"
          element={
            <RoleRoute roles={[ROLES.ADMIN, ROLES.HR]}>
              <PageSuspense><DepartmentList /></PageSuspense>
            </RoleRoute>
          }
        />

        {/* Leave */}
        <Route path="leave/types"
          element={
            <RoleRoute roles={[ROLES.ADMIN, ROLES.HR]}>
              <PageSuspense><LeaveTypes /></PageSuspense>
            </RoleRoute>
          }
        />
        <Route path="leave/balance"    element={<PageSuspense><LeaveBalance /></PageSuspense>} />
        <Route path="leave/apply"      element={<PageSuspense><ApplyLeave /></PageSuspense>} />
        <Route path="leave/history"    element={<PageSuspense><LeaveHistory /></PageSuspense>} />
        <Route path="leave/approvals"
          element={
            <RoleRoute roles={[ROLES.ADMIN, ROLES.HR, ROLES.MANAGER]}>
              <PageSuspense><LeaveApproval /></PageSuspense>
            </RoleRoute>
          }
        />

        {/* Attendance */}
        <Route path="attendance" element={<PageSuspense><AttendancePage /></PageSuspense>} />

        {/* Notifications */}
        <Route path="notifications" element={<PageSuspense><NotificationsPage /></PageSuspense>} />

        {/* Audit Logs */}
        <Route path="audit-logs"
          element={
            <RoleRoute roles={[ROLES.ADMIN]}>
              <PageSuspense><AuditLogsPage /></PageSuspense>
            </RoleRoute>
          }
        />

        {/* AI Assistant */}
        <Route path="ai-assistant" element={<PageSuspense><AIAssistantPage /></PageSuspense>} />

        {/* Profile */}
        <Route path="profile"          element={<PageSuspense><ProfilePage /></PageSuspense>} />
        <Route path="change-password"  element={<PageSuspense><ChangePassword /></PageSuspense>} />
      </Route>

      {/* 404 fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default AppRouter
