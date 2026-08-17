import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';
import { useAuth } from '../../context/AuthContext';

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

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-800 mb-6">My Dashboard</h2>

      <div className="grid lg:grid-cols-3 gap-5">
        {/* Profile card */}
        <div className="card col-span-1">
          <div className="text-4xl mb-3">👤</div>
          <h3 className="font-bold text-lg text-slate-800">
            {emp ? `${emp.firstName} ${emp.lastName}` : '…'}
          </h3>
          <p className="text-sm text-slate-500">{user?.email}</p>
          <p className="text-sm text-indigo-600 font-medium mt-1">{emp?.designation || '—'}</p>
          <p className="text-xs text-slate-400 mt-1">{emp?.department?.name || 'No Department'}</p>
          <p className="text-xs text-slate-400 mt-1">Joined: {emp?.dateJoined || '—'}</p>
        </div>

        {/* Today's attendance */}
        <div className="card">
          <h3 className="font-semibold text-slate-700 mb-3">📋 Today's Attendance</h3>
          {today ? (
            <div>
              <p className="text-sm mb-1"><span className="font-medium">Status:</span>{' '}
                <span className={today.status === 'PRESENT' ? 'text-emerald-600 font-semibold' : 'text-yellow-600'}>
                  {today.status}
                </span>
              </p>
              <p className="text-sm text-slate-500">Check-in: {today.checkIn || '—'}</p>
              <p className="text-sm text-slate-500">Check-out: {today.checkOut || '—'}</p>
            </div>
          ) : (
            <div>
              <p className="text-sm text-slate-500 mb-3">Not marked yet today.</p>
              <button onClick={markPresent} className="btn-primary text-sm">
                ✓ Mark Present
              </button>
            </div>
          )}
        </div>

        {/* Leave balance */}
        <div className="card">
          <h3 className="font-semibold text-slate-700 mb-3">📅 Leave Balance {balance?.year}</h3>
          {balance ? (
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span>Total Allowed</span>
                <span className="font-semibold">{balance.totalAllowed} days</span>
              </div>
              <div className="flex justify-between text-sm">
                <span>Days Used</span>
                <span className="font-semibold text-yellow-600">{balance.daysUsed} days</span>
              </div>
              <div className="flex justify-between text-sm border-t pt-2">
                <span className="font-medium">Remaining</span>
                <span className="font-bold text-emerald-600">{balance.daysRemaining} days</span>
              </div>
              {/* Progress bar */}
              <div className="mt-2 h-2 bg-slate-200 rounded-full overflow-hidden">
                <div
                  className="h-2 bg-indigo-500 rounded-full transition-all"
                  style={{ width: `${(balance.daysUsed / balance.totalAllowed) * 100}%` }}
                />
              </div>
            </div>
          ) : <p className="text-slate-400 text-sm">Loading…</p>}
        </div>
      </div>
    </div>
  );
}
