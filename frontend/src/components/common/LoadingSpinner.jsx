import clsx from 'clsx'

/**
 * Reusable loading spinner with configurable sizes.
 * Usage: <LoadingSpinner size="sm" | "md" | "lg" />
 */
function LoadingSpinner({ size = 'md', className = '' }) {
  const sizes = {
    sm: 'h-4 w-4 border-2',
    md: 'h-8 w-8 border-2',
    lg: 'h-12 w-12 border-4',
  }

  return (
    <div
      className={clsx(
        'animate-spin rounded-full border-gray-200 border-t-primary-600',
        sizes[size],
        className,
      )}
      role="status"
      aria-label="Loading"
    />
  )
}

export default LoadingSpinner
