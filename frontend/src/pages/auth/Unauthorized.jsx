import { useNavigate } from 'react-router-dom'
import { ShieldAlert, ArrowLeft } from 'lucide-react'

function Unauthorized() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
      <div className="card max-w-md w-full text-center py-12 px-8 space-y-4">
        <div className="w-16 h-16 bg-red-100 text-red-600 rounded-2xl flex items-center justify-center mx-auto">
          <ShieldAlert size={32} />
        </div>
        <h1 className="text-2xl font-black text-gray-900">Access Restricted</h1>
        <p className="text-gray-500 text-sm">
          You do not possess the role permissions required to view this administrative resource.
        </p>
        <div className="pt-4">
          <button
            onClick={() => navigate('/dashboard')}
            className="btn-primary w-full"
          >
            <ArrowLeft size={16} /> Return to Dashboard
          </button>
        </div>
      </div>
    </div>
  )
}

export default Unauthorized
