import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Users, Building2, Calendar, Clock, AlertCircle, CheckCircle2,
  TrendingUp, UserCheck, UserX, ArrowRight
} from 'lucide-react'
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts'
import toast from 'react-hot-toast'
import dashboardService from '../../services/dashboardService'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate } from '../../utils/dateUtils'

const ATTENDANCE_COLORS = ['#10b981', '#6366f1', '#f59e0b', '#ef4444']

function AdminDashboard() {
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    dashboardService.getAdminDashboard()
      .then(res => setData(res))
      .catch(() => toast.error('Failed to load dashboard metrics'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="flex justify-center items-center py-24">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (!data) return null

  // Prepare chart data
  const attendanceChartData = [
    { name: 'Present', value: Number(data.todayPresent) || 0 },
    { name: 'On Leave', value: Number(data.todayOnLeave) || 0 },
    { name: 'Absent', value: Number(data.todayAbsent) || 0 },
  ].filter(d => d.value > 0)

  const deptChartData = (data.departmentStats || []).map(d => ({
    name: d.name,
    employees: d.count,
  }))

  return (
    <div className="space-y-6">
      {/* Top Welcome */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Executive Admin Dashboard</h1>
          <p className="page-subtitle">Real-time organizational headcount, attendance logs, and leave approval pipeline</p>
        </div>
        <div className="text-xs text-gray-500 font-semibold bg-white px-3 py-1.5 rounded-lg border border-gray-200">
          Today: {formatDate(new Date(), 'EEEE, dd MMMM yyyy')}
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Total Employees */}
        <div className="card-hover flex items-center gap-4 cursor-pointer" onClick={() => navigate('/employees')}>
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
            <Users size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Total Workforce</span>
            <span className="text-2xl font-black text-gray-900">{data.totalEmployees}</span>
            <span className="text-[11px] text-emerald-600 block mt-0.5 font-semibold">
              {data.activeEmployees} Active
            </span>
          </div>
        </div>

        {/* Departments */}
        <div className="card-hover flex items-center gap-4 cursor-pointer" onClick={() => navigate('/departments')}>
          <div className="p-3 bg-indigo-50 text-indigo-600 rounded-xl">
            <Building2 size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Departments</span>
            <span className="text-2xl font-black text-gray-900">{data.totalDepartments}</span>
            <span className="text-[11px] text-gray-400 block mt-0.5">Configured units</span>
          </div>
        </div>

        {/* Today's Attendance */}
        <div className="card-hover flex items-center gap-4 cursor-pointer" onClick={() => navigate('/attendance')}>
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
            <UserCheck size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Today Present</span>
            <span className="text-2xl font-black text-emerald-700">{data.todayPresent}</span>
            <span className="text-[11px] text-gray-400 block mt-0.5">
              {data.todayOnLeave} on leave
            </span>
          </div>
        </div>

        {/* Pending Approvals */}
        <div className="card-hover flex items-center gap-4 cursor-pointer" onClick={() => navigate('/leave/approvals')}>
          <div className="p-3 bg-amber-50 text-amber-600 rounded-xl">
            <Clock size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Pending Approvals</span>
            <span className="text-2xl font-black text-amber-600">{data.pendingLeaves}</span>
            <span className="text-[11px] text-amber-600/90 block mt-0.5 font-semibold">Action required</span>
          </div>
        </div>
      </div>

      {/* Analytics Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Department Distribution (Bar Chart) */}
        <div className="card lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-base font-bold text-gray-900">Headcount by Department</h3>
              <p className="text-xs text-gray-400">Employee allocation across departments</p>
            </div>
          </div>

          <div className="h-64 w-full pt-2">
            {deptChartData.length === 0 ? (
              <div className="h-full flex items-center justify-center text-xs text-gray-400">
                No department data available
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={deptChartData} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                  <XAxis dataKey="name" tick={{ fontSize: 11 }} interval={0} angle={-15} textAnchor="end" />
                  <YAxis tick={{ fontSize: 11 }} allowDecimals={false} />
                  <Tooltip
                    contentStyle={{ borderRadius: '8px', fontSize: '12px', border: '1px solid #e2e8f0' }}
                  />
                  <Bar dataKey="employees" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        {/* Attendance Breakdown (Pie Chart) */}
        <div className="card space-y-4">
          <div>
            <h3 className="text-base font-bold text-gray-900">Today's Attendance</h3>
            <p className="text-xs text-gray-400">Workforce distribution today</p>
          </div>

          <div className="h-64 w-full">
            {attendanceChartData.length === 0 ? (
              <div className="h-full flex items-center justify-center text-xs text-gray-400">
                No check-ins recorded today
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={attendanceChartData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={80}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {attendanceChartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={ATTENDANCE_COLORS[index % ATTENDANCE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ borderRadius: '8px', fontSize: '12px' }} />
                  <Legend verticalAlign="bottom" wrapperStyle={{ fontSize: '11px' }} />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>

      {/* Two Column Activity Feeds */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Leave Requests */}
        <div className="card space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-gray-900">Recent Leave Requests</h3>
            <button
              onClick={() => navigate('/leave/history')}
              className="text-xs text-primary-600 hover:text-primary-700 font-semibold flex items-center gap-1"
            >
              View all <ArrowRight size={13} />
            </button>
          </div>

          <div className="divide-y divide-gray-100">
            {data.recentLeaves?.length === 0 ? (
              <p className="text-xs text-gray-400 py-6 text-center">No recent leave requests</p>
            ) : (
              data.recentLeaves.map((l) => (
                <div key={l.id} className="py-3 flex items-center justify-between">
                  <div>
                    <div className="text-sm font-semibold text-gray-900">{l.employeeName}</div>
                    <div className="text-xs text-gray-400">
                      {l.leaveTypeName} • {formatDate(l.startDate)} ({l.totalDays} days)
                    </div>
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[11px] font-semibold ${
                    l.status === 'APPROVED' ? 'badge-green' :
                    l.status === 'PENDING' ? 'badge-yellow' :
                    l.status === 'MANAGER_APPROVED' ? 'badge-blue' :
                    'badge-red'
                  }`}>
                    {l.status}
                  </span>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Employees on Leave Today */}
        <div className="card space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-gray-900">Employees on Leave Today</h3>
            <span className="badge-purple font-bold">{data.employeesOnLeaveToday?.length || 0} away</span>
          </div>

          <div className="divide-y divide-gray-100">
            {data.employeesOnLeaveToday?.length === 0 ? (
              <p className="text-xs text-gray-400 py-6 text-center">No employees scheduled on leave today</p>
            ) : (
              data.employeesOnLeaveToday.map((emp) => (
                <div key={emp.id} className="py-3 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-purple-100 text-purple-700 font-bold text-xs flex items-center justify-center">
                      {emp.firstName?.[0]}{emp.lastName?.[0]}
                    </div>
                    <div>
                      <div className="text-sm font-semibold text-gray-900">{emp.fullName}</div>
                      <div className="text-xs text-gray-400">{emp.designation} • {emp.departmentName}</div>
                    </div>
                  </div>
                  <span className="text-xs text-purple-700 font-medium">On Leave</span>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default AdminDashboard
