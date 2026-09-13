import { useState, useEffect, useCallback } from 'react'
import { Clock, LogIn, LogOut, Calendar, Users, CheckCircle2, AlertCircle } from 'lucide-react'
import toast from 'react-hot-toast'
import attendanceService from '../../services/attendanceService'
import { useAuth } from '../../context/AuthContext'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate, formatTime, formatWorkingHours, todayISO } from '../../utils/dateUtils'
import { ROLES, ATTENDANCE_STATUS } from '../../utils/constants'

function AttendancePage() {
  const { role } = useAuth()
  const isManager = role === ROLES.MANAGER || role === ROLES.ADMIN || role === ROLES.HR

  // Active tab: 'my' | 'team'
  const [activeTab, setActiveTab] = useState('my')

  // Clock state
  const [currentTime, setCurrentTime] = useState(new Date())
  const [todayRecord, setTodayRecord] = useState(null)
  const [checkingIn, setCheckingIn] = useState(false)
  const [notes, setNotes] = useState('')

  // My Attendance list state
  const [myRecords, setMyRecords] = useState([])
  const [loadingMy, setLoadingMy] = useState(true)
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1)
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())

  // Team Attendance state
  const [teamRecords, setTeamRecords] = useState([])
  const [teamDate, setTeamDate] = useState(todayISO())
  const [loadingTeam, setLoadingTeam] = useState(false)

  // Digital clock timer
  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000)
    return () => clearInterval(timer)
  }, [])

  // Load today's check-in status
  const loadTodayStatus = async () => {
    try {
      const data = await attendanceService.getToday()
      setTodayRecord(data)
    } catch {
      console.error('Failed to load today attendance')
    }
  }

  // Load monthly personal attendance
  const loadMyAttendance = useCallback(async () => {
    setLoadingMy(true)
    try {
      const data = await attendanceService.getMyAttendance(selectedMonth, selectedYear)
      setMyRecords(data || [])
    } catch {
      toast.error('Failed to load attendance records')
    } finally {
      setLoadingMy(false)
    }
  }, [selectedMonth, selectedYear])

  // Load team attendance
  const loadTeamAttendance = useCallback(async () => {
    if (!isManager) return
    setLoadingTeam(true)
    try {
      const data = await attendanceService.getTeamAttendance(teamDate)
      setTeamRecords(data || [])
    } catch {
      toast.error('Failed to load team attendance')
    } finally {
      setLoadingTeam(false)
    }
  }, [isManager, teamDate])

  useEffect(() => {
    loadTodayStatus()
    loadMyAttendance()
  }, [loadMyAttendance])

  useEffect(() => {
    if (activeTab === 'team') {
      loadTeamAttendance()
    }
  }, [activeTab, loadTeamAttendance])

  const handleCheckIn = async () => {
    setCheckingIn(true)
    try {
      const res = await attendanceService.checkIn(notes)
      setTodayRecord(res)
      setNotes('')
      toast.success('Successfully checked in!')
      loadMyAttendance()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Check-in failed')
    } finally {
      setCheckingIn(false)
    }
  }

  const handleCheckOut = async () => {
    setCheckingIn(true)
    try {
      const res = await attendanceService.checkOut(notes)
      setTodayRecord(res)
      setNotes('')
      toast.success(`Successfully checked out! Worked: ${res.workingHours} hours`)
      loadMyAttendance()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Check-out failed')
    } finally {
      setCheckingIn(false)
    }
  }

  const getStatusBadge = (status) => {
    switch (status) {
      case ATTENDANCE_STATUS.PRESENT:
        return <span className="badge-green">Present</span>
      case ATTENDANCE_STATUS.HALF_DAY:
        return <span className="badge-yellow">Half Day</span>
      case ATTENDANCE_STATUS.ON_LEAVE:
        return <span className="badge-purple">On Leave</span>
      case ATTENDANCE_STATUS.ABSENT:
        return <span className="badge-red">Absent</span>
      default:
        return <span className="badge-gray">{status}</span>
    }
  }

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Attendance Tracking</h1>
          <p className="page-subtitle">Record daily office attendance and inspect working hour logs</p>
        </div>

        {isManager && (
          <div className="flex bg-gray-200/80 p-1 rounded-lg">
            <button
              onClick={() => setActiveTab('my')}
              className={`px-4 py-1.5 rounded-md text-xs font-bold transition-all ${
                activeTab === 'my' ? 'bg-white shadow text-primary-700' : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              My Attendance
            </button>
            <button
              onClick={() => setActiveTab('team')}
              className={`px-4 py-1.5 rounded-md text-xs font-bold transition-all ${
                activeTab === 'team' ? 'bg-white shadow text-primary-700' : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              Team Attendance
            </button>
          </div>
        )}
      </div>

      {activeTab === 'my' ? (
        <div className="space-y-6">
          {/* Check-In / Check-Out Hero Card */}
          <div className="card bg-gradient-to-br from-slate-900 via-slate-800 to-indigo-950 text-white p-6 sm:p-8">
            <div className="flex flex-col md:flex-row items-center justify-between gap-6">
              {/* Digital Live Clock */}
              <div className="space-y-1 text-center md:text-left">
                <div className="text-xs uppercase tracking-widest text-indigo-300 font-semibold">
                  {formatDate(currentTime, 'EEEE, dd MMMM yyyy')}
                </div>
                <div className="text-4xl sm:text-5xl font-black font-mono tracking-tight text-white">
                  {currentTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                </div>
                <div className="text-xs text-gray-400">Standard Work Day: 8.0 hours</div>
              </div>

              {/* Status & Action */}
              <div className="flex flex-col sm:flex-row items-center gap-4 w-full md:w-auto">
                {/* Status Box */}
                <div className="bg-white/10 backdrop-blur rounded-xl p-4 border border-white/10 text-center min-w-[180px]">
                  <span className="text-xs text-indigo-200 block">Today's Status</span>
                  {!todayRecord ? (
                    <span className="font-bold text-amber-300 text-sm mt-1 block">Not Checked In</span>
                  ) : todayRecord.checkOut ? (
                    <div>
                      <span className="font-bold text-emerald-400 text-sm block">Checked Out</span>
                      <span className="text-[11px] text-gray-300 block">Worked: {todayRecord.workingHours} hrs</span>
                    </div>
                  ) : (
                    <div>
                      <span className="font-bold text-emerald-400 text-sm block">Checked In</span>
                      <span className="text-[11px] text-gray-300 block">At: {formatTime(todayRecord.checkIn)}</span>
                    </div>
                  )}
                </div>

                {/* Action Buttons */}
                <div className="flex flex-col gap-2 w-full sm:w-auto">
                  {!todayRecord ? (
                    <button
                      onClick={handleCheckIn}
                      disabled={checkingIn}
                      className="btn-success btn-lg shadow-lg flex items-center justify-center gap-2"
                    >
                      <LogIn size={20} />
                      {checkingIn ? 'Checking in...' : 'Clock In Now'}
                    </button>
                  ) : !todayRecord.checkOut ? (
                    <button
                      onClick={handleCheckOut}
                      disabled={checkingIn}
                      className="btn-danger btn-lg shadow-lg flex items-center justify-center gap-2"
                    >
                      <LogOut size={20} />
                      {checkingIn ? 'Checking out...' : 'Clock Out Now'}
                    </button>
                  ) : (
                    <div className="px-4 py-2 bg-emerald-500/20 border border-emerald-500/30 text-emerald-300 rounded-lg text-xs font-semibold flex items-center gap-1.5">
                      <CheckCircle2 size={16} /> Completed for Today
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Month Filter & Monthly Logs Table */}
          <div className="card p-0 overflow-hidden">
            <div className="p-4 border-b border-gray-100 flex flex-col sm:flex-row items-center justify-between gap-4">
              <h3 className="font-bold text-gray-900 text-base">Monthly Log</h3>
              <div className="flex items-center gap-2">
                <select
                  value={selectedMonth}
                  onChange={(e) => setSelectedMonth(Number(e.target.value))}
                  className="input w-auto text-xs py-1"
                >
                  {[
                    'January', 'February', 'March', 'April', 'May', 'June',
                    'July', 'August', 'September', 'October', 'November', 'December'
                  ].map((m, idx) => (
                    <option key={idx + 1} value={idx + 1}>{m}</option>
                  ))}
                </select>

                <select
                  value={selectedYear}
                  onChange={(e) => setSelectedYear(Number(e.target.value))}
                  className="input w-auto text-xs py-1"
                >
                  {[2024, 2025, 2026].map((y) => (
                    <option key={y} value={y}>{y}</option>
                  ))}
                </select>
              </div>
            </div>

            {loadingMy ? (
              <div className="flex justify-center items-center py-20">
                <LoadingSpinner size="lg" />
              </div>
            ) : myRecords.length === 0 ? (
              <div className="text-center py-16">
                <Clock size={40} className="mx-auto text-gray-300 mb-3" />
                <p className="text-gray-500 font-medium">No attendance records for this month</p>
              </div>
            ) : (
              <div className="table-container border-0">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Check In</th>
                      <th>Check Out</th>
                      <th>Working Hours</th>
                      <th>Status</th>
                      <th>Notes</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {myRecords.map((rec) => (
                      <tr key={rec.id}>
                        <td className="font-semibold text-gray-900">{formatDate(rec.date)}</td>
                        <td>{formatTime(rec.checkIn)}</td>
                        <td>{rec.checkOut ? formatTime(rec.checkOut) : '—'}</td>
                        <td>
                          {rec.workingHours ? (
                            <span className="font-bold text-gray-800">{rec.workingHours} hrs</span>
                          ) : (
                            <span className="text-xs text-blue-600 font-semibold">Active shift</span>
                          )}
                        </td>
                        <td>{getStatusBadge(rec.status)}</td>
                        <td>
                          <span className="text-xs text-gray-500">{rec.notes || '—'}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      ) : (
        /* Team Attendance Tab */
        <div className="space-y-4">
          <div className="card p-4 flex flex-col sm:flex-row items-center justify-between gap-4">
            <h3 className="font-bold text-gray-900 text-base">Direct Reports Attendance</h3>
            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-500 font-semibold">Select Date:</span>
              <input
                type="date"
                value={teamDate}
                onChange={(e) => setTeamDate(e.target.value)}
                className="input w-auto text-xs py-1"
              />
            </div>
          </div>

          <div className="card p-0 overflow-hidden">
            {loadingTeam ? (
              <div className="flex justify-center items-center py-20">
                <LoadingSpinner size="lg" />
              </div>
            ) : teamRecords.length === 0 ? (
              <div className="text-center py-16">
                <Users size={40} className="mx-auto text-gray-300 mb-3" />
                <p className="text-gray-500 font-medium">No team attendance entries logged for {teamDate}</p>
              </div>
            ) : (
              <div className="table-container border-0">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Team Member</th>
                      <th>Check In</th>
                      <th>Check Out</th>
                      <th>Hours Worked</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {teamRecords.map((t) => (
                      <tr key={t.id}>
                        <td>
                          <div className="font-semibold text-gray-900">{t.employeeName}</div>
                          <div className="text-xs text-gray-400">{t.employeeCode}</div>
                        </td>
                        <td>{formatTime(t.checkIn)}</td>
                        <td>{t.checkOut ? formatTime(t.checkOut) : 'In Progress'}</td>
                        <td>
                          <span className="font-bold text-gray-800">{t.workingHours || '—'} hrs</span>
                        </td>
                        <td>{getStatusBadge(t.status)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

export default AttendancePage
