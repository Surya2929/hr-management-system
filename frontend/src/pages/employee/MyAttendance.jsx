import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';

export default function MyAttendance() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [marking, setMarking] = useState(false);
  const [remarks, setRemarks] = useState(''); // optional note; time and status are set by the server
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

  // Check-in is allowed once a day, and not on an approved leave day
  const onLeaveToday = today?.status === 'ON_LEAVE';
  const canCheckIn = !today?.checkIn && !onLeaveToday;

  const handleMark = async (e) => {
    e.preventDefault();
    setMarking(true);
    try {
      // Only the note is sent - the server records the current time and status
      await axiosInstance.post('/attendance/mark', remarks ? { remarks } : {});
      fetch();
      setRemarks('');
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
      <h2 className="text-3xl sm:text-4xl font-light text-neutral-900 mb-6">My Attendance</h2>

      {/* Today: check-in / check-out status */}
      <div className="card mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="text-sm space-y-1">
          <p className="font-semibold text-neutral-700">
            Today{' '}
            {today?.late && <span className="badge-rejected ml-1">Late</span>}
          </p>
          <p className="text-neutral-500">
            Check-in: <span className="font-medium text-neutral-700">{today?.checkIn || '—'}</span>
            {' · '}
            Check-out: <span className="font-medium text-neutral-700">{today?.checkOut || '—'}</span>
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
          {checkingOut ? 'Checking out…' : 'Check Out'}
        </button>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <div className="card">
          <h3 className="font-semibold text-neutral-700 mb-1">Check In</h3>
          <p className="text-xs text-neutral-500 mb-4">
            The current time is recorded automatically. Checking in after the office start time is marked Late.
          </p>
          {onLeaveToday ? (
            <p className="text-sm text-neutral-600 bg-neutral-50 border border-neutral-200 rounded-2xl p-3">
              You are on approved leave today.
            </p>
          ) : today?.checkIn ? (
            <p className="text-sm text-neutral-600 bg-neutral-50 border border-neutral-200 rounded-2xl p-3">
              You checked in today at <b>{today.checkIn}</b>.
            </p>
          ) : (
            <form onSubmit={handleMark} className="space-y-3">
              <div>
                <label className="label" htmlFor="attRemarks">Remarks (optional)</label>
                <input id="attRemarks" className="input" placeholder="e.g. Working from home" maxLength={255}
                  value={remarks} onChange={e => setRemarks(e.target.value)} />
              </div>
              <button type="submit" disabled={marking || !canCheckIn} className="btn-primary">
                {marking ? 'Saving…' : 'Check In Now'}
              </button>
            </form>
          )}
        </div>

        <div className="card">
          <h3 className="font-semibold text-neutral-700 mb-4">Attendance History</h3>
          {loading ? <p className="text-neutral-400">Loading…</p> : (
            <div className="space-y-2 max-h-80 overflow-y-auto">
              {history.length === 0 && <p className="text-neutral-400 text-sm">No records yet</p>}
              {history.map(r => (
                <div key={r.id} className="flex flex-wrap items-center justify-between gap-2 text-sm border-b pb-2">
                  <span className="text-neutral-600">{r.date}</span>
                  <span className="flex items-center gap-1.5">
                    {statusBadge(r.status)}
                    {r.late && <span className="badge-rejected">Late</span>}
                  </span>
                  <span className="text-xs text-neutral-400">
                    {r.checkIn || '—'} → {r.checkOut || '—'}
                    {r.workingHours != null && <span className="ml-1 font-medium text-neutral-600">({r.workingHours} h)</span>}
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
