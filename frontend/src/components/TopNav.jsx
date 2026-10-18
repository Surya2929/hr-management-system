import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  FiHome, FiUsers, FiLayers, FiClock, FiCalendar, FiBriefcase, FiCpu,
  FiSettings, FiLogOut, FiMenu, FiX,
} from 'react-icons/fi';
import { useAuth } from '../context/AuthContext';

const hrLinks = [
  { to: '/hr/dashboard',   label: 'Dashboard',   icon: FiHome },
  { to: '/hr/employees',   label: 'Employees',   icon: FiUsers },
  { to: '/hr/departments', label: 'Departments', icon: FiLayers },
  { to: '/hr/attendance',  label: 'Attendance',  icon: FiClock },
  { to: '/hr/leave',       label: 'Leave',       icon: FiCalendar },
  { to: '/hr/jobs',        label: 'Jobs',        icon: FiBriefcase },
  { to: '/hr/candidates',  label: 'Candidates',  icon: FiCpu },
];

const employeeLinks = [
  { to: '/emp/dashboard',  label: 'Dashboard',      icon: FiHome },
  { to: '/emp/attendance', label: 'My Attendance',  icon: FiClock },
  { to: '/emp/leave',      label: 'My Leaves',      icon: FiCalendar },
  { to: '/emp/jobs',       label: 'Open Positions', icon: FiBriefcase },
];

// Small orange square logo + name
function Logo() {
  return (
    <Link to="/" className="flex items-center gap-2.5 shrink-0" aria-label="SmartHR home">
      <span className="w-9 h-9 rounded-xl bg-brand-500 text-white grid place-items-center font-semibold">S</span>
      <span className="text-lg font-semibold tracking-tight text-neutral-900">SmartHR</span>
    </Link>
  );
}

export default function TopNav() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false); // mobile menu

  const isHr = user?.role === 'HR';
  const links = isHr ? hrLinks : employeeLinks;
  const settingsPath = isHr ? '/hr/change-password' : '/emp/change-password';
  const initials = (user?.email || '?').slice(0, 2).toUpperCase();
  const isActive = (to) => location.pathname.startsWith(to);

  const handleLogout = () => {
    setOpen(false);
    logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-40 bg-neutral-200/85 backdrop-blur-md">
      <div className="max-w-[1600px] mx-auto h-16 md:h-20 px-4 sm:px-5 md:px-6 lg:px-8 flex items-center justify-between gap-4">
        <Logo />

        {/* Desktop: round icon buttons; the active page expands to show its name */}
        <nav className="hidden md:flex items-center gap-1 p-1.5 rounded-full bg-white/60 border border-white" aria-label="Main">
          {links.map(({ to, label, icon: Icon }) => {
            const active = isActive(to);
            return (
              <Link
                key={to}
                to={to}
                title={label}
                aria-label={label}
                aria-current={active ? 'page' : undefined}
                className={`h-10 rounded-full flex items-center justify-center gap-2 transition-all
                  ${active
                    ? 'bg-neutral-900 text-white px-4'
                    : 'w-10 text-neutral-600 hover:bg-white hover:text-neutral-900'}`}
              >
                <Icon className="w-[18px] h-[18px] shrink-0" aria-hidden="true" />
                {active && <span className="text-sm font-medium whitespace-nowrap hidden lg:inline">{label}</span>}
              </Link>
            );
          })}
        </nav>

        {/* Desktop: settings, user chip, logout */}
        <div className="hidden md:flex items-center gap-2 shrink-0">
          <Link
            to={settingsPath}
            className={`icon-btn ${isActive(settingsPath) ? '!bg-neutral-900 !text-white !border-neutral-900' : ''}`}
            title="Change password"
            aria-label="Change password"
          >
            <FiSettings className="w-[18px] h-[18px]" aria-hidden="true" />
          </Link>
          <div className="flex items-center gap-2.5 pl-1">
            <span className="w-10 h-10 rounded-full bg-neutral-900 text-white grid place-items-center text-sm font-medium">
              {initials}
            </span>
            <div className="hidden xl:block leading-tight">
              <p className="text-sm font-medium text-neutral-900 max-w-[180px] truncate">{user?.email}</p>
              <p className="text-xs text-neutral-500">{isHr ? 'HR Admin' : 'Employee'}</p>
            </div>
          </div>
          <button type="button" onClick={handleLogout} className="icon-btn" title="Logout" aria-label="Logout">
            <FiLogOut className="w-[18px] h-[18px]" aria-hidden="true" />
          </button>
        </div>

        {/* Mobile: menu button */}
        <button
          type="button"
          onClick={() => setOpen(true)}
          className="md:hidden icon-btn w-11 h-11"
          aria-label="Open navigation menu"
        >
          <FiMenu className="w-5 h-5" aria-hidden="true" />
        </button>
      </div>

      {/* Mobile drawer */}
      {open && (
        <div className="md:hidden fixed inset-0 z-50">
          <button
            type="button"
            aria-label="Close navigation menu"
            className="absolute inset-0 bg-black/40"
            onClick={() => setOpen(false)}
          />
          <aside className="absolute right-0 top-0 h-dvh w-[290px] max-w-[85vw] bg-neutral-100 rounded-l-3xl p-4 flex flex-col shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <Logo />
              <button type="button" onClick={() => setOpen(false)} className="icon-btn" aria-label="Close navigation menu">
                <FiX className="w-5 h-5" aria-hidden="true" />
              </button>
            </div>

            <div className="flex items-center gap-3 p-3 rounded-2xl bg-white mb-4">
              <span className="w-10 h-10 rounded-full bg-neutral-900 text-white grid place-items-center text-sm font-medium shrink-0">
                {initials}
              </span>
              <div className="min-w-0 leading-tight">
                <p className="text-sm font-medium truncate">{user?.email}</p>
                <p className="text-xs text-neutral-500">{isHr ? 'HR Admin' : 'Employee'}</p>
              </div>
            </div>

            <nav className="flex-1 overflow-y-auto space-y-1" aria-label="Main">
              {[...links, { to: settingsPath, label: 'Change Password', icon: FiSettings }].map(({ to, label, icon: Icon }) => (
                <Link
                  key={to}
                  to={to}
                  onClick={() => setOpen(false)}
                  className={`flex items-center gap-3 px-4 py-3 rounded-full text-sm transition-colors
                    ${isActive(to) ? 'bg-neutral-900 text-white' : 'text-neutral-700 hover:bg-white'}`}
                >
                  <Icon className="w-[18px] h-[18px]" aria-hidden="true" />
                  {label}
                </Link>
              ))}
            </nav>

            <button type="button" onClick={handleLogout} className="btn-secondary w-full mt-4">
              <FiLogOut className="w-4 h-4" aria-hidden="true" /> Logout
            </button>
          </aside>
        </div>
      )}
    </header>
  );
}
