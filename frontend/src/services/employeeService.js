import api from './api'

export const employeeService = {
  getEmployees: async (params = {}) => {
    const response = await api.get('/api/employees', { params })
    return response.data
  },

  getEmployeeById: async (id) => {
    const response = await api.get(`/api/employees/${id}`)
    return response.data
  },

  createEmployee: async (employeeData) => {
    const response = await api.post('/api/employees', employeeData)
    return response.data
  },

  updateEmployee: async (id, employeeData) => {
    const response = await api.put(`/api/employees/${id}`, employeeData)
    return response.data
  },

  deleteEmployee: async (id) => {
    const response = await api.delete(`/api/employees/${id}`)
    return response.data
  },

  getEmployeeLeaveBalances: async (id, year) => {
    const response = await api.get(`/api/employees/${id}/leave-balances`, {
      params: year ? { year } : {}
    })
    return response.data
  },

  getTeam: async (managerId) => {
    const response = await api.get(`/api/employees/manager/${managerId}/team`)
    return response.data
  }
}

export default employeeService
