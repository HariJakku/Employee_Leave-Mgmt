import { useState, useEffect, useCallback } from 'react'
import { Check, X, CheckCircle2, Clock, FileText, AlertCircle } from 'lucide-react'
import toast from 'react-hot-toast'
import leaveService from '../../services/leaveService'
import { useAuth } from '../../context/AuthContext'
import Modal from '../../components/common/Modal'
import Pagination from '../../components/common/Pagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate } from '../../utils/dateUtils'
import { ROLES, LEAVE_STATUS } from '../../utils/constants'

function LeaveApproval() {
  const { role } = useAuth()
  const isManager = role === ROLES.MANAGER
  const isHR = role === ROLES.HR || role === ROLES.ADMIN

  const [requests, setRequests] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [size] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  // Action modal
  const [activeRequest, setActiveRequest] = useState(null)
  const [actionType, setActionType] = useState(null) // 'APPROVE' | 'REJECT'
  const [comments, setComments] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const fetchPending = useCallback(async () => {
    setLoading(true)
    try {
      const data = await leaveService.getPendingApprovals(page, size)
      setRequests(data.content || [])
      setTotalPages(data.totalPages || 0)
      setTotalElements(data.totalElements || 0)
    } catch (err) {
      toast.error('Failed to load pending approvals')
    } finally {
      setLoading(false)
    }
  }, [page, size])

  useEffect(() => {
    fetchPending()
  }, [fetchPending])

  const handleOpenAction = (req, type) => {
    setActiveRequest(req)
    setActionType(type)
    setComments('')
  }

  const handleConfirmAction = async () => {
    if (actionType === 'REJECT' && !comments.trim()) {
      toast.error('Please specify a reason for rejecting the leave request.')
      return
    }

    setSubmitting(true)
    try {
      if (actionType === 'APPROVE') {
        await leaveService.approveLeave(activeRequest.id, comments.trim())
        toast.success(`Leave request approved`)
      } else {
        await leaveService.rejectLeave(activeRequest.id, comments.trim())
        toast.success(`Leave request rejected`)
      }
      setActiveRequest(null)
      fetchPending()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Approval action failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Leave Approvals</h1>
          <p className="page-subtitle">
            {isManager ? 'Review and authorize leave applications from your direct reports' :
             'Review and provide final HR authorization for employee leave requests'}
          </p>
        </div>
      </div>

      {/* Table */}
      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <LoadingSpinner size="lg" />
          </div>
        ) : requests.length === 0 ? (
          <div className="text-center py-16">
            <CheckCircle2 size={40} className="mx-auto text-emerald-400 mb-3" />
            <p className="text-gray-700 font-bold">All caught up!</p>
            <p className="text-gray-400 text-sm mt-1">No pending leave requests require your action</p>
          </div>
        ) : (
          <div className="table-container border-0">
            <table className="table">
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Leave Type</th>
                  <th>Period</th>
                  <th>Days</th>
                  <th>Workflow Stage</th>
                  <th>Reason</th>
                  <th className="text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {requests.map((req) => (
                  <tr key={req.id}>
                    <td>
                      <div className="font-semibold text-gray-900">{req.employeeName}</div>
                      <div className="text-xs text-gray-400">{req.employeeCode} • {req.departmentName}</div>
                    </td>
                    <td>
                      <span className="font-semibold text-gray-800">{req.leaveTypeName}</span>
                    </td>
                    <td>
                      <div className="text-sm font-medium text-gray-900">
                        {formatDate(req.startDate)} - {formatDate(req.endDate)}
                      </div>
                      <div className="text-xs text-gray-400">Applied {formatDate(req.appliedAt)}</div>
                    </td>
                    <td>
                      <span className="font-extrabold text-gray-900 text-sm">{req.totalDays}</span>
                    </td>
                    <td>
                      {req.status === LEAVE_STATUS.PENDING ? (
                        <span className="badge-yellow flex items-center gap-1">
                          <Clock size={12} /> Step 1: Manager Review
                        </span>
                      ) : req.status === LEAVE_STATUS.MANAGER_APPROVED ? (
                        <span className="badge-blue flex items-center gap-1">
                          <Clock size={12} /> Step 2: Final HR Review
                        </span>
                      ) : (
                        <span className="badge-gray">{req.status}</span>
                      )}
                    </td>
                    <td>
                      <p className="text-xs text-gray-600 max-w-[220px] line-clamp-2" title={req.reason}>
                        {req.reason}
                      </p>
                    </td>
                    <td className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleOpenAction(req, 'APPROVE')}
                          className="btn-success btn-sm"
                        >
                          <Check size={14} /> Approve
                        </button>
                        <button
                          onClick={() => handleOpenAction(req, 'REJECT')}
                          className="btn-danger btn-sm"
                        >
                          <X size={14} /> Reject
                        </button>
                      </div>
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

      {/* Confirmation Modal */}
      <Modal
        isOpen={Boolean(activeRequest)}
        onClose={() => setActiveRequest(null)}
        title={actionType === 'APPROVE' ? 'Approve Leave Request' : 'Reject Leave Request'}
      >
        {activeRequest && (
          <div className="space-y-4">
            <div className="p-3 bg-gray-50 rounded-lg space-y-1 text-sm border border-gray-100">
              <div className="font-semibold text-gray-900">
                {activeRequest.employeeName} ({activeRequest.employeeCode})
              </div>
              <div className="text-gray-600 text-xs">
                {activeRequest.leaveTypeName} • {formatDate(activeRequest.startDate)} to {formatDate(activeRequest.endDate)} ({activeRequest.totalDays} days)
              </div>
              <div className="text-gray-500 text-xs italic pt-1">
                "{activeRequest.reason}"
              </div>
            </div>

            <div>
              <label className="label">
                {actionType === 'APPROVE' ? 'Comments (Optional)' : 'Rejection Reason (Required) *'}
              </label>
              <textarea
                rows={3}
                value={comments}
                onChange={(e) => setComments(e.target.value)}
                placeholder={
                  actionType === 'APPROVE'
                    ? 'Add any instructions or remarks...'
                    : 'Explain reason for rejecting this leave request...'
                }
                className="input"
              />
            </div>

            <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
              <button
                type="button"
                onClick={() => setActiveRequest(null)}
                className="btn btn-secondary"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={submitting}
                onClick={handleConfirmAction}
                className={actionType === 'APPROVE' ? 'btn-success' : 'btn-danger'}
              >
                {submitting ? <LoadingSpinner size="sm" /> : null}
                {submitting ? 'Processing...' : actionType === 'APPROVE' ? 'Confirm Approval' : 'Confirm Rejection'}
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}

export default LeaveApproval
