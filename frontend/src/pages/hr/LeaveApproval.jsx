import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';

export default function LeaveApproval() {
  const [leaves, setLeaves]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter]   = useState('PENDING');

  const fetch = () => {
    setLoading(true);
    const url = filter === 'ALL' ? '/leave/all' : '/leave/pending';
    axiosInstance.get(url).then(r => setLeaves(r.data)).finally(() => setLoading(false));
  };
  useEffect(fetch, [filter]);

  const handleAction = async (id, action, remarks = '') => {
    try {
      await axiosInstance.put(`/leave/${id}/${action}`, { hrRemarks: remarks });
      fetch();
    } catch (err) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  const approve = (id) => {
    const remarks = prompt('Approval remarks (optional):') || '';
    handleAction(id, 'approve', remarks);
  };
  const reject = (id) => {
    const remarks = prompt('Rejection reason:') || '';
    handleAction(id, 'reject', remarks);
  };

  const badge = (s) => {
    if (s === 'APPROVED') return <span className="badge-approved">APPROVED</span>;
    if (s === 'REJECTED') return <span className="badge-rejected">REJECTED</span>;
    return <span className="badge-pending">PENDING</span>;
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-800 mb-6">Leave Requests</h2>

      <div className="card">
        <div className="flex gap-3 mb-5">
          {['PENDING', 'ALL'].map(f => (
            <button key={f} onClick={() => setFilter(f)}
              className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors
                ${filter === f ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}`}>
              {f === 'PENDING' ? '⏳ Pending' : '📋 All'}
            </button>
          ))}
        </div>

        {loading ? <p className="text-slate-400">Loading…</p> : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-slate-500 border-b">
                  <th className="pb-2 pr-4">Employee</th>
                  <th className="pb-2 pr-4">Type</th>
                  <th className="pb-2 pr-4">From</th>
                  <th className="pb-2 pr-4">To</th>
                  <th className="pb-2 pr-4">Reason</th>
                  <th className="pb-2 pr-4">Status</th>
                  <th className="pb-2">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {leaves.length === 0 ? (
                  <tr><td colSpan={7} className="py-8 text-center text-slate-400">No leave requests</td></tr>
                ) : leaves.map(l => (
                  <tr key={l.id} className="hover:bg-slate-50">
                    <td className="py-3 pr-4 font-medium">
                      {l.employee?.firstName} {l.employee?.lastName}
                    </td>
                    <td className="py-3 pr-4">{l.leaveType}</td>
                    <td className="py-3 pr-4">{l.fromDate}</td>
                    <td className="py-3 pr-4">{l.toDate}</td>
                    <td className="py-3 pr-4 text-slate-500 max-w-xs truncate">{l.reason || '—'}</td>
                    <td className="py-3 pr-4">{badge(l.status)}</td>
                    <td className="py-3">
                      {l.status === 'PENDING' && (
                        <div className="flex gap-2">
                          <button onClick={() => approve(l.id)}
                            className="text-xs btn-success py-1 px-2">✓ Approve</button>
                          <button onClick={() => reject(l.id)}
                            className="text-xs btn-danger py-1 px-2">✗ Reject</button>
                        </div>
                      )}
                      {l.status !== 'PENDING' && (
                        <span className="text-xs text-slate-400">{l.hrRemarks || '—'}</span>
                      )}
                    </td>
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
