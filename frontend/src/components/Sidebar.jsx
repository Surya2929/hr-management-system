import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const hrLinks = [
  { to: '/hr/dashboard', label: '🏠 Dashboard' },
  { to: '/hr/employees', label: '👥 Employees' },
  { to: '/hr/departments', label: '🏢 Departments' },
  { to: '/hr/attendance', label: '📋 Attendance' },
  { to: '/hr/leave', label: '📅 Leave Requests' },
  { to: '/hr/jobs', label: '💼 Job Postings' },
  { to: '/hr/candidates', label: '🤖 Candidates (AI)' },
];

const employeeLinks = [
  { to: '/emp/dashboard', label: '🏠 Dashboard' },
  { to: '/emp/attendance', label: '📋 My Attendance' },
  { to: '/emp/leave', label: '📅 My Leaves' },
  { to: '/emp/jobs', label: '💼 Open Positions' },
];

export default function Sidebar() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const links = user?.role === 'HR' ? hrLinks : employeeLinks;

  const handleLogout = () => {
    setOpen(false);
    logout();
    navigate('/login');
  };

  return (
    <>
      {/* Mobile top bar */}
      <header className="md:hidden fixed top-0 left-0 right-0 z-40 h-16 bg-slate-900 text-white border-b border-slate-700 flex items-center justify-between px-4 shadow-lg">
        <div className="min-w-0">
          <div className="font-bold text-indigo-400">⚡ SmartHR</div>
          <div className="text-[10px] text-slate-400 truncate max-w-[220px]">{user?.email}</div>
        </div>
        <button
          type="button"
          onClick={() => setOpen(true)}
          className="w-11 h-11 rounded-xl bg-slate-800 border border-slate-700 flex items-center justify-center text-2xl"
          aria-label="Open navigation menu"
        >
          ☰
        </button>
      </header>

      {/* Mobile backdrop */}
      {open && (
        <button
          type="button"
          aria-label="Close navigation menu"
          className="md:hidden fixed inset-0 z-40 bg-black/50 backdrop-blur-[1px]"
          onClick={() => setOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed md:sticky top-0 left-0 z-50 md:z-30 w-[280px] md:w-64 h-screen bg-slate-900 text-white flex flex-col shrink-0 shadow-2xl md:shadow-none transition-transform duration-300 ease-out
          ${open ? 'translate-x-0' : '-translate-x-full'} md:translate-x-0`}
      >
        <div className="px-5 py-5 border-b border-slate-700 flex items-start justify-between gap-3">
          <div className="min-w-0">
            <h1 className="text-lg font-bold text-indigo-400">⚡ SmartHR</h1>
            <p className="text-xs text-slate-400 mt-1 truncate">{user?.email}</p>
            <span className="inline-block mt-1 px-2 py-0.5 rounded text-xs font-semibold bg-indigo-600">
              {user?.role}
            </span>
          </div>
          <button
            type="button"
            onClick={() => setOpen(false)}
            className="md:hidden w-9 h-9 rounded-lg bg-slate-800 hover:bg-slate-700 flex items-center justify-center text-lg"
            aria-label="Close navigation menu"
          >
            ✕
          </button>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {links.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              onClick={() => setOpen(false)}
              className={`flex items-center gap-2 px-3 py-2.5 rounded-lg text-sm transition-colors ${
                location.pathname.startsWith(link.to)
                  ? 'bg-indigo-600 text-white font-semibold'
                  : 'text-slate-300 hover:bg-slate-700 hover:text-white'
              }`}
            >
              {link.label}
            </Link>
          ))}
        </nav>

        <div className="px-4 py-4 border-t border-slate-700">
          <button
            type="button"
            onClick={handleLogout}
            className="w-full text-sm text-slate-300 hover:text-red-400 hover:bg-slate-800 transition-colors text-left px-3 py-2.5 rounded-lg"
          >
            🚪 Logout
          </button>
        </div>
      </aside>
    </>
  );
}