import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * RoleRoute — renders children only if current user has one of the allowed roles.
 * Redirects to /unauthorized otherwise.
 *
 * Usage:
 *   <RoleRoute roles={['ADMIN', 'HR']}>
 *     <SensitivePage />
 *   </RoleRoute>
 */
function RoleRoute({ children, roles = [] }) {
  const { role, isAuthenticated } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (roles.length > 0 && !roles.includes(role)) {
    return <Navigate to="/unauthorized" replace />
  }

  return children
}

export default RoleRoute
