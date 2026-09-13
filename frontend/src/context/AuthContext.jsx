import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { STORAGE_KEYS, ROLES } from '../utils/constants'

/**
 * AuthContext — provides authentication state and actions to the entire app.
 *
 * State shape:
 *   user        - object from /api/auth/me response
 *   token       - JWT string
 *   role        - string (ADMIN | HR | MANAGER | EMPLOYEE)
 *   isLoading   - true while checking stored token on app init
 *   isAuthenticated - derived boolean
 */
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]         = useState(null)
  const [token, setToken]       = useState(null)
  const [role, setRole]         = useState(null)
  const [isLoading, setIsLoading] = useState(true)

  // On mount, restore auth state from localStorage
  useEffect(() => {
    try {
      const storedToken = localStorage.getItem(STORAGE_KEYS.TOKEN)
      const storedUser  = localStorage.getItem(STORAGE_KEYS.USER)
      const storedRole  = localStorage.getItem(STORAGE_KEYS.ROLE)

      if (storedToken && storedUser) {
        setToken(storedToken)
        setUser(JSON.parse(storedUser))
        setRole(storedRole)
      }
    } catch {
      // Corrupted storage — clear it
      localStorage.clear()
    } finally {
      setIsLoading(false)
    }
  }, [])

  /**
   * Call this after a successful /api/auth/login response.
   * @param {string} jwtToken
   * @param {object} userData
   */
  const login = useCallback((jwtToken, userData) => {
    localStorage.setItem(STORAGE_KEYS.TOKEN, jwtToken)
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(userData))
    localStorage.setItem(STORAGE_KEYS.ROLE, userData.role)
    setToken(jwtToken)
    setUser(userData)
    setRole(userData.role)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEYS.TOKEN)
    localStorage.removeItem(STORAGE_KEYS.USER)
    localStorage.removeItem(STORAGE_KEYS.ROLE)
    setToken(null)
    setUser(null)
    setRole(null)
  }, [])

  // Role helpers
  const isAdmin    = role === ROLES.ADMIN
  const isHR       = role === ROLES.HR
  const isManager  = role === ROLES.MANAGER
  const isEmployee = role === ROLES.EMPLOYEE
  const hasRole    = (...roles) => roles.includes(role)

  const value = {
    user,
    token,
    role,
    isLoading,
    isAuthenticated: !!token,
    login,
    logout,
    isAdmin,
    isHR,
    isManager,
    isEmployee,
    hasRole,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

/**
 * Custom hook — use anywhere inside AuthProvider.
 * @returns {ReturnType<typeof AuthProvider>} auth context
 */
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

export default AuthContext
