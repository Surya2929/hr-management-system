import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';
import { useAuth } from '../../context/AuthContext';
import { FiCheck, FiClock, FiCalendar } from 'react-icons/fi';

export default function EmployeeDashboard() {
  const { user }  = useAuth();
  const [emp, setEmp]     = useState(null);
  const [today, setToday] = useState(null);
  const [balance, setBalance] = useState(null);

  useEffect(() => {
    axiosInstance.get('/employees/me').then(r => setEmp(r.data)).catch(console.error);
    axiosInstance.get('/attendance/me/today').then(r => setToday(r.data)).catch(console.error);
    axiosInstance.get('/leave/balance').then(r => setBalance(r.data)).catch(console.error);
  }, []);

  const markPresent = async () => {
    try {
      const res = await axiosInstance.post('/attendance/mark', { status: 'PRESENT' });
      setToday(res.data);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to mark attendance');
    }
  };

  const initials = emp ? `${emp.firstName?.[0] || ''}${emp.lastName?.[0] || ''}`.toUpperCase() : '';
  const usedPct = balance?.totalAllowed ? Math.min(100, (balance.daysUsed / balance.totalAllowed) * 100) : 0;

  return (
    <div>
      <h2 className="text-3xl sm:text-4xl font-light text-neutral-900 mb-6">My Dashboard</h2>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Profile: orange highlight card */}
        <div className="rounded-3xl bg-brand-500 text-white p-6 flex flex-col justify-between min-h-[220px] relative overflow-hidden">
          <div className="absolute -bottom-24 -right-16 w-64 h-64 rounded-full bg-white/40 blur-3xl pointer-events-none" aria-hidden="true" />
          <div className="relative flex items-center justify-between">
            <span className="w-14 h-14 rounded-full bg-white text-brand-600 grid place-items-center text-lg font-medium">
              {initials || '…'}
            </span>
            <span className="text-xs bg-white/90 text-neutral-900 rounded-full px-3 py-1">
              {emp?.department?.name || 'No Department'}
            </span>
          </div>
          <div className="relative">
            <h3 className="text-3xl font-light tracking-tight">
              {emp ? `${emp.firstName} ${emp.lastName}` : '…'}
            </h3>
            <p className="text-sm text-neutral-950">{emp?.designation || '—'}</p>
            <p className="text-xs text-neutral-900 mt-2 truncate">{user?.email} · Joined {emp?.dateJoined || '—'}</p>
          </div>
        </div>

        {/* Today's attendance */}
        <div className="card flex flex-col justify-between min-h-[220px]">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="stat-label">Today</p>
              <h3 className="text-xl font-normal">Attendance</h3>
            </div>
            <span className="icon-btn"><FiClock className="w-[18px] h-[18px]" aria-hidden="true" /></span>
          </div>
          {today ? (
            <div>
              <p className="stat-value">{today.checkIn?.slice(0, 5) || '—'}</p>
              <p className="text-sm text-neutral-500 mt-1 flex flex-wrap items-center gap-2">
                <span className={today.status === 'PRESENT' ? 'badge-approved' : 'badge-pending'}>{today.status}</span>
                Check-out {today.checkOut?.slice(0, 5) || '—'}
              </p>
            </div>
          ) : (
            <div>
              <p className="text-sm text-neutral-500 mb-3">Not marked yet today.</p>
              <button onClick={markPresent} className="btn-primary text-sm">
                <FiCheck aria-hidden="true" /> Mark Present
              </button>
            </div>
          )}
        </div>

        {/* Leave balance */}
        <div className="card flex flex-col justify-between min-h-[220px]">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="stat-label">Leave Balance {balance?.year}</p>
              <h3 className="text-xl font-normal">Days Remaining</h3>
            </div>
            <span className="icon-btn"><FiCalendar className="w-[18px] h-[18px]" aria-hidden="true" /></span>
          </div>
          {balance ? (
            <div>
              <p className="flex items-baseline gap-2">
                <span className="stat-value">{balance.daysRemaining}</span>
                <span className="text-neutral-500">/ {balance.totalAllowed} days</span>
              </p>
              <div className="mt-4 h-2 bg-neutral-200 rounded-full overflow-hidden">
                <div className="h-2 bg-brand-500 rounded-full transition-all" style={{ width: `${usedPct}%` }} />
              </div>
              <p className="text-xs text-neutral-500 mt-2">{balance.daysUsed} days used</p>
            </div>
          ) : <p className="text-neutral-400 text-sm">Loading…</p>}
        </div>
      </div>
    </div>
  );
}
