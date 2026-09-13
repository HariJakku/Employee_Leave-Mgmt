import api from './api'

export const authService = {
  login: async (credentials) => {
    const response = await api.post('/api/auth/login', credentials)
    return response.data
  },

  register: async (userData) => {
    const response = await api.post('/api/auth/register', userData)
    return response.data
  },

  changePassword: async (passwordData) => {
    const response = await api.post('/api/auth/change-password', passwordData)
    return response.data
  },

  getCurrentUser: async () => {
    const response = await api.get('/api/auth/me')
    return response.data
  }
}

export default authService
