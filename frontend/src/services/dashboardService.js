import api from './api'

export const dashboardService = {
  getAdminDashboard: async () => {
    const response = await api.get('/api/dashboard/admin')
    return response.data
  },

  getHRDashboard: async () => {
    const response = await api.get('/api/dashboard/hr')
    return response.data
  },

  getManagerDashboard: async () => {
    const response = await api.get('/api/dashboard/manager')
    return response.data
  },

  getEmployeeDashboard: async () => {
    const response = await api.get('/api/dashboard/employee')
    return response.data
  }
}

export default dashboardService
