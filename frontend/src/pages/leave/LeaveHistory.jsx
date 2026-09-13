import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus, XCircle, Calendar, CheckCircle2, Clock, Ban } from 'lucide-react'
import toast from 'react-hot-toast'
import leaveService from '../../services/leaveService'
import { useAuth } from '../../context/AuthContext'
import Pagination from '../../components/common/Pagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate } from '../../utils/dateUtils'
import { ROLES, LEAVE_STATUS } from '../../utils/constants'

function LeaveHistory() {
  const navigate = useNavigate()
  const { role } = useAuth()
  const isPrivileged = [ROLES.ADMIN, ROLES.HR].includes(role)

  const [leaves, setLeaves] = useState([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('')
  const [page, setPage] = useState(0)
  const [size] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const fetchLeaves = useCallback(async () => {
    setLoading(true)
    try {
      let data
      if (isPrivileged) {
        data = await leaveService.getAllLeaves({
          status: statusFilter || undefined,
          page,
          size,
        })
      } else {
        data = await leaveService.getMyLeaves(page, size)
      }
      setLeaves(data.content || [])
      setTotalPages(data.totalPages || 0)
      setTotalElements(data.totalElements || 0)
    } catch (err) {
      toast.error('Failed to load leave history')
    } finally {
      setLoading(false)
    }
  }, [isPrivileged, statusFilter, page, size])

  useEffect(() => {
    fetchLeaves()
  }, [fetchLeaves])

  const handleCancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this leave request?')) return
    try {
      await leaveService.cancelLeave(id)
      toast.success('Leave request cancelled and quota restored')
      fetchLeaves()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to cancel leave')
    }
  }

  const getStatusBadge = (status) => {
    switch (status) {
      case LEAVE_STATUS.APPROVED:
        return <span className="badge-green flex items-center gap-1"><CheckCircle2 size={12} /> Approved</span>
      case LEAVE_STATUS.PENDING:
        return <span className="badge-yellow flex items-center gap-1"><Clock size={12} /> Pending</span>
      case LEAVE_STATUS.MANAGER_APPROVED:
        return <span className="badge-blue flex items-center gap-1"><Clock size={12} /> Manager Approved</span>
      case LEAVE_STATUS.REJECTED:
        return <span className="badge-red flex items-center gap-1"><Ban size={12} /> Rejected</span>
      case LEAVE_STATUS.CANCELLED:
        return <span className="badge-gray flex items-center gap-1"><XCircle size={12} /> Cancelled</span>
      default:
        return <span className="badge-gray">{status}</span>
    }
  }

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">{isPrivileged ? 'All Leave Requests' : 'My Leave Requests'}</h1>
          <p className="page-subtitle">View past and current leave applications, statuses, and manager comments</p>
        </div>
        <button onClick={() => navigate('/leave/apply')} className="btn-primary">
          <Plus size={16} /> Apply Leave
        </button>
      </div>

      {/* Filter bar */}
      <div className="card p-4">
        <div className="flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Status:</span>
            <select
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
              className="input w-auto min-w-[160px]"
            >
              <option value="">All Statuses</option>
              <option value="PENDING">Pending</option>
              <option value="MANAGER_APPROVED">Manager Approved</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <LoadingSpinner size="lg" />
          </div>
        ) : leaves.length === 0 ? (
          <div className="text-center py-16">
            <Calendar size={40} className="mx-auto text-gray-300 mb-3" />
            <p className="text-gray-600 font-medium">No leave records found</p>
            <p className="text-gray-400 text-sm mt-1">Submit your first leave request to track here</p>
          </div>
        ) : (
          <div className="table-container border-0">
            <table className="table">
              <thead>
                <tr>
                  {isPrivileged && <th>Employee</th>}
                  <th>Leave Type</th>
                  <th>Dates</th>
                  <th>Days</th>
                  <th>Status</th>
                  <th>Reason</th>
                  <th>Applied On</th>
                  <th className="text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {leaves.map((l) => (
                  <tr key={l.id}>
                    {isPrivileged && (
                      <td>
                        <div className="font-semibold text-gray-900">{l.employeeName}</div>
                        <div className="text-xs text-gray-400">{l.employeeCode} • {l.departmentName}</div>
                      </td>
                    )}
                    <td>
                      <span className="font-semibold text-gray-800">{l.leaveTypeName}</span>
                    </td>
                    <td>
                      <div className="text-sm font-medium text-gray-900">
                        {formatDate(l.startDate)} - {formatDate(l.endDate)}
                      </div>
                    </td>
                    <td>
                      <span className="font-bold text-gray-800">{l.totalDays}</span>
                    </td>
                    <td>
                      {getStatusBadge(l.status)}
                    </td>
                    <td>
                      <span className="text-xs text-gray-600 line-clamp-1 max-w-[200px]" title={l.reason}>
                        {l.reason}
                      </span>
                      {l.rejectionReason && (
                        <span className="text-xs text-red-600 block mt-0.5">
                          Reason: {l.rejectionReason}
                        </span>
                      )}
                    </td>
                    <td>
                      <span className="text-xs text-gray-500">{formatDate(l.appliedAt)}</span>
                    </td>
                    <td className="text-right">
                      {['PENDING', 'MANAGER_APPROVED', 'APPROVED'].includes(l.status) && (
                        <button
                          onClick={() => handleCancel(l.id)}
                          className="btn btn-secondary btn-sm text-red-600 hover:bg-red-50 hover:border-red-200"
                          title="Cancel Request"
                        >
                          Cancel
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination
          currentPage={page}
          totalPages={totalPages}
          totalElements={totalElements}
          pageSize={size}
          onPageChange={(newPage) => setPage(newPage)}
        />
      </div>
    </div>
  )
}

export default LeaveHistory
