import api from './api'

export const notificationService = {
  getMyNotifications: async (page = 0, size = 15) => {
    const response = await api.get('/api/notifications', { params: { page, size } })
    return response.data
  },

  getUnreadCount: async () => {
    const response = await api.get('/api/notifications/unread-count')
    return response.data.unreadCount
  },

  markAsRead: async (id) => {
    const response = await api.put(`/api/notifications/${id}/read`)
    return response.data
  },

  markAllAsRead: async () => {
    const response = await api.put('/api/notifications/read-all')
    return response.data
  }
}

export default notificationService
