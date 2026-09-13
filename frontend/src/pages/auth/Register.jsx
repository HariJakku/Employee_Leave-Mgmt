import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Building2, Lock, Mail, User, Phone, ArrowLeft, Shield } from 'lucide-react'
import toast from 'react-hot-toast'
import authService from '../../services/authService'
import departmentService from '../../services/departmentService'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { ROLES } from '../../utils/constants'

function Register() {
  const navigate = useNavigate()
  const [departments, setDepartments] = useState([])
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    role: ROLES.EMPLOYEE,
    departmentId: '',
    designation: '',
    phone: '',
  })
  const [errors, setErrors] = useState({})

  useEffect(() => {
    departmentService.getDepartments()
      .then(data => setDepartments(data || []))
      .catch(console.error)
  }, [])

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }))
  }

  const validate = () => {
    const errs = {}
    if (!form.firstName.trim()) errs.firstName = 'First name is required'
    if (!form.lastName.trim()) errs.lastName = 'Last name is required'
    if (!form.email.trim()) errs.email = 'Email is required'
    if (!form.password || form.password.length < 6) errs.password = 'Password must be at least 6 characters'
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
      await authService.register({
        ...form,
        departmentId: form.departmentId ? Number(form.departmentId) : null,
      })
      toast.success('User registered successfully!')
      navigate('/login')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
      <div className="w-full max-w-lg card space-y-6">
        <div className="flex items-center gap-3 border-b border-gray-100 pb-4">
          <div className="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center text-white">
            <Building2 size={20} />
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-900">Create Account</h2>
            <p className="text-xs text-gray-500">Register employee account credentials</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label">First Name *</label>
              <input
                type="text"
                name="firstName"
                value={form.firstName}
                onChange={handleChange}
                className={`input ${errors.firstName ? 'input-error' : ''}`}
                placeholder="Jane"
              />
              {errors.firstName && <p className="error-text">{errors.firstName}</p>}
            </div>

            <div>
              <label className="label">Last Name *</label>
              <input
                type="text"
                name="lastName"
                value={form.lastName}
                onChange={handleChange}
                className={`input ${errors.lastName ? 'input-error' : ''}`}
                placeholder="Doe"
              />
              {errors.lastName && <p className="error-text">{errors.lastName}</p>}
            </div>
          </div>

          <div>
            <label className="label">Work Email *</label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              className={`input ${errors.email ? 'input-error' : ''}`}
              placeholder="jane.doe@company.com"
            />
            {errors.email && <p className="error-text">{errors.email}</p>}
          </div>

          <div>
            <label className="label">Password *</label>
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              className={`input ${errors.password ? 'input-error' : ''}`}
              placeholder="Min 6 characters"
            />
            {errors.password && <p className="error-text">{errors.password}</p>}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label">Department</label>
              <select
                name="departmentId"
                value={form.departmentId}
                onChange={handleChange}
                className="input"
              >
                <option value="">Select Department</option>
                {departments.map(d => (
                  <option key={d.id} value={d.id}>{d.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="label">Role</label>
              <select name="role" value={form.role} onChange={handleChange} className="input">
                <option value="EMPLOYEE">Employee</option>
                <option value="MANAGER">Manager</option>
                <option value="HR">HR</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          </div>

          <div>
            <label className="label">Designation</label>
            <input
              type="text"
              name="designation"
              value={form.designation}
              onChange={handleChange}
              className="input"
              placeholder="e.g. QA Engineer"
            />
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full btn-lg"
            >
              {loading ? <LoadingSpinner size="sm" /> : null}
              {loading ? 'Creating Account...' : 'Register User'}
            </button>
          </div>

          <div className="text-center pt-2">
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="text-xs text-primary-600 hover:text-primary-700 font-semibold"
            >
              Already have an account? Sign in
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default Register
