import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Lock, Check, ArrowLeft } from 'lucide-react'
import toast from 'react-hot-toast'
import authService from '../../services/authService'
import LoadingSpinner from '../../components/common/LoadingSpinner'

function ChangePassword() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }))
  }

  const validate = () => {
    const errs = {}
    if (!form.currentPassword) errs.currentPassword = 'Enter current password'
    if (!form.newPassword || form.newPassword.length < 6) errs.newPassword = 'New password must be at least 6 characters'
    if (form.newPassword !== form.confirmPassword) errs.confirmPassword = 'Passwords do not match'
    return errs
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length > 0) {
      setErrors(errs)
      return
    }

    setLoading(true)
    try {
      await authService.changePassword(form)
      toast.success('Password updated successfully!')
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
      navigate('/profile')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to change password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-md mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate(-1)} className="p-2 rounded-lg hover:bg-gray-100 text-gray-500">
          <ArrowLeft size={20} />
        </button>
        <div>
          <h1 className="page-title">Change Password</h1>
          <p className="page-subtitle">Update your account login credentials</p>
        </div>
      </div>

      <div className="card">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label">Current Password *</label>
            <input
              type="password"
              name="currentPassword"
              value={form.currentPassword}
              onChange={handleChange}
              className={`input ${errors.currentPassword ? 'input-error' : ''}`}
            />
            {errors.currentPassword && <p className="error-text">{errors.currentPassword}</p>}
          </div>

          <div>
            <label className="label">New Password *</label>
            <input
              type="password"
              name="newPassword"
              value={form.newPassword}
              onChange={handleChange}
              className={`input ${errors.newPassword ? 'input-error' : ''}`}
            />
            {errors.newPassword && <p className="error-text">{errors.newPassword}</p>}
          </div>

          <div>
            <label className="label">Confirm New Password *</label>
            <input
              type="password"
              name="confirmPassword"
              value={form.confirmPassword}
              onChange={handleChange}
              className={`input ${errors.confirmPassword ? 'input-error' : ''}`}
            />
            {errors.confirmPassword && <p className="error-text">{errors.confirmPassword}</p>}
          </div>

          <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
            <button type="button" onClick={() => navigate(-1)} className="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? <LoadingSpinner size="sm" /> : <Lock size={15} />}
              {loading ? 'Updating...' : 'Update Password'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default ChangePassword
