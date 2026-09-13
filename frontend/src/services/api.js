import axios from 'axios'
import toast from 'react-hot-toast'
import { STORAGE_KEYS } from '../utils/constants'

/**
 * Centralized Axios instance.
 * - Base URL is '' in dev (Vite proxy handles /api → :8080)
 * - Automatically attaches JWT Bearer token from localStorage
 * - Handles 401 → clears auth state and redirects to login
 * - Shows error toasts for server errors (5xx)
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// ===== REQUEST INTERCEPTOR =====
// Attach JWT token to every request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// ===== RESPONSE INTERCEPTOR =====
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message

    if (status === 401) {
      // Token expired or invalid — clear auth and redirect
      localStorage.removeItem(STORAGE_KEYS.TOKEN)
      localStorage.removeItem(STORAGE_KEYS.USER)
      localStorage.removeItem(STORAGE_KEYS.ROLE)

      // Only redirect if not already on the login page
      if (!window.location.pathname.includes('/login')) {
        toast.error('Session expired. Please log in again.')
        window.location.href = '/login'
      }
    } else if (status === 403) {
      toast.error('You do not have permission to perform this action.')
    } else if (status >= 500) {
      toast.error('Server error. Please try again later.')
    }

    return Promise.reject(error)
  },
)

export default api
