import { useState, useEffect } from 'react'
import { Plus, Edit2, Trash2, CalendarCheck, Shield } from 'lucide-react'
import toast from 'react-hot-toast'
import leaveService from '../../services/leaveService'
import { useAuth } from '../../context/AuthContext'
import Modal from '../../components/common/Modal'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { ROLES } from '../../utils/constants'

function LeaveTypes() {
  const { role } = useAuth()
  const canManage = [ROLES.ADMIN, ROLES.HR].includes(role)
  const isAdmin = role === ROLES.ADMIN

  const [leaveTypes, setLeaveTypes] = useState([])
  const [loading, setLoading] = useState(true)

  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingType, setEditingType] = useState(null)
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    maxDaysPerYear: 12,
    isPaid: true,
    applicableGender: '',
    requiresDocument: false,
    minNoticeDays: 0,
    isActive: true,
  })
  const [submitting, setSubmitting] = useState(false)

  const fetchLeaveTypes = async () => {
    setLoading(true)
    try {
      const data = await leaveService.getLeaveTypes(false)
      setLeaveTypes(data || [])
    } catch (err) {
      toast.error('Failed to load leave types')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchLeaveTypes()
  }, [])

  const handleOpenCreate = () => {
    setEditingType(null)
    setFormData({
      name: '',
      description: '',
      maxDaysPerYear: 12,
      isPaid: true,
      applicableGender: '',
      requiresDocument: false,
      minNoticeDays: 0,
      isActive: true,
    })
    setIsModalOpen(true)
  }

  const handleOpenEdit = (lt) => {
    setEditingType(lt)
    setFormData({
      name: lt.name,
      description: lt.description || '',
      maxDaysPerYear: lt.maxDaysPerYear,
      isPaid: lt.isPaid,
      applicableGender: lt.applicableGender || '',
      requiresDocument: lt.requiresDocument,
      minNoticeDays: lt.minNoticeDays,
      isActive: lt.isActive,
    })
    setIsModalOpen(true)
  }

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Deactivate leave type "${name}"?`)) return
    try {
      await leaveService.deleteLeaveType(id)
      toast.success(`Leave type "${name}" deactivated`)
      fetchLeaveTypes()
    } catch (err) {
      toast.error('Failed to deactivate leave type')
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!formData.name.trim()) {
      toast.error('Leave type name is required')
      return
    }

    setSubmitting(true)
    try {
      const payload = {
        ...formData,
        maxDaysPerYear: Number(formData.maxDaysPerYear),
        minNoticeDays: Number(formData.minNoticeDays),
        applicableGender: formData.applicableGender || null,
      }

      if (editingType) {
        await leaveService.updateLeaveType(editingType.id, payload)
        toast.success('Leave type updated')
      } else {
        await leaveService.createLeaveType(payload)
        toast.success('Leave type created and allocated to employees')
      }
      setIsModalOpen(false)
      fetchLeaveTypes()
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
          <h1 className="page-title">Leave Types & Quotas</h1>
          <p className="page-subtitle">Configure annual leave categories, paid status, and employee quotas</p>
        </div>
        {canManage && (
          <button onClick={handleOpenCreate} className="btn-primary">
            <Plus size={16} /> Add Leave Type
          </button>
        )}
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-20">
          <LoadingSpinner size="lg" />
        </div>
      ) : leaveTypes.length === 0 ? (
        <div className="card text-center py-16">
          <CalendarCheck size={40} className="mx-auto text-gray-300 mb-3" />
          <p className="text-gray-600 font-medium">No leave types configured yet</p>
          {canManage && (
            <button onClick={handleOpenCreate} className="btn-primary btn-sm mt-4">
              Create First Leave Type
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {leaveTypes.map((lt) => (
            <div key={lt.id} className="card-hover flex flex-col justify-between">
              <div className="space-y-3">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="font-bold text-gray-900 text-base">{lt.name}</h3>
                    <div className="flex gap-1.5 mt-1">
                      <span className={`px-2 py-0.5 rounded text-xs font-semibold ${
                        lt.isPaid ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
                      }`}>
                        {lt.isPaid ? 'Paid' : 'Unpaid'}
                      </span>
                      <span className={`px-2 py-0.5 rounded text-xs font-semibold ${
                        lt.isActive ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-600'
                      }`}>
                        {lt.isActive ? 'Active' : 'Disabled'}
                      </span>
                    </div>
                  </div>

                  {canManage && (
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => handleOpenEdit(lt)}
                        className="p-1.5 text-gray-400 hover:text-amber-600 rounded-lg hover:bg-gray-100"
                        title="Edit"
                      >
                        <Edit2 size={16} />
                      </button>
                      {isAdmin && lt.isActive && (
                        <button
                          onClick={() => handleDelete(lt.id, lt.name)}
                          className="p-1.5 text-gray-400 hover:text-red-600 rounded-lg hover:bg-gray-100"
                          title="Deactivate"
                        >
                          <Trash2 size={16} />
                        </button>
                      )}
                    </div>
                  )}
                </div>

                <p className="text-gray-500 text-xs min-h-[32px]">
                  {lt.description || 'No description provided.'}
                </p>

                <div className="pt-3 border-t border-gray-100 space-y-1.5 text-xs text-gray-600">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Annual Allowance:</span>
                    <span className="font-bold text-gray-900">{lt.maxDaysPerYear} days / year</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Applicable Gender:</span>
                    <span className="font-medium text-gray-800">{lt.applicableGender || 'All Employees'}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Document Upload:</span>
                    <span className="font-medium text-gray-800">{lt.requiresDocument ? 'Required' : 'Optional'}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Notice Period:</span>
                    <span className="font-medium text-gray-800">{lt.minNoticeDays} days advance notice</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingType ? 'Edit Leave Type' : 'Add Leave Type'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label">Leave Type Name *</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="input"
              placeholder="e.g. Casual Leave, Sick Leave, Earned Leave"
            />
          </div>

          <div>
            <label className="label">Description</label>
            <textarea
              rows={2}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="input"
              placeholder="Policy details..."
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Annual Quota (Days) *</label>
              <input
                type="number"
                min="0"
                value={formData.maxDaysPerYear}
                onChange={(e) => setFormData({ ...formData, maxDaysPerYear: e.target.value })}
                className="input"
              />
            </div>

            <div>
              <label className="label">Applicable Gender</label>
              <select
                value={formData.applicableGender}
                onChange={(e) => setFormData({ ...formData, applicableGender: e.target.value })}
                className="input"
              >
                <option value="">All Employees</option>
                <option value="MALE">Male Only (Paternity)</option>
                <option value="FEMALE">Female Only (Maternity)</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Notice Days in Advance</label>
              <input
                type="number"
                min="0"
                value={formData.minNoticeDays}
                onChange={(e) => setFormData({ ...formData, minNoticeDays: e.target.value })}
                className="input"
              />
            </div>

            <div className="space-y-2 pt-6">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={formData.isPaid}
                  onChange={(e) => setFormData({ ...formData, isPaid: e.target.checked })}
                  className="rounded text-primary-600 focus:ring-primary-500"
                />
                <span className="text-sm font-medium text-gray-700">Paid Leave</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={formData.requiresDocument}
                  onChange={(e) => setFormData({ ...formData, requiresDocument: e.target.checked })}
                  className="rounded text-primary-600 focus:ring-primary-500"
                />
                <span className="text-sm font-medium text-gray-700">Requires Doctor Note/Document</span>
              </label>
            </div>
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
              {submitting ? 'Saving...' : editingType ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default LeaveTypes
