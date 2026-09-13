import { useState, useEffect } from 'react'
import { Menu, Bell, User } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import notificationService from '../../services/notificationService'

function Navbar({ onMenuClick }) {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    let isMounted = true

    const fetchCount = async () => {
      try {
        const count = await notificationService.getUnreadCount()
        if (isMounted) setUnreadCount(count || 0)
      } catch {
        // silent fail
      }
    }

    fetchCount()
    const interval = setInterval(fetchCount, 30000) // poll every 30s
    return () => {
      isMounted = false
      clearInterval(interval)
    }
  }, [])

  return (
    <header className="bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between z-30">
      {/* Left: hamburger (mobile) */}
      <div className="flex items-center gap-4">
        <button
          className="lg:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors"
          onClick={onMenuClick}
          aria-label="Open navigation menu"
        >
          <Menu size={20} className="text-gray-600" />
        </button>
        <h1 className="text-base font-semibold text-gray-800 hidden sm:block">
          Employee Management & Leave ERP System
        </h1>
      </div>

      {/* Right: notifications + avatar */}
      <div className="flex items-center gap-3">
        <button
          className="relative p-2 rounded-lg hover:bg-gray-100 transition-colors"
          onClick={() => navigate('/notifications')}
          aria-label="Notifications"
        >
          <Bell size={20} className="text-gray-600" />
          {unreadCount > 0 && (
            <span className="absolute top-1 right-1 min-w-[18px] h-[18px] bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1 border-2 border-white animate-pulse">
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
          )}
        </button>

        <button
          className="flex items-center gap-2 p-1.5 rounded-lg hover:bg-gray-100 transition-colors"
          onClick={() => navigate('/profile')}
        >
          <div className="w-8 h-8 bg-primary-600 rounded-full flex items-center justify-center text-white text-xs font-bold">
            {user?.firstName?.[0] || 'U'}{user?.lastName?.[0] || ''}
          </div>
          <span className="hidden sm:block text-sm font-medium text-gray-700">
            {user?.firstName} {user?.lastName}
          </span>
        </button>
      </div>
    </header>
  )
}

export default Navbar
