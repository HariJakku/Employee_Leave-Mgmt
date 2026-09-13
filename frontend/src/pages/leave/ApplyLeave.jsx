import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Calendar, Send, AlertCircle, ArrowLeft, Info } from 'lucide-react'
import toast from 'react-hot-toast'
import leaveService from '../../services/leaveService'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { calcWorkingDays, todayISO } from '../../utils/dateUtils'

function ApplyLeave() {
  const navigate = useNavigate()
  const location = useLocation()
  const preselectedLeaveTypeId = location.state?.leaveTypeId

  const [balances, setBalances] = useState([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  const [formData, setFormData] = useState({
    leaveTypeId: preselectedLeaveTypeId || '',
    startDate: todayISO(),
    endDate: todayISO(),
    reason: '',
    documentUrl: '',
  })
  const [errors, setErrors] = useState({})

  useEffect(() => {
    leaveService.getMyBalances()
      .then(data => {
        setBalances(data || [])
        if (!preselectedLeaveTypeId && data?.length > 0) {
          setFormData(prev => ({ ...prev, leaveTypeId: data[0].leaveTypeId }))
        }
      })
      .catch(() => toast.error('Failed to load leave balances'))
      .finally(() => setLoading(false))
  }, [preselectedLeaveTypeId])

  const selectedBalance = balances.find(b => String(b.leaveTypeId) === String(formData.leaveTypeId))
  const calculatedDays = calcWorkingDays(formData.startDate, formData.endDate)
  const remainingDays = selectedBalance ? Number(selectedBalance.remainingDays) : 0
  const isOverBalance = calculatedDays > remainingDays

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }))
  }

  const validate = () => {
    const errs = {}
    if (!formData.leaveTypeId) errs.leaveTypeId = 'Select a leave type'
    if (!formData.startDate) errs.startDate = 'Start date is required'
    if (!formData.endDate) errs.endDate = 'End date is required'
    if (formData.startDate && formData.endDate && formData.startDate > formData.endDate) {
      errs.endDate = 'End date cannot be before start date'
    }
    if (!formData.reason.trim()) errs.reason = 'Please provide a reason for leave'
    if (isOverBalance) {
      errs.balance = `Requested ${calculatedDays} days exceeds your available balance (${remainingDays} days)`
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

    setSubmitting(true)
    try {
      await leaveService.applyLeave({
        leaveTypeId: Number(formData.leaveTypeId),
        startDate: formData.startDate,
        endDate: formData.endDate,
        reason: formData.reason.trim(),
        documentUrl: formData.documentUrl || null,
      })
      toast.success('Leave request submitted successfully!')
      navigate('/leave/history')
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to submit leave request'
      toast.error(msg)
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate(-1)}
          className="p-2 rounded-lg hover:bg-gray-100 text-gray-500"
        >
          <ArrowLeft size={20} />
        </button>
        <div>
          <h1 className="page-title">Apply for Leave</h1>
          <p className="page-subtitle">Submit a leave request for manager & HR review</p>
        </div>
      </div>

      <div className="card">
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Leave Type Selector */}
          <div>
            <label className="label">Leave Type *</label>
            <select
              name="leaveTypeId"
              value={formData.leaveTypeId}
              onChange={handleChange}
              className={`input ${errors.leaveTypeId ? 'input-error' : ''}`}
            >
              <option value="">Select Leave Type</option>
              {balances.map(b => (
                <option key={b.id} value={b.leaveTypeId}>
                  {b.leaveTypeName} ({b.remainingDays} days available)
                </option>
              ))}
            </select>
            {errors.leaveTypeId && <p className="error-text">{errors.leaveTypeId}</p>}
          </div>

          {/* Date Range */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Start Date *</label>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className={`input ${errors.startDate ? 'input-error' : ''}`}
              />
              {errors.startDate && <p className="error-text">{errors.startDate}</p>}
            </div>

            <div>
              <label className="label">End Date *</label>
              <input
                type="date"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                min={formData.startDate}
                className={`input ${errors.endDate ? 'input-error' : ''}`}
              />
              {errors.endDate && <p className="error-text">{errors.endDate}</p>}
            </div>
          </div>

          {/* Real-time Days Calculation Banner */}
          <div className={`p-4 rounded-xl border flex items-center justify-between ${
            isOverBalance
              ? 'bg-red-50 border-red-200 text-red-800'
              : 'bg-blue-50 border-blue-200 text-blue-900'
          }`}>
            <div className="flex items-center gap-3">
              <Calendar size={22} className={isOverBalance ? 'text-red-600' : 'text-primary-600'} />
              <div>
                <div className="font-bold text-sm">
                  Total Requested: {calculatedDays} {calculatedDays === 1 ? 'day' : 'days'}
                </div>
                <div className="text-xs text-gray-500">
                  {selectedBalance ? `${selectedBalance.leaveTypeName} Balance: ${remainingDays} days available` : 'Select a leave type'}
                </div>
              </div>
            </div>

            {isOverBalance && (
              <span className="text-xs font-bold text-red-600 px-2.5 py-1 rounded bg-red-100">
                Exceeds Balance
              </span>
            )}
          </div>

          {errors.balance && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs flex items-center gap-2">
              <AlertCircle size={16} />
              <span>{errors.balance}</span>
            </div>
          )}

          {/* Reason */}
          <div>
            <label className="label">Reason for Absence *</label>
            <textarea
              name="reason"
              rows={4}
              value={formData.reason}
              onChange={handleChange}
              placeholder="Please provide detailed reason for taking leave..."
              className={`input ${errors.reason ? 'input-error' : ''}`}
            />
            {errors.reason && <p className="error-text">{errors.reason}</p>}
          </div>

          {/* Document Attachment URL (Optional) */}
          <div>
            <label className="label">Supporting Document URL (Optional)</label>
            <input
              type="url"
              name="documentUrl"
              value={formData.documentUrl}
              onChange={handleChange}
              placeholder="https://drive.google.com/..."
              className="input"
            />
            <p className="text-xs text-gray-400 mt-1">Upload doctor note or hospital certificate if applicable.</p>
          </div>

          {/* Submit buttons */}
          <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
            <button
              type="button"
              onClick={() => navigate('/leave/history')}
              className="btn btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || isOverBalance}
              className="btn-primary"
            >
              {submitting ? <LoadingSpinner size="sm" /> : <Send size={16} />}
              {submitting ? 'Submitting...' : 'Submit Request'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default ApplyLeave
