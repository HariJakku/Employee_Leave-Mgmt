import { useState, useEffect } from 'react'
import { Plus, Edit2, Trash2, Users, Building2, User } from 'lucide-react'
import toast from 'react-hot-toast'
import departmentService from '../../services/departmentService'
import employeeService from '../../services/employeeService'
import { useAuth } from '../../context/AuthContext'
import Modal from '../../components/common/Modal'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { ROLES } from '../../utils/constants'

function DepartmentList() {
  const { role } = useAuth()
  const canManage = [ROLES.ADMIN, ROLES.HR].includes(role)
  const isAdmin = role === ROLES.ADMIN

  const [departments, setDepartments] = useState([])
  const [employees, setEmployees] = useState([])
  const [loading, setLoading] = useState(true)

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingDept, setEditingDept] = useState(null)
  const [formData, setFormData] = useState({ name: '', description: '', headEmployeeId: '', isActive: true })
  const [formErrors, setFormErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  // Members modal
  const [membersModalOpen, setMembersModalOpen] = useState(false)
  const [selectedDeptName, setSelectedDeptName] = useState('')
  const [departmentMembers, setDepartmentMembers] = useState([])
  const [loadingMembers, setLoadingMembers] = useState(false)

  const fetchDepartments = async () => {
    setLoading(true)
    try {
      const data = await departmentService.getDepartments()
      setDepartments(data || [])
    } catch (err) {
      toast.error('Failed to load departments')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchDepartments()
    // Pre-load active employees for head selector
    employeeService.getEmployees({ size: 100, status: 'ACTIVE' })
      .then(res => setEmployees(res.content || []))
      .catch(console.error)
  }, [])

  const handleOpenCreate = () => {
    setEditingDept(null)
    setFormData({ name: '', description: '', headEmployeeId: '', isActive: true })
    setFormErrors({})
    setIsModalOpen(true)
  }

  const handleOpenEdit = (dept) => {
    setEditingDept(dept)
    setFormData({
      name: dept.name,
      description: dept.description || '',
      headEmployeeId: dept.headEmployeeId || '',
      isActive: dept.isActive ?? true,
    })
    setFormErrors({})
    setIsModalOpen(true)
  }

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete/deactivate "${name}" department?`)) return
    try {
      await departmentService.deleteDepartment(id)
      toast.success(`Department "${name}" processed`)
      fetchDepartments()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete department')
    }
  }

  const handleViewMembers = async (dept) => {
    setSelectedDeptName(dept.name)
    setMembersModalOpen(true)
    setLoadingMembers(true)
    try {
      const members = await departmentService.getDepartmentEmployees(dept.id)
      setDepartmentMembers(members || [])
    } catch (err) {
      toast.error('Failed to load department members')
    } finally {
      setLoadingMembers(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!formData.name.trim()) {
      setFormErrors({ name: 'Department name is required' })
      return
    }

    setSubmitting(true)
    try {
      const payload = {
        name: formData.name.trim(),
        description: formData.description.trim(),
        headEmployeeId: formData.headEmployeeId ? Number(formData.headEmployeeId) : null,
        isActive: formData.isActive,
      }

      if (editingDept) {
        await departmentService.updateDepartment(editingDept.id, payload)
        toast.success('Department updated successfully')
      } else {
        await departmentService.createDepartment(payload)
        toast.success('Department created successfully')
      }
      setIsModalOpen(false)
      fetchDepartments()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">Departments</h1>
          <p className="page-subtitle">Configure organization departments and team leadership</p>
        </div>
        {canManage && (
          <button onClick={handleOpenCreate} className="btn-primary">
            <Plus size={16} /> Add Department
          </button>
        )}
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-20">
          <LoadingSpinner size="lg" />
        </div>
      ) : departments.length === 0 ? (
        <div className="card text-center py-16">
          <Building2 size={40} className="mx-auto text-gray-300 mb-3" />
          <p className="text-gray-600 font-medium">No departments configured yet</p>
          {canManage && (
            <button onClick={handleOpenCreate} className="btn-primary btn-sm mt-4">
              Create First Department
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {departments.map((dept) => (
            <div key={dept.id} className="card-hover flex flex-col justify-between">
              <div className="space-y-3">
                <div className="flex items-start justify-between gap-2">
                  <div className="flex items-center gap-2.5">
                    <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center font-bold">
                      <Building2 size={20} />
                    </div>
                    <div>
                      <h3 className="font-bold text-gray-900 text-base">{dept.name}</h3>
                      <span className={`inline-block px-2 py-0.5 rounded text-xs font-semibold ${
                        dept.isActive ? 'bg-emerald-100 text-emerald-800' : 'bg-gray-100 text-gray-600'
                      }`}>
                        {dept.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </div>
                  </div>

                  {canManage && (
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => handleOpenEdit(dept)}
                        className="p-1.5 text-gray-400 hover:text-amber-600 rounded-lg hover:bg-gray-100"
                        title="Edit Department"
                      >
                        <Edit2 size={16} />
                      </button>
                      {isAdmin && (
                        <button
                          onClick={() => handleDelete(dept.id, dept.name)}
                          className="p-1.5 text-gray-400 hover:text-red-600 rounded-lg hover:bg-gray-100"
                          title="Delete / Deactivate"
                        >
                          <Trash2 size={16} />
                        </button>
                      )}
                    </div>
                  )}
                </div>

                <p className="text-gray-500 text-xs line-clamp-2 min-h-[32px]">
                  {dept.description || 'No description provided.'}
                </p>

                <div className="pt-3 border-t border-gray-100 space-y-2 text-xs text-gray-600">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400 flex items-center gap-1">
                      <User size={13} /> Department Head:
                    </span>
                    <span className="font-semibold text-gray-800">
                      {dept.headEmployeeName || 'Unassigned'}
                    </span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-gray-400 flex items-center gap-1">
                      <Users size={13} /> Team Members:
                    </span>
                    <span className="font-semibold text-gray-800">
                      {dept.employeeCount} {dept.employeeCount === 1 ? 'member' : 'members'}
                    </span>
                  </div>
                </div>
              </div>

              <div className="pt-4 mt-3 border-t border-gray-100">
                <button
                  onClick={() => handleViewMembers(dept)}
                  className="btn btn-secondary btn-sm w-full text-xs font-semibold"
                >
                  <Users size={14} /> View Members ({dept.employeeCount})
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create / Edit Department Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingDept ? 'Edit Department' : 'Add Department'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label">Department Name *</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className={`input ${formErrors.name ? 'input-error' : ''}`}
              placeholder="e.g. Engineering, Human Resources, Finance"
            />
            {formErrors.name && <p className="error-text">{formErrors.name}</p>}
          </div>

          <div>
            <label className="label">Description</label>
            <textarea
              rows={3}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="input"
              placeholder="Describe department responsibilities..."
            />
          </div>

          <div>
            <label className="label">Department Head</label>
            <select
              value={formData.headEmployeeId}
              onChange={(e) => setFormData({ ...formData, headEmployeeId: e.target.value })}
              className="input"
            >
              <option value="">No department head assigned</option>
              {employees.map((emp) => (
                <option key={emp.id} value={emp.id}>
                  {emp.fullName} ({emp.employeeCode} - {emp.designation})
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center gap-2 pt-2">
            <input
              type="checkbox"
              id="isActive"
              checked={formData.isActive}
              onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
              className="h-4 w-4 rounded text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <label htmlFor="isActive" className="text-sm font-medium text-gray-700">
              Department is Active
            </label>
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="btn btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="btn-primary"
            >
              {submitting ? <LoadingSpinner size="sm" /> : null}
              {submitting ? 'Saving...' : editingDept ? 'Update Department' : 'Create Department'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Department Members Modal */}
      <Modal
        isOpen={membersModalOpen}
        onClose={() => setMembersModalOpen(false)}
        title={`${selectedDeptName} — Team Members`}
      >
        {loadingMembers ? (
          <div className="flex justify-center items-center py-10">
            <LoadingSpinner size="md" />
          </div>
        ) : departmentMembers.length === 0 ? (
          <p className="text-center text-gray-500 py-6">No employees currently assigned to this department.</p>
        ) : (
          <div className="divide-y divide-gray-100">
            {departmentMembers.map((member) => (
              <div key={member.id} className="py-3 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-700 font-bold text-xs flex items-center justify-center">
                    {member.firstName?.[0]}{member.lastName?.[0]}
                  </div>
                  <div>
                    <div className="font-semibold text-gray-900 text-sm">{member.fullName}</div>
                    <div className="text-xs text-gray-400">{member.designation || 'Staff'} • {member.email}</div>
                  </div>
                </div>
                <span className="text-xs font-mono text-gray-500">{member.employeeCode}</span>
              </div>
            ))}
          </div>
        )}
      </Modal>
    </div>
  )
}

export default DepartmentList
