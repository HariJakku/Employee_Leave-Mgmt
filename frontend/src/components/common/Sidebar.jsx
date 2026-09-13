import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { ROLES } from '../../utils/constants'
import clsx from 'clsx'
import {
  LayoutDashboard, Users, Building2, CalendarDays, Clock,
  Bell, FileText, Bot, User, ChevronDown, LogOut, Shield,
  CheckSquare, ListChecks
} from 'lucide-react'

/**
 * Sidebar navigation — role-aware menu items.
 * On mobile, renders as an overlay controlled by `open` prop.
 */
function Sidebar({ open, onClose }) {
  const { role, user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  // ===== NAV ITEMS per role =====
  const navItems = [
    // Dashboard (everyone, but points to role-specific page)
    {
      label: 'Dashboard',
      icon: LayoutDashboard,
      to: '/dashboard',
      roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER, ROLES.EMPLOYEE],
    },

    // ── SECTION: People ──
    { type: 'section', label: 'People Management', roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER] },
    {
      label: 'Employees',
      icon: Users,
      to: '/employees',
      roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER],
    },
    {
      label: 'Departments',
      icon: Building2,
      to: '/departments',
      roles: [ROLES.ADMIN, ROLES.HR],
    },

    // ── SECTION: Leave ──
    { type: 'section', label: 'Leave Management', roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER, ROLES.EMPLOYEE] },
    {
      label: 'My Leave Balance',
      icon: CalendarDays,
      to: '/leave/balance',
      roles: [ROLES.EMPLOYEE],
    },
    {
      label: 'Apply Leave',
      icon: CalendarDays,
      to: '/leave/apply',
      roles: [ROLES.EMPLOYEE, ROLES.HR],
    },
    {
      label: 'Leave History',
      icon: ListChecks,
      to: '/leave/history',
      roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER, ROLES.EMPLOYEE],
    },
    {
      label: 'Leave Approvals',
      icon: CheckSquare,
      to: '/leave/approvals',
      roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER],
    },
    {
      label: 'Leave Types',
      icon: FileText,
      to: '/leave/types',
      roles: [ROLES.ADMIN, ROLES.HR],
    },

    // ── SECTION: Attendance ──
    { type: 'section', label: 'Attendance', roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER, ROLES.EMPLOYEE] },
    {
      label: 'Attendance',
      icon: Clock,
      to: '/attendance',
      roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER, ROLES.EMPLOYEE],
    },

    // ── SECTION: System ──
    { type: 'section', label: 'System', roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER, ROLES.EMPLOYEE] },
    {
      label: 'Notifications',
      icon: Bell,
      to: '/notifications',
      roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER, ROLES.EMPLOYEE],
    },
    {
      label: 'AI Assistant',
      icon: Bot,
      to: '/ai-assistant',
      roles: [ROLES.ADMIN, ROLES.HR, ROLES.MANAGER, ROLES.EMPLOYEE],
    },
    {
      label: 'Audit Logs',
      icon: Shield,
      to: '/audit-logs',
      roles: [ROLES.ADMIN],
    },
  ]

  const filteredItems = navItems.filter(item =>
    item.roles?.includes(role)
  )

  return (
    <>
      {/* Mobile overlay */}
      {open && (
        <div
          className="fixed inset-0 bg-black/50 z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={clsx(
          'fixed top-0 left-0 z-50 h-full w-64 bg-sidebar-bg flex flex-col',
          'transform transition-transform duration-300 ease-in-out',
          'lg:static lg:translate-x-0 lg:flex',
          open ? 'translate-x-0' : '-translate-x-full',
        )}
      >
        {/* Logo */}
        <div className="flex items-center gap-3 px-6 py-5 border-b border-white/10">
          <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
            <Building2 size={18} className="text-white" />
          </div>
          <div>
            <p className="text-white font-semibold text-sm leading-tight">ERP System</p>
            <p className="text-sidebar-text text-xs">{role}</p>
          </div>
        </div>

        {/* Nav items */}
        <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-0.5">
          {filteredItems.map((item, idx) => {
            if (item.type === 'section') {
              return (
                <p
                  key={idx}
                  className="text-xs font-semibold text-gray-500 uppercase tracking-widest
                             px-3 pt-4 pb-1 mt-2"
                >
                  {item.label}
                </p>
              )
            }

            const Icon = item.icon
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={onClose}
                className={({ isActive }) =>
                  clsx('sidebar-link', isActive && 'active')
                }
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            )
          })}
        </nav>

        {/* User profile footer */}
        <div className="border-t border-white/10 p-4">
          <NavLink
            to="/profile"
            className="flex items-center gap-3 p-2 rounded-lg hover:bg-sidebar-hover
                       transition-colors duration-200"
          >
            <div className="w-8 h-8 bg-primary-600 rounded-full flex items-center justify-center">
              <User size={16} className="text-white" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-white text-xs font-medium truncate">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="text-sidebar-text text-xs truncate">{user?.email}</p>
            </div>
          </NavLink>
          <button
            onClick={handleLogout}
            className="mt-2 w-full flex items-center gap-2 px-3 py-2 rounded-lg
                       text-sidebar-text hover:text-red-400 hover:bg-red-900/20
                       text-sm transition-colors duration-200"
          >
            <LogOut size={16} />
            Sign out
          </button>
        </div>
      </aside>
    </>
  )
}

export default Sidebar
