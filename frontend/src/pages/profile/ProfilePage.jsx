import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { User, Mail, Shield, Building2, Calendar, Lock, Phone } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import authService from '../../services/authService'
import employeeService from '../../services/employeeService'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate } from '../../utils/dateUtils'

function ProfilePage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    authService.getCurrentUser()
      .then(async (u) => {
        if (u.employeeId) {
          try {
            const emp = await employeeService.getEmployeeById(u.employeeId)
            setProfile(emp)
          } catch {
            setProfile(u)
          }
        } else {
          setProfile(u)
        }
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">My Account Profile</h1>
          <p className="page-subtitle">Personal information, authentication role, and security settings</p>
        </div>
        <button
          onClick={() => navigate('/change-password')}
          className="btn btn-secondary"
        >
          <Lock size={15} /> Change Password
        </button>
      </div>

      <div className="card space-y-6">
        <div className="flex items-center gap-5 border-b border-gray-100 pb-6">
          <div className="w-20 h-20 bg-primary-600 text-white rounded-2xl flex items-center justify-center text-2xl font-bold">
            {profile?.firstName?.[0]}{profile?.lastName?.[0]}
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-900">{profile?.fullName || `${profile?.firstName} ${profile?.lastName}`}</h2>
            <p className="text-sm text-gray-500">{profile?.designation || 'System User'} • {profile?.departmentName || 'No Department'}</p>
            <div className="flex items-center gap-2 mt-2">
              <span className="badge-purple font-mono text-xs">{profile?.role || user?.role}</span>
              {profile?.employeeCode && (
                <span className="badge-blue font-mono text-xs">{profile.employeeCode}</span>
              )}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 text-sm">
          <div>
            <span className="text-gray-400 block text-xs flex items-center gap-1">
              <Mail size={13} /> Email Address
            </span>
            <span className="font-semibold text-gray-800">{profile?.email}</span>
          </div>

          <div>
            <span className="text-gray-400 block text-xs flex items-center gap-1">
              <Phone size={13} /> Contact Phone
            </span>
            <span className="font-semibold text-gray-800">{profile?.phone || '—'}</span>
          </div>

          <div>
            <span className="text-gray-400 block text-xs flex items-center gap-1">
              <Building2 size={13} /> Department
            </span>
            <span className="font-semibold text-gray-800">{profile?.departmentName || '—'}</span>
          </div>

          <div>
            <span className="text-gray-400 block text-xs flex items-center gap-1">
              <Calendar size={13} /> Date Joined
            </span>
            <span className="font-semibold text-gray-800">{formatDate(profile?.joiningDate)}</span>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ProfilePage
