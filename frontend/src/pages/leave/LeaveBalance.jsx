import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Calendar, PlusCircle, AlertCircle } from 'lucide-react'
import toast from 'react-hot-toast'
import leaveService from '../../services/leaveService'
import LoadingSpinner from '../../components/common/LoadingSpinner'

function LeaveBalance() {
  const navigate = useNavigate()
  const [balances, setBalances] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    leaveService.getMyBalances()
      .then(data => setBalances(data || []))
      .catch(err => toast.error('Failed to load leave balances'))
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
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1 className="page-title">My Leave Balances</h1>
          <p className="page-subtitle">Track your annual leave entitlement, utilized days, and available balances for {new Date().getFullYear()}</p>
        </div>
        <button
          onClick={() => navigate('/leave/apply')}
          className="btn-primary"
        >
          <PlusCircle size={16} /> Apply Leave
        </button>
      </div>

      {balances.length === 0 ? (
        <div className="card text-center py-16">
          <AlertCircle size={40} className="mx-auto text-gray-300 mb-3" />
          <p className="text-gray-600 font-medium">No leave balances found for current year.</p>
          <p className="text-gray-400 text-sm mt-1">Please contact your HR administrator.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {balances.map((b) => {
            const allocated = Number(b.allocatedDays) || 0
            const used = Number(b.usedDays) || 0
            const pending = Number(b.pendingDays) || 0
            const remaining = Number(b.remainingDays) || 0
            const percentUsed = allocated > 0 ? Math.min(100, Math.round(((used + pending) / allocated) * 100)) : 0

            return (
              <div key={b.id} className="card-hover space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="font-bold text-gray-900 text-base">{b.leaveTypeName}</h3>
                  <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-blue-50 text-blue-700">
                    {remaining} Days Available
                  </span>
                </div>

                {/* Progress bar */}
                <div className="space-y-1.5">
                  <div className="flex justify-between text-xs text-gray-500">
                    <span>Utilization</span>
                    <span className="font-semibold text-gray-700">{percentUsed}%</span>
                  </div>
                  <div className="w-full h-2.5 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-300 ${
                        percentUsed >= 90 ? 'bg-red-500' : percentUsed >= 70 ? 'bg-amber-500' : 'bg-primary-600'
                      }`}
                      style={{ width: `${percentUsed}%` }}
                    />
                  </div>
                </div>

                {/* Metrics Breakdown */}
                <div className="grid grid-cols-3 gap-2 pt-3 border-t border-gray-100 text-center text-xs">
                  <div className="p-2 rounded-lg bg-gray-50">
                    <span className="text-gray-400 block text-[11px]">Total</span>
                    <span className="font-bold text-gray-800 text-sm">{allocated}</span>
                  </div>
                  <div className="p-2 rounded-lg bg-amber-50">
                    <span className="text-amber-700 block text-[11px]">Used</span>
                    <span className="font-bold text-amber-800 text-sm">{used}</span>
                  </div>
                  <div className="p-2 rounded-lg bg-blue-50">
                    <span className="text-blue-700 block text-[11px]">Pending</span>
                    <span className="font-bold text-blue-800 text-sm">{pending}</span>
                  </div>
                </div>

                <button
                  onClick={() => navigate('/leave/apply', { state: { leaveTypeId: b.leaveTypeId } })}
                  disabled={remaining <= 0}
                  className="btn btn-secondary btn-sm w-full text-xs font-semibold"
                >
                  {remaining > 0 ? 'Request this Leave' : 'Quota Exhausted'}
                </button>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

export default LeaveBalance
