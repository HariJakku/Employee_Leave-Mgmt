import { useState, useEffect, useCallback } from 'react'
import { Shield, Search, RefreshCw, User, FileText } from 'lucide-react'
import toast from 'react-hot-toast'
import auditService from '../../services/auditService'
import Pagination from '../../components/common/Pagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDateTime } from '../../utils/dateUtils'

function AuditLogsPage() {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [actionFilter, setActionFilter] = useState('')
  const [page, setPage] = useState(0)
  const [size] = useState(15)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const fetchLogs = useCallback(async () => {
    setLoading(true)
    try {
      const data = await auditService.getAuditLogs({
        action: actionFilter || undefined,
        page,
        size,
      })
      setLogs(data.content || [])
      setTotalPages(data.totalPages || 0)
      setTotalElements(data.totalElements || 0)
    } catch {
      toast.error('Failed to load audit logs')
    } finally {
      setLoading(false)
    }
  }, [actionFilter, page, size])

  useEffect(() => {
    fetchLogs()
  }, [fetchLogs])

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">System Audit Logs</h1>
          <p className="page-subtitle">Security trail recording user logins, employee profile alterations, and leave approvals</p>
        </div>
        <button onClick={fetchLogs} className="btn btn-secondary btn-sm">
          <RefreshCw size={14} /> Refresh Logs
        </button>
      </div>

      {/* Filter */}
      <div className="card p-4">
        <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
          <div className="flex items-center gap-2 w-full sm:w-auto">
            <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Action:</span>
            <select
              value={actionFilter}
              onChange={(e) => { setActionFilter(e.target.value); setPage(0); }}
              className="input w-auto min-w-[200px]"
            >
              <option value="">All Actions</option>
              <option value="LOGIN">User Login</option>
              <option value="EMPLOYEE_CREATED">Employee Created</option>
              <option value="EMPLOYEE_UPDATED">Employee Updated</option>
              <option value="LEAVE_APPLIED">Leave Applied</option>
              <option value="LEAVE_APPROVED">Leave Approved</option>
              <option value="LEAVE_REJECTED">Leave Rejected</option>
              <option value="LEAVE_CANCELLED">Leave Cancelled</option>
              <option value="DEPARTMENT_MODIFIED">Department Modified</option>
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
        ) : logs.length === 0 ? (
          <div className="text-center py-16">
            <Shield size={40} className="mx-auto text-gray-300 mb-3" />
            <p className="text-gray-600 font-medium">No audit events found</p>
          </div>
        ) : (
          <div className="table-container border-0">
            <table className="table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>User</th>
                  <th>Action</th>
                  <th>Target Entity</th>
                  <th>Description</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {logs.map((log) => (
                  <tr key={log.id}>
                    <td className="text-xs font-mono text-gray-500 whitespace-nowrap">
                      {formatDateTime(log.createdAt)}
                    </td>
                    <td>
                      <span className="font-semibold text-gray-900 text-xs flex items-center gap-1.5">
                        <User size={13} className="text-gray-400" /> {log.userEmail}
                      </span>
                    </td>
                    <td>
                      <span className="badge-purple font-mono text-[11px]">
                        {log.action}
                      </span>
                    </td>
                    <td>
                      <span className="text-xs text-gray-600 font-medium">
                        {log.entityType ? `${log.entityType} ${log.entityId ? '#' + log.entityId : ''}` : '—'}
                      </span>
                    </td>
                    <td>
                      <span className="text-xs text-gray-700">{log.description || '—'}</span>
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

export default AuditLogsPage
