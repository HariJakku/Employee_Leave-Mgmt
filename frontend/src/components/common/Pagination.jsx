import { ChevronLeft, ChevronRight } from 'lucide-react'

function Pagination({ currentPage, totalPages, totalElements, pageSize, onPageChange }) {
  if (totalPages <= 1) return null

  const start = currentPage * pageSize + 1
  const end = Math.min((currentPage + 1) * pageSize, totalElements)

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 py-4 px-2 border-t border-gray-100 mt-4">
      <div className="text-sm text-gray-500">
        Showing <span className="font-semibold text-gray-800">{start}</span> to{' '}
        <span className="font-semibold text-gray-800">{end}</span> of{' '}
        <span className="font-semibold text-gray-800">{totalElements}</span> results
      </div>

      <div className="flex items-center gap-2">
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 0}
          className="btn btn-secondary btn-sm p-1.5"
          aria-label="Previous Page"
        >
          <ChevronLeft size={16} />
        </button>

        <span className="text-sm text-gray-600 px-2">
          Page {currentPage + 1} of {totalPages}
        </span>

        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages - 1}
          className="btn btn-secondary btn-sm p-1.5"
          aria-label="Next Page"
        >
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  )
}

export default Pagination
