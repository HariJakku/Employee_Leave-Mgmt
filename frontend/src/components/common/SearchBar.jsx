import { Search, X } from 'lucide-react'

function SearchBar({ value, onChange, placeholder = "Search...", onClear }) {
  return (
    <div className="relative flex-1 min-w-[200px] max-w-md">
      <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="input pl-9 pr-8"
      />
      {value && (
        <button
          onClick={onClear || (() => onChange(''))}
          className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          type="button"
        >
          <X size={14} />
        </button>
      )}
    </div>
  )
}

export default SearchBar
