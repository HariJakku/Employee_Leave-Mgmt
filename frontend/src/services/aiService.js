import api from './api'

export const aiService = {
  chatWithLeaveAssistant: async (prompt) => {
    const response = await api.post('/api/ai/chat', { prompt })
    return response.data
  },

  parseLeaveRequest: async (prompt) => {
    const response = await api.post('/api/ai/generate-leave-request', { prompt })
    return response.data
  },

  queryHRAssistant: async (prompt) => {
    const response = await api.post('/api/ai/hr-query', { prompt })
    return response.data
  },

  generateEmployeeSummary: async (employeeId) => {
    const response = await api.get(`/api/ai/employee-summary/${employeeId}`)
    return response.data
  }
}

export default aiService
