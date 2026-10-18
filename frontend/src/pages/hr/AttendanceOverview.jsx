import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';

export default function AttendanceOverview() {
  const [date, setDate]           = useState(new Date().toLocaleDateString('en-CA')); // local YYYY-MM-DD
  const [records, setRecords]     = useState([]);
  const [loading, setLoading]     = useState(false);

  const fetchAttendance = () => {
    setLoading(true);
    axiosInstance.get(`/attendance?date=${date}`)
      .then(r => setRecords(r.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchAttendance(); }, [date]);

  const statusBadge = (s) => {
    const map = { PRESENT:'badge-approved', ABSENT:'badge-rejected', HALF_DAY:'badge-pending', ON_LEAVE:'badge-pending' };
    return <span className={map[s] || 'badge-pending'}>{s}</span>;
  };

  return (
    <div>
      <h2 className="text-3xl sm:text-4xl font-light text-neutral-900 mb-6">Attendance Overview</h2>

      <div className="card">
        <div className="flex flex-wrap items-center gap-4 mb-5">
          <div>
            <label className="label" htmlFor="attDate">Select Date</label>
            <input id="attDate" type="date" className="input w-48"
              value={date} onChange={e => setDate(e.target.value)} />
          </div>
          <div className="self-end">
            <span className="text-sm text-neutral-500">
              {records.length} record{records.length !== 1 ? 's' : ''} found
            </span>
          </div>
        </div>

        {loading ? <p className="text-neutral-400">Loading…</p> : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-neutral-500 border-b">
                  <th className="pb-2 pr-4">Employee</th>
                  <th className="pb-2 pr-4">Status</th>
                  <th className="pb-2 pr-4">Check In</th>
                  <th className="pb-2 pr-4">Check Out</th>
                  <th className="pb-2 pr-4">Hours</th>
                  <th className="pb-2">Remarks</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-100">
                {records.length === 0 ? (
                  <tr><td colSpan={6} className="py-8 text-center text-neutral-400">No attendance records for this date</td></tr>
                ) : records.map(r => (
                  <tr key={r.id} className="hover:bg-neutral-50">
                    <td className="py-3 pr-4 font-medium">
                      {r.employee?.firstName} {r.employee?.lastName}
                    </td>
                    <td className="py-3 pr-4">{statusBadge(r.status)}</td>
                    <td className="py-3 pr-4 whitespace-nowrap">
                      {r.checkIn || '—'}
                      {r.late && <span className="badge-rejected ml-2">Late</span>}
                    </td>
                    <td className="py-3 pr-4">{r.checkOut || '—'}</td>
                    <td className="py-3 pr-4">{r.workingHours != null ? `${r.workingHours} h` : '—'}</td>
                    <td className="py-3 text-neutral-500">{r.remarks || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
