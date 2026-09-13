import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Users, UserPlus, CheckSquare, Clock, ArrowRight, Building2, UserCheck } from 'lucide-react'
import toast from 'react-hot-toast'
import dashboardService from '../../services/dashboardService'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate } from '../../utils/dateUtils'

function HRDashboard() {
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    dashboardService.getHRDashboard()
      .then(res => setData(res))
      .catch(() => toast.error('Failed to load HR dashboard'))
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

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">HR Operations Dashboard</h1>
          <p className="page-subtitle">Workforce operations, headcount management, and policy compliance</p>
        </div>
        <button onClick={() => navigate('/employees/new')} className="btn-primary">
          <UserPlus size={16} /> New Onboarding
        </button>
      </div>

      {/* Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="card-hover flex items-center gap-4 cursor-pointer" onClick={() => navigate('/employees')}>
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
            <Users size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Total Employees</span>
            <span className="text-2xl font-black text-gray-900">{data.totalEmployees}</span>
            <span className="text-[11px] text-emerald-600 font-semibold">{data.activeEmployees} Active</span>
          </div>
        </div>

        <div className="card-hover flex items-center gap-4 cursor-pointer" onClick={() => navigate('/leave/approvals')}>
          <div className="p-3 bg-amber-50 text-amber-600 rounded-xl">
            <CheckSquare size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Awaiting Final HR Approval</span>
            <span className="text-2xl font-black text-amber-600">{data.pendingHRApprovals}</span>
            <span className="text-[11px] text-amber-600 font-semibold">Stage 2 approvals</span>
          </div>
        </div>

        <div className="card-hover flex items-center gap-4 cursor-pointer" onClick={() => navigate('/attendance')}>
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
            <UserCheck size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Present Today</span>
            <span className="text-2xl font-black text-emerald-700">{data.todayPresent}</span>
            <span className="text-[11px] text-gray-400">{data.todayOnLeave} on leave</span>
          </div>
        </div>

        <div className="card-hover flex items-center gap-4 cursor-pointer" onClick={() => navigate('/departments')}>
          <div className="p-3 bg-indigo-50 text-indigo-600 rounded-xl">
            <Building2 size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Departments</span>
            <span className="text-2xl font-black text-indigo-700">{data.departmentStats?.length || 0}</span>
            <span className="text-[11px] text-gray-400">Operating units</span>
          </div>
        </div>
      </div>

      {/* Grid: Departments and Recent Applications */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Department Headcounts */}
        <div className="card space-y-4">
          <h3 className="text-base font-bold text-gray-900">Department Workforce Distribution</h3>
          <div className="divide-y divide-gray-100">
            {data.departmentStats?.map((dept, idx) => (
              <div key={idx} className="py-2.5 flex items-center justify-between">
                <span className="text-sm font-medium text-gray-800">{dept.name}</span>
                <span className="badge-blue font-bold">{dept.count} staff</span>
              </div>
            ))}
          </div>
        </div>

        {/* Recent Applications */}
        <div className="card space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-gray-900">Recent Applications</h3>
            <button
              onClick={() => navigate('/leave/history')}
              className="text-xs text-primary-600 hover:text-primary-700 font-semibold flex items-center gap-1"
            >
              All requests <ArrowRight size={13} />
            </button>
          </div>

          <div className="divide-y divide-gray-100">
            {data.recentLeaveRequests?.length === 0 ? (
              <p className="text-xs text-gray-400 py-6 text-center">No applications</p>
            ) : (
              data.recentLeaveRequests.map((req) => (
                <div key={req.id} className="py-3 flex items-center justify-between">
                  <div>
                    <div className="text-sm font-semibold text-gray-900">{req.employeeName}</div>
                    <div className="text-xs text-gray-400">
                      {req.leaveTypeName} • {formatDate(req.startDate)} ({req.totalDays} days)
                    </div>
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[11px] font-semibold ${
                    req.status === 'APPROVED' ? 'badge-green' :
                    req.status === 'PENDING' ? 'badge-yellow' :
                    req.status === 'MANAGER_APPROVED' ? 'badge-blue' : 'badge-red'
                  }`}>
                    {req.status}
                  </span>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default HRDashboard
