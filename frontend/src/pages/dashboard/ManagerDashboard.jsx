import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Users, Clock, UserCheck, CalendarX, ArrowRight, Check, X } from 'lucide-react'
import toast from 'react-hot-toast'
import dashboardService from '../../services/dashboardService'
import leaveService from '../../services/leaveService'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate, formatTime } from '../../utils/dateUtils'

function ManagerDashboard() {
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadData = () => {
    dashboardService.getManagerDashboard()
      .then(res => setData(res))
      .catch(() => toast.error('Failed to load manager dashboard'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleQuickApprove = async (id) => {
    try {
      await leaveService.approveLeave(id, 'Approved by manager')
      toast.success('Leave request approved!')
      loadData()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Approval failed')
    }
  }

  const handleQuickReject = async (id) => {
    const reason = window.prompt('Please provide a reason for rejection:')
    if (!reason) return
    try {
      await leaveService.rejectLeave(id, reason)
      toast.success('Leave request rejected')
      loadData()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Rejection failed')
    }
  }

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
          <h1 className="page-title">Manager Team Dashboard</h1>
          <p className="page-subtitle">Track team capacity, approve leave applications, and supervise daily attendance</p>
        </div>
      </div>

      {/* Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="card-hover flex items-center gap-4">
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
            <Users size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Team Size</span>
            <span className="text-2xl font-black text-gray-900">{data.teamSize}</span>
            <span className="text-[11px] text-gray-400">Direct reports</span>
          </div>
        </div>

        <div className="card-hover flex items-center gap-4 cursor-pointer" onClick={() => navigate('/leave/approvals')}>
          <div className="p-3 bg-amber-50 text-amber-600 rounded-xl">
            <Clock size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Pending Approvals</span>
            <span className="text-2xl font-black text-amber-600">{data.pendingApprovalsCount}</span>
            <span className="text-[11px] text-amber-600 font-semibold">Awaiting your review</span>
          </div>
        </div>

        <div className="card-hover flex items-center gap-4">
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
            <UserCheck size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">Present Today</span>
            <span className="text-2xl font-black text-emerald-700">{data.teamPresentToday}</span>
            <span className="text-[11px] text-gray-400">Checked in</span>
          </div>
        </div>

        <div className="card-hover flex items-center gap-4">
          <div className="p-3 bg-purple-50 text-purple-600 rounded-xl">
            <CalendarX size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-500 font-medium block">On Leave Today</span>
            <span className="text-2xl font-black text-purple-700">{data.teamMembersOnLeaveToday}</span>
            <span className="text-[11px] text-purple-600">Scheduled away</span>
          </div>
        </div>
      </div>

      {/* Pending Approvals Table */}
      <div className="card p-0 overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-gray-900">Pending Team Leave Requests</h3>
            <p className="text-xs text-gray-400">Review requests from your team members</p>
          </div>
          <button
            onClick={() => navigate('/leave/approvals')}
            className="text-xs text-primary-600 hover:text-primary-700 font-semibold flex items-center gap-1"
          >
            Manage all <ArrowRight size={13} />
          </button>
        </div>

        {data.pendingApprovals?.length === 0 ? (
          <p className="text-xs text-gray-400 py-8 text-center">No pending leave requests for your team</p>
        ) : (
          <div className="table-container border-0">
            <table className="table">
              <thead>
                <tr>
                  <th>Team Member</th>
                  <th>Leave Type</th>
                  <th>Dates</th>
                  <th>Days</th>
                  <th>Reason</th>
                  <th className="text-right">Quick Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data.pendingApprovals.map((req) => (
                  <tr key={req.id}>
                    <td>
                      <div className="font-semibold text-gray-900">{req.employeeName}</div>
                      <div className="text-xs text-gray-400">{req.employeeCode}</div>
                    </td>
                    <td>{req.leaveTypeName}</td>
                    <td>{formatDate(req.startDate)} - {formatDate(req.endDate)}</td>
                    <td className="font-bold">{req.totalDays}</td>
                    <td className="text-xs text-gray-500 max-w-[200px] truncate">{req.reason}</td>
                    <td className="text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => handleQuickApprove(req.id)}
                          className="btn-success btn-sm text-xs py-1 px-2"
                        >
                          <Check size={13} /> Approve
                        </button>
                        <button
                          onClick={() => handleQuickReject(req.id)}
                          className="btn-danger btn-sm text-xs py-1 px-2"
                        >
                          <X size={13} /> Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Team Attendance Today */}
      <div className="card p-0 overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex items-center justify-between">
          <h3 className="text-base font-bold text-gray-900">Today's Team Attendance Status</h3>
          <button
            onClick={() => navigate('/attendance')}
            className="text-xs text-primary-600 hover:text-primary-700 font-semibold flex items-center gap-1"
          >
            Attendance Logs <ArrowRight size={13} />
          </button>
        </div>

        {data.todayTeamAttendance?.length === 0 ? (
          <p className="text-xs text-gray-400 py-8 text-center">No team members checked in today</p>
        ) : (
          <div className="divide-y divide-gray-100">
            {data.todayTeamAttendance.map((att) => (
              <div key={att.id} className="p-4 flex items-center justify-between">
                <div>
                  <div className="font-semibold text-gray-900 text-sm">{att.employeeName}</div>
                  <div className="text-xs text-gray-400">{att.employeeCode}</div>
                </div>

                <div className="text-right">
                  <span className="text-xs text-gray-600 block">
                    In: {formatTime(att.checkIn)} {att.checkOut ? `• Out: ${formatTime(att.checkOut)}` : ''}
                  </span>
                  <span className="text-[11px] font-bold text-emerald-600">
                    {att.workingHours ? `${att.workingHours} hrs` : 'Working'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default ManagerDashboard
