import { useState, useEffect, useCallback } from 'react'
import { Bell, CheckCheck, Calendar, CheckCircle2, Clock, XCircle, Info } from 'lucide-react'
import toast from 'react-hot-toast'
import notificationService from '../../services/notificationService'
import Pagination from '../../components/common/Pagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate } from '../../utils/dateUtils'

function NotificationsPage() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [size] = useState(15)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const fetchNotifications = useCallback(async () => {
    setLoading(true)
    try {
      const data = await notificationService.getMyNotifications(page, size)
      setNotifications(data.content || [])
      setTotalPages(data.totalPages || 0)
      setTotalElements(data.totalElements || 0)
    } catch {
      toast.error('Failed to load notifications')
    } finally {
      setLoading(false)
    }
  }, [page, size])

  useEffect(() => {
    fetchNotifications()
  }, [fetchNotifications])

  const handleMarkAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id)
      setNotifications(prev =>
        prev.map(n => n.id === id ? { ...n, isRead: true } : n)
      )
    } catch {
      toast.error('Failed to update notification')
    }
  }

  const handleMarkAllRead = async () => {
    try {
      await notificationService.markAllAsRead()
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })))
      toast.success('All notifications marked as read')
    } catch {
      toast.error('Failed to mark notifications read')
    }
  }

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'LEAVE_APPROVED':
        return <div className="p-2 rounded-xl bg-emerald-100 text-emerald-600"><CheckCircle2 size={18} /></div>
      case 'LEAVE_REJECTED':
        return <div className="p-2 rounded-xl bg-red-100 text-red-600"><XCircle size={18} /></div>
      case 'LEAVE_APPLIED':
        return <div className="p-2 rounded-xl bg-blue-100 text-blue-600"><Calendar size={18} /></div>
      case 'ATTENDANCE_REMINDER':
        return <div className="p-2 rounded-xl bg-amber-100 text-amber-600"><Clock size={18} /></div>
      default:
        return <div className="p-2 rounded-xl bg-gray-100 text-gray-600"><Info size={18} /></div>
    }
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Notifications</h1>
          <p className="page-subtitle">Stay updated on leave request statuses, approvals, and company announcements</p>
        </div>

        {notifications.some(n => !n.isRead) && (
          <button
            onClick={handleMarkAllRead}
            className="btn btn-secondary btn-sm"
          >
            <CheckCheck size={16} /> Mark All as Read
          </button>
        )}
      </div>

      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <LoadingSpinner size="lg" />
          </div>
        ) : notifications.length === 0 ? (
          <div className="text-center py-16">
            <Bell size={40} className="mx-auto text-gray-300 mb-3" />
            <p className="text-gray-700 font-bold">No notifications yet</p>
            <p className="text-gray-400 text-sm mt-1">You're all caught up!</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {notifications.map((n) => (
              <div
                key={n.id}
                onClick={() => !n.isRead && handleMarkAsRead(n.id)}
                className={`p-4 flex items-start gap-4 transition-colors cursor-pointer ${
                  !n.isRead ? 'bg-blue-50/40 hover:bg-blue-50/70' : 'hover:bg-gray-50'
                }`}
              >
                {getNotificationIcon(n.type)}

                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between gap-2">
                    <h4 className={`text-sm font-bold ${!n.isRead ? 'text-gray-900' : 'text-gray-700'}`}>
                      {n.title}
                    </h4>
                    <span className="text-[11px] text-gray-400 whitespace-nowrap">
                      {formatDate(n.createdAt, 'dd MMM yyyy, HH:mm')}
                    </span>
                  </div>

                  <p className="text-xs text-gray-600 mt-1 leading-relaxed">
                    {n.message}
                  </p>
                </div>

                {!n.isRead && (
                  <div className="w-2.5 h-2.5 bg-primary-600 rounded-full flex-shrink-0 mt-2" title="Unread" />
                )}
              </div>
            ))}
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

export default NotificationsPage
