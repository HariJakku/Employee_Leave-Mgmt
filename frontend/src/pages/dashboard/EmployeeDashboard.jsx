import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Calendar, Clock, Bell, PlusCircle, CheckCircle2,
  LogIn, LogOut, ArrowRight, UserCheck
} from 'lucide-react'
import toast from 'react-hot-toast'
import dashboardService from '../../services/dashboardService'
import attendanceService from '../../services/attendanceService'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate, formatTime } from '../../utils/dateUtils'

function EmployeeDashboard() {
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [attLoading, setAttLoading] = useState(false)

  const loadData = () => {
    dashboardService.getEmployeeDashboard()
      .then(res => setData(res))
      .catch(() => toast.error('Failed to load employee dashboard'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleQuickCheckIn = async () => {
    setAttLoading(true)
    try {
      await attendanceService.checkIn('Dashboard Quick Check-in')
      toast.success('Successfully checked in!')
      loadData()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Check-in failed')
    } finally {
      setAttLoading(false)
    }
  }

  const handleQuickCheckOut = async () => {
    setAttLoading(true)
    try {
      await attendanceService.checkOut('Dashboard Quick Check-out')
      toast.success('Successfully checked out!')
      loadData()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Check-out failed')
    } finally {
      setAttLoading(false)
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

  const profile = data.profile
  const todayAtt = data.todayAttendance

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="card bg-gradient-to-r from-primary-900 to-indigo-900 text-white p-6 sm:p-8">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-6">
          <div>
            <span className="text-xs font-semibold uppercase tracking-wider text-blue-300">Employee Portal</span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white mt-1">
              Welcome back, {profile.firstName}!
            </h1>
            <p className="text-blue-100/80 text-sm mt-1">
              {profile.designation || 'Team Member'} • {profile.departmentName || 'General Staff'} ({profile.employeeCode})
            </p>
          </div>

          {/* Quick Action Button */}
          <div className="flex gap-3">
            <button
              onClick={() => navigate('/leave/apply')}
              className="btn bg-white text-primary-900 hover:bg-blue-50 font-bold"
            >
              <PlusCircle size={16} /> Apply for Leave
            </button>
          </div>
        </div>
      </div>

      {/* Grid: Attendance Widget + Leave Balances */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Today's Attendance Widget */}
        <div className="card space-y-4 lg:col-span-1">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-gray-900 text-base flex items-center gap-2">
              <Clock size={18} className="text-primary-600" /> Today's Attendance
            </h3>
            <span className="text-xs text-gray-400">{formatDate(new Date(), 'dd MMM')}</span>
          </div>

          <div className="p-4 bg-gray-50 rounded-xl space-y-3">
            {!todayAtt ? (
              <div className="text-center py-2">
                <span className="text-xs text-gray-500 block mb-3">You have not clocked in for work today</span>
                <button
                  onClick={handleQuickCheckIn}
                  disabled={attLoading}
                  className="btn-success w-full text-xs font-bold py-2"
                >
                  <LogIn size={15} /> {attLoading ? 'Clocking in...' : 'Clock In Now'}
                </button>
              </div>
            ) : !todayAtt.checkOut ? (
              <div className="space-y-3">
                <div className="flex items-center justify-between text-xs">
                  <span className="text-gray-500">Clocked In:</span>
                  <span className="font-bold text-gray-800">{formatTime(todayAtt.checkIn)}</span>
                </div>
                <button
                  onClick={handleQuickCheckOut}
                  disabled={attLoading}
                  className="btn-danger w-full text-xs font-bold py-2"
                >
                  <LogOut size={15} /> {attLoading ? 'Clocking out...' : 'Clock Out Now'}
                </button>
              </div>
            ) : (
              <div className="text-center py-2 space-y-1">
                <span className="badge-green inline-flex items-center gap-1 text-xs">
                  <CheckCircle2 size={13} /> Completed Today
                </span>
                <p className="text-xs text-gray-600 font-semibold pt-1">
                  Worked {todayAtt.workingHours} hours
                </p>
                <p className="text-[11px] text-gray-400">
                  {formatTime(todayAtt.checkIn)} — {formatTime(todayAtt.checkOut)}
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Leave Balances Grid */}
        <div className="card lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-gray-900 text-base flex items-center gap-2">
              <Calendar size={18} className="text-primary-600" /> Leave Balances ({new Date().getFullYear()})
            </h3>
            <button
              onClick={() => navigate('/leave/balance')}
              className="text-xs text-primary-600 hover:text-primary-700 font-semibold flex items-center gap-1"
            >
              All balances <ArrowRight size={13} />
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            {data.leaveBalances?.slice(0, 3).map((b) => (
              <div key={b.id} className="p-3.5 bg-gray-50 rounded-xl border border-gray-100 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-gray-800 truncate">{b.leaveTypeName}</span>
                  <span className="text-xs font-extrabold text-primary-700 bg-blue-100/70 px-2 py-0.5 rounded">
                    {b.remainingDays} left
                  </span>
                </div>
                <div className="flex justify-between text-[11px] text-gray-400 pt-1 border-t border-gray-200/60">
                  <span>Quota: {b.allocatedDays}</span>
                  <span>Used: {b.usedDays}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Grid: Recent Leaves + Recent Notifications */}
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
                    <div className="text-sm font-semibold text-gray-900">{l.leaveTypeName}</div>
                    <div className="text-xs text-gray-400">
                      {formatDate(l.startDate)} - {formatDate(l.endDate)} ({l.totalDays} days)
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

        {/* Recent Notifications */}
        <div className="card space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-gray-900">Recent Notifications</h3>
            <button
              onClick={() => navigate('/notifications')}
              className="text-xs text-primary-600 hover:text-primary-700 font-semibold flex items-center gap-1"
            >
              View all <ArrowRight size={13} />
            </button>
          </div>

          <div className="divide-y divide-gray-100">
            {data.recentNotifications?.length === 0 ? (
              <p className="text-xs text-gray-400 py-6 text-center">No notifications</p>
            ) : (
              data.recentNotifications.map((n) => (
                <div key={n.id} className="py-3">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-gray-900">{n.title}</span>
                    <span className="text-[10px] text-gray-400">{formatDate(n.createdAt)}</span>
                  </div>
                  <p className="text-xs text-gray-500 mt-0.5 line-clamp-1">{n.message}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default EmployeeDashboard
