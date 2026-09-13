import api from './api'

export const attendanceService = {
  checkIn: async (notes = '') => {
    const response = await api.post('/api/attendance/check-in', { notes })
    return response.data
  },

  checkOut: async (notes = '') => {
    const response = await api.post('/api/attendance/check-out', { notes })
    return response.data
  },

  getToday: async () => {
    const response = await api.get('/api/attendance/today')
    return response.data
  },

  getMyAttendance: async (month, year) => {
    const response = await api.get('/api/attendance/my', {
      params: { month, year }
    })
    return response.data
  },

  getEmployeeAttendance: async (employeeId, month, year) => {
    const response = await api.get(`/api/attendance/employee/${employeeId}`, {
      params: { month, year }
    })
    return response.data
  },

  getTeamAttendance: async (date) => {
    const response = await api.get('/api/attendance/team', {
      params: date ? { date } : {}
    })
    return response.data
  }
}

export default attendanceService
