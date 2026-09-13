import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus, Eye, Edit2, UserX, Building2, Filter } from 'lucide-react'
import toast from 'react-hot-toast'
import employeeService from '../../services/employeeService'
import departmentService from '../../services/departmentService'
import { useAuth } from '../../context/AuthContext'
import SearchBar from '../../components/common/SearchBar'
import Pagination from '../../components/common/Pagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate } from '../../utils/dateUtils'
import { ROLES, EMPLOYEE_STATUS } from '../../utils/constants'

function EmployeeList() {
  const navigate = useNavigate()
  const { role } = useAuth()
  const canManage = [ROLES.ADMIN, ROLES.HR].includes(role)
  const isAdmin = role === ROLES.ADMIN

  const [employees, setEmployees] = useState([])
  const [departments, setDepartments] = useState([])
  const [loading, setLoading] = useState(true)

  // Filter & Pagination state
  const [search, setSearch] = useState('')
  const [departmentId, setDepartmentId] = useState('')
  const [status, setStatus] = useState('')
  const [designation, setDesignation] = useState('')
  const [page, setPage] = useState(0)
  const [size] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  // Fetch departments for filter dropdown
  useEffect(() => {
    departmentService.getDepartments()
      .then(data => setDepartments(data))
      .catch(() => console.error('Failed to load departments'))
  }, [])

  const fetchEmployees = useCallback(async () => {
    setLoading(true)
    try {
      const params = {
        page,
        size,
        search: search || undefined,
        department: departmentId || undefined,
        status: status || undefined,
        designation: designation || undefined,
      }
      const data = await employeeService.getEmployees(params)
      setEmployees(data.content || [])
      setTotalPages(data.totalPages || 0)
      setTotalElements(data.totalElements || 0)
    } catch (err) {
      toast.error('Failed to load employees')
    } finally {
      setLoading(false)
    }
  }, [page, size, search, departmentId, status, designation])

  useEffect(() => {
    fetchEmployees()
  }, [fetchEmployees])

  const handleDeactivate = async (id, name) => {
    if (!window.confirm(`Are you sure you want to deactivate employee ${name}?`)) return
    try {
      await employeeService.deleteEmployee(id)
      toast.success(`Employee ${name} deactivated`)
      fetchEmployees()
    } catch (err) {
      toast.error('Failed to deactivate employee')
    }
  }

  const getStatusBadge = (empStatus) => {
    switch (empStatus) {
      case EMPLOYEE_STATUS.ACTIVE:
        return <span className="badge-green">Active</span>
      case EMPLOYEE_STATUS.INACTIVE:
        return <span className="badge-yellow">Inactive</span>
      case EMPLOYEE_STATUS.TERMINATED:
        return <span className="badge-red">Terminated</span>
      default:
        return <span className="badge-gray">{empStatus}</span>
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Employees</h1>
          <p className="page-subtitle">View and manage employee directory and records</p>
        </div>
        {canManage && (
          <button
            onClick={() => navigate('/employees/new')}
            className="btn-primary"
          >
            <Plus size={16} />
            Add Employee
          </button>
        )}
      </div>

      {/* Filters Bar */}
      <div className="card p-4">
        <div className="flex flex-col lg:flex-row gap-4 items-center justify-between">
          <SearchBar
            value={search}
            onChange={(val) => { setSearch(val); setPage(0); }}
            placeholder="Search by name, email, code..."
          />

          <div className="flex flex-wrap items-center gap-3 w-full lg:w-auto">
            {/* Department Filter */}
            <select
              value={departmentId}
              onChange={(e) => { setDepartmentId(e.target.value); setPage(0); }}
              className="input w-auto min-w-[150px]"
            >
              <option value="">All Departments</option>
              {departments.map((dept) => (
                <option key={dept.id} value={dept.id}>{dept.name}</option>
              ))}
            </select>

            {/* Status Filter */}
            <select
              value={status}
              onChange={(e) => { setStatus(e.target.value); setPage(0); }}
              className="input w-auto min-w-[130px]"
            >
              <option value="">All Statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
              <option value="TERMINATED">Terminated</option>
            </select>

            {(search || departmentId || status || designation) && (
              <button
                onClick={() => {
                  setSearch('')
                  setDepartmentId('')
                  setStatus('')
                  setDesignation('')
                  setPage(0)
                }}
                className="btn btn-secondary btn-sm"
              >
                Reset
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <LoadingSpinner size="lg" />
          </div>
        ) : employees.length === 0 ? (
          <div className="text-center py-16">
            <Building2 size={40} className="mx-auto text-gray-300 mb-3" />
            <p className="text-gray-500 font-medium">No employees found</p>
            <p className="text-gray-400 text-sm mt-1">Try adjusting your search or filters</p>
          </div>
        ) : (
          <div className="table-container border-0">
            <table className="table">
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Department</th>
                  <th>Designation</th>
                  <th>Joining Date</th>
                  <th>Status</th>
                  <th className="text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {employees.map((emp) => (
                  <tr key={emp.id}>
                    <td>
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-primary-100 text-primary-700 font-bold flex items-center justify-center text-sm">
                          {emp.firstName?.[0]}{emp.lastName?.[0]}
                        </div>
                        <div>
                          <div className="font-semibold text-gray-900">{emp.fullName}</div>
                          <div className="text-xs text-gray-400">{emp.employeeCode} • {emp.email}</div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className="text-gray-800">{emp.departmentName || '—'}</span>
                    </td>
                    <td>
                      <span className="text-gray-700">{emp.designation || '—'}</span>
                    </td>
                    <td>
                      <span className="text-gray-600">{formatDate(emp.joiningDate)}</span>
                    </td>
                    <td>
                      {getStatusBadge(emp.status)}
                    </td>
                    <td className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => navigate(`/employees/${emp.id}`)}
                          className="p-1.5 text-gray-500 hover:text-primary-600 rounded-lg hover:bg-gray-100"
                          title="View Details"
                        >
                          <Eye size={16} />
                        </button>

                        {canManage && (
                          <button
                            onClick={() => navigate(`/employees/${emp.id}/edit`)}
                            className="p-1.5 text-gray-500 hover:text-amber-600 rounded-lg hover:bg-gray-100"
                            title="Edit Employee"
                          >
                            <Edit2 size={16} />
                          </button>
                        )}

                        {isAdmin && emp.status !== EMPLOYEE_STATUS.TERMINATED && (
                          <button
                            onClick={() => handleDeactivate(emp.id, emp.fullName)}
                            className="p-1.5 text-gray-500 hover:text-red-600 rounded-lg hover:bg-gray-100"
                            title="Deactivate"
                          >
                            <UserX size={16} />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination
          currentPage={page}
          totalPages={totalPages}
          totalElements={totalElements}
          pageSize={size}
          onPageChange={(newPage) => setPage(newPage)}
        />
      </div>
    </div>
  )
}

export default EmployeeList
