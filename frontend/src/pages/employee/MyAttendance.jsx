import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';

export default function MyAttendance() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [marking, setMarking] = useState(false);
  const [form, setForm]       = useState({ status: 'PRESENT', checkIn: '', checkOut: '', remarks: '' });
  const [today, setToday]     = useState(null);   // today's record (null = not checked in)
  const [checkingOut, setCheckingOut] = useState(false);

  const fetch = () => {
    axiosInstance.get('/attendance/me')
      .then(r => setHistory(r.data))
      .finally(() => setLoading(false));
    axiosInstance.get('/attendance/me/today')
      .then(r => setToday(r.data || null))
      .catch(console.error);
  };
  useEffect(fetch, []);

  const handleCheckOut = async () => {
    setCheckingOut(true);
    try {
      await axiosInstance.post('/attendance/checkout');
      fetch();
    } catch (err) {
      alert(err.response?.data?.message || 'Check out failed');
    } finally {
      setCheckingOut(false);
    }
  };

  // Check Out is allowed only after check-in and only once
  const canCheckOut = Boolean(today?.checkIn) && !today?.checkOut;

  const handleMark = async (e) => {
    e.preventDefault();
    setMarking(true);
    try {
      const body = { status: form.status };
      if (form.checkIn)  body.checkIn  = form.checkIn + ':00';
      if (form.checkOut) body.checkOut = form.checkOut + ':00';
      if (form.remarks)  body.remarks  = form.remarks;
      await axiosInstance.post('/attendance/mark', body);
      fetch();
      setForm({ status: 'PRESENT', checkIn: '', checkOut: '', remarks: '' });
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    } finally {
      setMarking(false);
    }
  };

  const statusBadge = (s) => {
    const map = { PRESENT: 'badge-approved', ABSENT: 'badge-rejected', HALF_DAY: 'badge-pending', ON_LEAVE: 'badge-pending' };
    return <span className={map[s] || ''}>{s}</span>;
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-800 mb-6">My Attendance</h2>

      {/* Today: check-in / check-out status */}
      <div className="card mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="text-sm space-y-1">
          <p className="font-semibold text-slate-700">
            Today{' '}
            {today?.late && <span className="badge-rejected ml-1">Late</span>}
          </p>
          <p className="text-slate-500">
            Check-in: <span className="font-medium text-slate-700">{today?.checkIn || '—'}</span>
            {' · '}
            Check-out: <span className="font-medium text-slate-700">{today?.checkOut || '—'}</span>
            {today?.workingHours != null && (
              <> · Worked: <span className="font-medium text-emerald-700">{today.workingHours} h</span></>
            )}
          </p>
        </div>
        <button
          type="button"
          onClick={handleCheckOut}
          disabled={!canCheckOut || checkingOut}
          className="btn-primary text-sm w-full sm:w-auto"
          title={canCheckOut ? '' : today?.checkOut ? 'Already checked out today' : 'Check in first'}
        >
          {checkingOut ? 'Checking out…' : '⏏ Check Out'}
        </button>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <div className="card">
          <h3 className="font-semibold text-slate-700 mb-4">Mark Today's Attendance</h3>
          <form onSubmit={handleMark} className="space-y-3">
            <div>
              <label className="label" htmlFor="attStatus">Status</label>
              <select id="attStatus" className="input"
                value={form.status} onChange={e => setForm(f => ({ ...f, status: e.target.value }))}>
                <option value="PRESENT">Present</option>
                <option value="ABSENT">Absent</option>
                <option value="HALF_DAY">Half Day</option>
                <option value="ON_LEAVE">On Leave</option>
              </select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="label" htmlFor="checkIn">Check In</label>
                <input id="checkIn" type="time" className="input"
                  value={form.checkIn} onChange={e => setForm(f => ({ ...f, checkIn: e.target.value }))} />
              </div>
              <div>
                <label className="label" htmlFor="checkOut">Check Out</label>
                <input id="checkOut" type="time" className="input"
                  value={form.checkOut} onChange={e => setForm(f => ({ ...f, checkOut: e.target.value }))} />
              </div>
            </div>
            <div>
              <label className="label" htmlFor="attRemarks">Remarks (optional)</label>
              <input id="attRemarks" className="input" placeholder="e.g. Working from home"
                value={form.remarks} onChange={e => setForm(f => ({ ...f, remarks: e.target.value }))} />
            </div>
            <button type="submit" disabled={marking} className="btn-primary">
              {marking ? 'Saving…' : '✓ Submit Attendance'}
            </button>
          </form>
        </div>

        <div className="card">
          <h3 className="font-semibold text-slate-700 mb-4">Attendance History</h3>
          {loading ? <p className="text-slate-400">Loading…</p> : (
            <div className="space-y-2 max-h-80 overflow-y-auto">
              {history.length === 0 && <p className="text-slate-400 text-sm">No records yet</p>}
              {history.map(r => (
                <div key={r.id} className="flex flex-wrap items-center justify-between gap-2 text-sm border-b pb-2">
                  <span className="text-slate-600">{r.date}</span>
                  <span className="flex items-center gap-1.5">
                    {statusBadge(r.status)}
                    {r.late && <span className="badge-rejected">Late</span>}
                  </span>
                  <span className="text-xs text-slate-400">
                    {r.checkIn || '—'} → {r.checkOut || '—'}
                    {r.workingHours != null && <span className="ml-1 font-medium text-slate-600">({r.workingHours} h)</span>}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
