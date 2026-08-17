import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const hrLinks = [
  { to: '/hr/dashboard',    label: '🏠 Dashboard' },
  { to: '/hr/employees',    label: '👥 Employees' },
  { to: '/hr/departments',  label: '🏢 Departments' },
  { to: '/hr/attendance',   label: '📋 Attendance' },
  { to: '/hr/leave',        label: '📅 Leave Requests' },
  { to: '/hr/jobs',         label: '💼 Job Postings' },
  { to: '/hr/candidates',   label: '🤖 Candidates (AI)' },
];

const employeeLinks = [
  { to: '/emp/dashboard',   label: '🏠 Dashboard' },
  { to: '/emp/attendance',  label: '📋 My Attendance' },
  { to: '/emp/leave',       label: '📅 My Leaves' },
];

export default function Sidebar() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const links = user?.role === 'HR' ? hrLinks : employeeLinks;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="w-64 min-h-screen bg-slate-900 text-white flex flex-col">
      {/* Logo */}
      <div className="px-6 py-5 border-b border-slate-700">
        <h1 className="text-lg font-bold text-indigo-400">⚡ SmartHR</h1>
        <p className="text-xs text-slate-400 mt-1">{user?.email}</p>
        <span className="inline-block mt-1 px-2 py-0.5 rounded text-xs font-semibold bg-indigo-600">
          {user?.role}
        </span>
      </div>

      {/* Nav links */}
      <nav className="flex-1 px-3 py-4 space-y-1">
        {links.map((link) => (
          <Link
            key={link.to}
            to={link.to}
            className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-colors
              ${location.pathname.startsWith(link.to)
                ? 'bg-indigo-600 text-white font-semibold'
                : 'text-slate-300 hover:bg-slate-700 hover:text-white'
              }`}
          >
            {link.label}
          </Link>
        ))}
      </nav>

      {/* Logout */}
      <div className="px-4 py-4 border-t border-slate-700">
        <button
          onClick={handleLogout}
          className="w-full text-sm text-slate-400 hover:text-red-400 transition-colors text-left px-2 py-1"
        >
          🚪 Logout
        </button>
      </div>
    </aside>
  );
}
