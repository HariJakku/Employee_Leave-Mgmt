import { format, parseISO, differenceInDays, isValid } from 'date-fns'

/**
 * Format an ISO date string or Date object for display.
 * @param {string|Date} date
 * @param {string} fmt - date-fns format string (default: 'dd MMM yyyy')
 */
export const formatDate = (date, fmt = 'dd MMM yyyy') => {
  if (!date) return '—'
  try {
    const d = typeof date === 'string' ? parseISO(date) : date
    return isValid(d) ? format(d, fmt) : '—'
  } catch {
    return '—'
  }
}

/**
 * Format a datetime string to "dd MMM yyyy, HH:mm"
 */
export const formatDateTime = (date) => formatDate(date, 'dd MMM yyyy, HH:mm')

/**
 * Format a time string or Date to "HH:mm"
 */
export const formatTime = (date) => formatDate(date, 'HH:mm')

/**
 * Calculate number of working days between two ISO date strings (inclusive).
 */
export const calcWorkingDays = (startDate, endDate) => {
  if (!startDate || !endDate) return 0
  const start = parseISO(startDate)
  const end = parseISO(endDate)
  return Math.max(0, differenceInDays(end, start) + 1)
}

/**
 * Return today's date as 'yyyy-MM-dd' (for HTML date input value).
 */
export const todayISO = () => format(new Date(), 'yyyy-MM-dd')

/**
 * Convert decimal hours to "Xh Ym" format.
 * E.g. 8.5 → "8h 30m"
 */
export const formatWorkingHours = (decimalHours) => {
  if (!decimalHours && decimalHours !== 0) return '—'
  const h = Math.floor(decimalHours)
  const m = Math.round((decimalHours - h) * 60)
  return `${h}h ${m}m`
}
