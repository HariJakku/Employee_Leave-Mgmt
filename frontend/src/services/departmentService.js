import api from './api'

export const departmentService = {
  getDepartments: async () => {
    const response = await api.get('/api/departments')
    return response.data
  },

  getDepartmentById: async (id) => {
    const response = await api.get(`/api/departments/${id}`)
    return response.data
  },

  createDepartment: async (deptData) => {
    const response = await api.post('/api/departments', deptData)
    return response.data
  },

  updateDepartment: async (id, deptData) => {
    const response = await api.put(`/api/departments/${id}`, deptData)
    return response.data
  },

  deleteDepartment: async (id) => {
    const response = await api.delete(`/api/departments/${id}`)
    return response.data
  },

  getDepartmentEmployees: async (id) => {
    const response = await api.get(`/api/departments/${id}/employees`)
    return response.data
  }
}

export default departmentService
