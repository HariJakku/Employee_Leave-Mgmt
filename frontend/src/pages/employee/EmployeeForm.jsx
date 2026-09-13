import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Save } from 'lucide-react'
import toast from 'react-hot-toast'
import employeeService from '../../services/employeeService'
import departmentService from '../../services/departmentService'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { ROLES, EMPLOYEE_STATUS, EMPLOYMENT_TYPE } from '../../utils/constants'

function EmployeeForm() {
  const navigate = useNavigate()
  const { id } = useParams()
  const isEdit = Boolean(id)

  const [departments, setDepartments] = useState([])
  const [managers, setManagers] = useState([])
  const [loading, setLoading] = useState(false)
  const [initialLoading, setInitialLoading] = useState(isEdit)

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phone: '',
    dateOfBirth: '',
    gender: 'MALE',
    address: '',
    role: ROLES.EMPLOYEE,
    departmentId: '',
    designation: '',
    joiningDate: '',
    managerId: '',
    employmentType: EMPLOYMENT_TYPE.FULL_TIME,
    salary: '',
    status: EMPLOYEE_STATUS.ACTIVE,
  })

  const [errors, setErrors] = useState({})

  useEffect(() => {
    // Fetch lookup data
    const loadLookups = async () => {
      try {
        const [depts, empList] = await Promise.all([
          departmentService.getDepartments(),
          employeeService.getEmployees({ size: 100, status: 'ACTIVE' }),
        ])
        setDepartments(depts || [])
        // Filter out current employee from potential managers list in edit mode
        const possibleManagers = (empList.content || []).filter(e => !isEdit || e.id !== Number(id))
        setManagers(possibleManagers)
      } catch (err) {
        console.error('Error loading form lookups', err)
      }
    }

    loadLookups()

    if (isEdit) {
      employeeService.getEmployeeById(id)
        .then(emp => {
          setFormData({
            firstName: emp.firstName || '',
            lastName: emp.lastName || '',
            email: emp.email || '',
            password: '', // do not display password
            phone: emp.phone || '',
            dateOfBirth: emp.dateOfBirth || '',
            gender: emp.gender || 'MALE',
            address: emp.address || '',
            role: emp.role || ROLES.EMPLOYEE,
            departmentId: emp.departmentId || '',
            designation: emp.designation || '',
            joiningDate: emp.joiningDate || '',
            managerId: emp.managerId || '',
            employmentType: emp.employmentType || EMPLOYMENT_TYPE.FULL_TIME,
            salary: emp.salary || '',
            status: emp.status || EMPLOYEE_STATUS.ACTIVE,
          })
        })
        .catch(err => {
          toast.error('Failed to load employee details')
          navigate('/employees')
        })
        .finally(() => setInitialLoading(false))
    }
  }, [id, isEdit, navigate])

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }))
  }

  const validate = () => {
    const errs = {}
    if (!formData.firstName.trim()) errs.firstName = 'First name is required'
    if (!formData.lastName.trim()) errs.lastName = 'Last name is required'
    if (!formData.email.trim()) errs.email = 'Email is required'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) errs.email = 'Invalid email'

    if (!isEdit && formData.password && formData.password.length < 6) {
      errs.password = 'Password must be at least 6 characters'
    }

    if (formData.salary && Number(formData.salary) < 0) {
      errs.salary = 'Salary cannot be negative'
    }

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
      const payload = {
        ...formData,
        departmentId: formData.departmentId ? Number(formData.departmentId) : null,
        managerId: formData.managerId ? Number(formData.managerId) : null,
        salary: formData.salary ? Number(formData.salary) : null,
      }

      if (isEdit) {
        await employeeService.updateEmployee(id, payload)
        toast.success('Employee updated successfully!')
      } else {
        await employeeService.createEmployee(payload)
        toast.success('Employee created successfully!')
      }
      navigate('/employees')
    } catch (err) {
      const msg = err.response?.data?.message || 'Operation failed'
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  if (initialLoading) {
    return (
      <div className="flex justify-center items-center py-20">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/employees')}
          className="p-2 rounded-lg hover:bg-gray-200 text-gray-600 transition-colors"
        >
          <ArrowLeft size={20} />
        </button>
        <div>
          <h1 className="page-title">{isEdit ? 'Edit Employee' : 'Add New Employee'}</h1>
          <p className="page-subtitle">
            {isEdit ? 'Update employee records and permissions' : 'Create an employee profile and user login'}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Personal Details Card */}
        <div className="card space-y-4">
          <h2 className="text-base font-bold text-gray-900 border-b border-gray-100 pb-2">
            Personal Information
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="label">First Name *</label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                className={`input ${errors.firstName ? 'input-error' : ''}`}
                placeholder="Jakku"
              />
              {errors.firstName && <p className="error-text">{errors.firstName}</p>}
            </div>

            <div>
              <label className="label">Last Name *</label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                className={`input ${errors.lastName ? 'input-error' : ''}`}
                placeholder="Kumar"
              />
              {errors.lastName && <p className="error-text">{errors.lastName}</p>}
            </div>

            <div>
              <label className="label">Email *</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className={`input ${errors.email ? 'input-error' : ''}`}
                placeholder="harij@company.com"
              />
              {errors.email && <p className="error-text">{errors.email}</p>}
            </div>

            <div>
              <label className="label">Phone Number</label>
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                className="input"
                placeholder="+1 234 567 890"
              />
            </div>

            <div>
              <label className="label">Date of Birth</label>
              <input
                type="date"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleChange}
                className="input"
              />
            </div>

            <div>
              <label className="label">Gender</label>
              <select name="gender" value={formData.gender} onChange={handleChange} className="input">
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <div className="sm:col-span-2">
              <label className="label">Address</label>
              <textarea
                name="address"
                rows={2}
                value={formData.address}
                onChange={handleChange}
                className="input"
                placeholder="123 Corporate Blvd, Suite 400..."
              />
            </div>
          </div>
        </div>

        {/* Employment & Job Details Card */}
        <div className="card space-y-4">
          <h2 className="text-base font-bold text-gray-900 border-b border-gray-100 pb-2">
            Job & Organizational Details
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Department</label>
              <select
                name="departmentId"
                value={formData.departmentId}
                onChange={handleChange}
                className="input"
              >
                <option value="">Select Department</option>
                {departments.map((dept) => (
                  <option key={dept.id} value={dept.id}>{dept.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="label">Designation</label>
              <input
                type="text"
                name="designation"
                value={formData.designation}
                onChange={handleChange}
                className="input"
                placeholder="Senior Software Engineer"
              />
            </div>

            <div>
              <label className="label">Reporting Manager</label>
              <select
                name="managerId"
                value={formData.managerId}
                onChange={handleChange}
                className="input"
              >
                <option value="">No Manager (Top-level)</option>
                {managers.map((mgr) => (
                  <option key={mgr.id} value={mgr.id}>{mgr.fullName} ({mgr.employeeCode})</option>
                ))}
              </select>
            </div>

            <div>
              <label className="label">Employment Type</label>
              <select
                name="employmentType"
                value={formData.employmentType}
                onChange={handleChange}
                className="input"
              >
                <option value="FULL_TIME">Full Time</option>
                <option value="PART_TIME">Part Time</option>
                <option value="CONTRACT">Contract</option>
                <option value="INTERN">Intern</option>
              </select>
            </div>

            <div>
              <label className="label">Joining Date</label>
              <input
                type="date"
                name="joiningDate"
                value={formData.joiningDate}
                onChange={handleChange}
                className="input"
              />
            </div>

            <div>
              <label className="label">Annual Salary (USD)</label>
              <input
                type="number"
                name="salary"
                value={formData.salary}
                onChange={handleChange}
                className={`input ${errors.salary ? 'input-error' : ''}`}
                placeholder="85000"
              />
              {errors.salary && <p className="error-text">{errors.salary}</p>}
            </div>

            <div>
              <label className="label">Status</label>
              <select name="status" value={formData.status} onChange={handleChange} className="input">
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
                <option value="TERMINATED">Terminated</option>
              </select>
            </div>
          </div>
        </div>

        {/* Security & Access Card */}
        <div className="card space-y-4">
          <h2 className="text-base font-bold text-gray-900 border-b border-gray-100 pb-2">
            Account & Security
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Access Role *</label>
              <select name="role" value={formData.role} onChange={handleChange} className="input">
                <option value="EMPLOYEE">Employee (Self-service)</option>
                <option value="MANAGER">Manager (Team approvals)</option>
                <option value="HR">HR (Personnel & Policies)</option>
                <option value="ADMIN">Admin (Full System Access)</option>
              </select>
            </div>

            {!isEdit && (
              <div>
                <label className="label">Initial Password</label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Default: Employee@123"
                  className={`input ${errors.password ? 'input-error' : ''}`}
                />
                {errors.password && <p className="error-text">{errors.password}</p>}
                <p className="text-xs text-gray-400 mt-1">Leave blank to use default (Employee@123)</p>
              </div>
            )}
          </div>
        </div>

        {/* Submit Bar */}
        <div className="flex items-center justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={() => navigate('/employees')}
            className="btn btn-secondary"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="btn-primary"
          >
            {loading ? <LoadingSpinner size="sm" /> : <Save size={16} />}
            {loading ? 'Saving...' : isEdit ? 'Update Employee' : 'Create Employee'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default EmployeeForm
