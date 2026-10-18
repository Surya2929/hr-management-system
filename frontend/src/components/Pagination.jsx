// Previous / Next buttons for a Spring Page response ({ number, totalPages, totalElements })
export default function Pagination({ page, onChange }) {
  if (!page || page.totalPages <= 1) return null;

  const current = page.number; // 0-based
  return (
    <div className="flex items-center justify-between gap-3 mt-4 text-sm">
      <button
        type="button"
        className="btn-secondary text-sm py-1.5 disabled:opacity-50 disabled:cursor-not-allowed"
        disabled={current === 0}
        onClick={() => onChange(current - 1)}
      >
        ← Previous
      </button>
      <span className="text-neutral-500 text-center">
        Page {current + 1} of {page.totalPages}
        <span className="hidden sm:inline"> · {page.totalElements} total</span>
      </span>
      <button
        type="button"
        className="btn-secondary text-sm py-1.5 disabled:opacity-50 disabled:cursor-not-allowed"
        disabled={current >= page.totalPages - 1}
        onClick={() => onChange(current + 1)}
      >
        Next →
      </button>
    </div>
  );
}
