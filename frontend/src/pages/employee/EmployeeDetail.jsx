import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Edit2, Mail, Phone, Calendar, Building2, User, Award, Shield, Users } from 'lucide-react'
import toast from 'react-hot-toast'
import employeeService from '../../services/employeeService'
import { useAuth } from '../../context/AuthContext'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate } from '../../utils/dateUtils'
import { ROLES, EMPLOYEE_STATUS } from '../../utils/constants'

function EmployeeDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { role, user } = useAuth()
  const canEdit = [ROLES.ADMIN, ROLES.HR].includes(role)

  const [employee, setEmployee] = useState(null)
  const [leaveBalances, setLeaveBalances] = useState([])
  const [subordinates, setSubordinates] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchAllData = async () => {
      setLoading(true)
      try {
        const [emp, balances, team] = await Promise.all([
          employeeService.getEmployeeById(id),
          employeeService.getEmployeeLeaveBalances(id),
          employeeService.getTeam(id).catch(() => []),
        ])
        setEmployee(emp)
        setLeaveBalances(balances || [])
        setSubordinates(team || [])
      } catch (err) {
        toast.error('Failed to load employee details')
        navigate('/employees')
      } finally {
        setLoading(false)
      }
    }

    fetchAllData()
  }, [id, navigate])

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (!employee) return null

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Top Bar */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/employees')}
          className="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900"
        >
          <ArrowLeft size={16} /> Back to Employees
        </button>

        {canEdit && (
          <button
            onClick={() => navigate(`/employees/${employee.id}/edit`)}
            className="btn-primary btn-sm"
          >
            <Edit2 size={14} /> Edit Profile
          </button>
        )}
      </div>

      {/* Main Profile Header Card */}
      <div className="card bg-gradient-to-r from-blue-900 to-indigo-900 text-white p-8">
        <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6">
          <div className="w-24 h-24 rounded-2xl bg-white/10 backdrop-blur border border-white/20 flex items-center justify-center text-3xl font-extrabold text-white shadow-lg">
            {employee.firstName?.[0]}{employee.lastName?.[0]}
          </div>

          <div className="flex-1 text-center sm:text-left space-y-2">
            <div className="flex flex-col sm:flex-row sm:items-center gap-3">
              <h1 className="text-2xl sm:text-3xl font-bold">{employee.fullName}</h1>
              <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${
                employee.status === EMPLOYEE_STATUS.ACTIVE ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30' :
                employee.status === EMPLOYEE_STATUS.INACTIVE ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30' :
                'bg-red-500/20 text-red-300 border border-red-500/30'
              }`}>
                {employee.status}
              </span>
            </div>

            <p className="text-blue-200 text-base font-medium">
              {employee.designation || 'Staff'} • {employee.departmentName || 'No Department'}
            </p>

            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-4 pt-2 text-sm text-blue-100/80">
              <span className="flex items-center gap-1.5">
                <Shield size={16} className="text-blue-300" /> {employee.employeeCode}
              </span>
              <span className="flex items-center gap-1.5">
                <Mail size={16} className="text-blue-300" /> {employee.email}
              </span>
              {employee.phone && (
                <span className="flex items-center gap-1.5">
                  <Phone size={16} className="text-blue-300" /> {employee.phone}
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Grid: Details & Balances */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Personal & Job Details */}
        <div className="space-y-6 lg:col-span-1">
          {/* Job Overview */}
          <div className="card space-y-4">
            <h3 className="font-bold text-gray-900 text-sm uppercase tracking-wider text-gray-500">
              Job Information
            </h3>
            <div className="space-y-3 text-sm">
              <div>
                <span className="text-gray-500 block text-xs">Department</span>
                <span className="font-medium text-gray-800">{employee.departmentName || '—'}</span>
              </div>
              <div>
                <span className="text-gray-500 block text-xs">Designation</span>
                <span className="font-medium text-gray-800">{employee.designation || '—'}</span>
              </div>
              <div>
                <span className="text-gray-500 block text-xs">Reporting Manager</span>
                <span className="font-medium text-gray-800">{employee.managerName || 'None (Top Level)'}</span>
              </div>
              <div>
                <span className="text-gray-500 block text-xs">Employment Type</span>
                <span className="font-medium text-gray-800">{employee.employmentType || '—'}</span>
              </div>
              <div>
                <span className="text-gray-500 block text-xs">Joining Date</span>
                <span className="font-medium text-gray-800">{formatDate(employee.joiningDate)}</span>
              </div>
              {canEdit && employee.salary && (
                <div>
                  <span className="text-gray-500 block text-xs">Annual Compensation</span>
                  <span className="font-semibold text-emerald-700">${Number(employee.salary).toLocaleString()}</span>
                </div>
              )}
            </div>
          </div>

          {/* Personal Info */}
          <div className="card space-y-4">
            <h3 className="font-bold text-gray-900 text-sm uppercase tracking-wider text-gray-500">
              Personal Information
            </h3>
            <div className="space-y-3 text-sm">
              <div>
                <span className="text-gray-500 block text-xs">Gender</span>
                <span className="font-medium text-gray-800">{employee.gender || '—'}</span>
              </div>
              <div>
                <span className="text-gray-500 block text-xs">Date of Birth</span>
                <span className="font-medium text-gray-800">{formatDate(employee.dateOfBirth)}</span>
              </div>
              <div>
                <span className="text-gray-500 block text-xs">Address</span>
                <span className="font-medium text-gray-800">{employee.address || '—'}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Leave Balances & Team */}
        <div className="space-y-6 lg:col-span-2">
          {/* Leave Balances Card */}
          <div className="card">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-base font-bold text-gray-900">
                Current Year Leave Balances ({new Date().getFullYear()})
              </h3>
            </div>

            {leaveBalances.length === 0 ? (
              <p className="text-sm text-gray-500 py-4">No active leave balances assigned.</p>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {leaveBalances.map((lb) => (
                  <div key={lb.id} className="p-4 rounded-xl border border-gray-100 bg-gray-50/50 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-gray-800 text-sm">{lb.leaveTypeName}</span>
                      <span className="text-xs px-2 py-0.5 rounded font-semibold bg-blue-100 text-blue-700">
                        {lb.remainingDays} left
                      </span>
                    </div>

                    <div className="grid grid-cols-3 gap-2 text-center pt-2 border-t border-gray-200/60 text-xs">
                      <div>
                        <span className="text-gray-400 block">Allocated</span>
                        <span className="font-semibold text-gray-700">{lb.allocatedDays}</span>
                      </div>
                      <div>
                        <span className="text-gray-400 block">Used</span>
                        <span className="font-semibold text-amber-600">{lb.usedDays}</span>
                      </div>
                      <div>
                        <span className="text-gray-400 block">Pending</span>
                        <span className="font-semibold text-blue-600">{lb.pendingDays}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Direct Subordinates if Manager */}
          {subordinates.length > 0 && (
            <div className="card">
              <div className="flex items-center gap-2 mb-4">
                <Users size={18} className="text-primary-600" />
                <h3 className="text-base font-bold text-gray-900">
                  Direct Reports ({subordinates.length})
                </h3>
              </div>

              <div className="divide-y divide-gray-100">
                {subordinates.map((sub) => (
                  <div
                    key={sub.id}
                    onClick={() => navigate(`/employees/${sub.id}`)}
                    className="py-3 flex items-center justify-between hover:bg-gray-50 px-2 rounded-lg cursor-pointer transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-primary-50 text-primary-700 font-bold text-xs flex items-center justify-center">
                        {sub.firstName?.[0]}{sub.lastName?.[0]}
                      </div>
                      <div>
                        <div className="text-sm font-semibold text-gray-900">{sub.fullName}</div>
                        <div className="text-xs text-gray-400">{sub.designation} • {sub.departmentName}</div>
                      </div>
                    </div>
                    <span className="text-xs text-gray-500">{sub.employeeCode}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default EmployeeDetail
