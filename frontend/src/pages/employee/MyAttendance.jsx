import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';

export default function MyAttendance() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [marking, setMarking] = useState(false);
  const [form, setForm]       = useState({ status: 'PRESENT', checkIn: '', checkOut: '', remarks: '' });

  const fetch = () => {
    axiosInstance.get('/attendance/me')
      .then(r => setHistory(r.data))
      .finally(() => setLoading(false));
  };
  useEffect(fetch, []);

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
                <div key={r.id} className="flex items-center justify-between text-sm border-b pb-2">
                  <span className="text-slate-600">{r.date}</span>
                  {statusBadge(r.status)}
                  <span className="text-xs text-slate-400">{r.checkIn || '—'} → {r.checkOut || '—'}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
