import api from './api'

export const leaveService = {
  // Leave Types
  getLeaveTypes: async (activeOnly = false) => {
    const response = await api.get('/api/leave-types', {
      params: activeOnly ? { activeOnly: true } : {}
    })
    return response.data
  },

  createLeaveType: async (data) => {
    const response = await api.post('/api/leave-types', data)
    return response.data
  },

  updateLeaveType: async (id, data) => {
    const response = await api.put(`/api/leave-types/${id}`, data)
    return response.data
  },

  deleteLeaveType: async (id) => {
    const response = await api.delete(`/api/leave-types/${id}`)
    return response.data
  },

  // Leave Requests & Balances
  getMyBalances: async () => {
    const response = await api.get('/api/leaves/my/balances')
    return response.data
  },

  getMyLeaves: async (page = 0, size = 10) => {
    const response = await api.get('/api/leaves/my', { params: { page, size } })
    return response.data
  },

  getAllLeaves: async (params = {}) => {
    const response = await api.get('/api/leaves', { params })
    return response.data
  },

  getLeaveById: async (id) => {
    const response = await api.get(`/api/leaves/${id}`)
    return response.data
  },

  applyLeave: async (data) => {
    const response = await api.post('/api/leaves', data)
    return response.data
  },

  cancelLeave: async (id) => {
    const response = await api.put(`/api/leaves/${id}/cancel`)
    return response.data
  },

  // Approvals
  getPendingApprovals: async (page = 0, size = 10) => {
    const response = await api.get('/api/leaves/approvals/pending', { params: { page, size } })
    return response.data
  },

  approveLeave: async (id, comments) => {
    const response = await api.put(`/api/leaves/${id}/approve`, { comments })
    return response.data
  },

  rejectLeave: async (id, reason) => {
    const response = await api.put(`/api/leaves/${id}/reject`, {
      comments: reason,
      rejectionReason: reason
    })
    return response.data
  }
}

export default leaveService
