import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';

export default function LeaveRequest() {
  const [leaves, setLeaves] = useState([]);
  const [balance, setBalance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [form, setForm] = useState({
    leaveType: 'CASUAL',
    fromDate: '',
    toDate: '',
    reason: '',
  });

  const fetchData = async () => {
    setLoading(true);
    try {
      const [leavesRes, balanceRes] = await Promise.all([
        axiosInstance.get('/leave/my'),
        axiosInstance.get('/leave/balance'),
      ]);
      setLeaves(leavesRes.data);
      setBalance(balanceRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleChange = (field) => (e) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSubmitting(true);

    try {
      await axiosInstance.post('/leave/apply', form);
      setSuccess('Leave request submitted successfully!');
      setForm({ leaveType: 'CASUAL', fromDate: '', toDate: '', reason: '' });
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit leave request');
    } finally {
      setSubmitting(false);
    }
  };

  const statusBadge = (s) => {
    if (s === 'APPROVED') return <span className="badge-approved">APPROVED</span>;
    if (s === 'REJECTED') return <span className="badge-rejected">REJECTED</span>;
    return <span className="badge-pending">PENDING</span>;
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-800 mb-6">Leave Management</h2>

      {balance && (
        <div className="card mb-6 bg-gradient-to-r from-indigo-50 to-purple-50 border-indigo-100">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <h3 className="font-bold text-slate-800">Annual Leave Quota ({balance.year})</h3>
              <p className="text-xs text-slate-500 mt-0.5">Track your available paid and casual leaves</p>
            </div>
            <div className="flex items-center gap-6">
              <div className="text-center">
                <p className="text-xs text-slate-400">Total Allowed</p>
                <p className="text-xl font-bold text-slate-700">{balance.totalAllowed} days</p>
              </div>
              <div className="text-center">
                <p className="text-xs text-slate-400">Used</p>
                <p className="text-xl font-bold text-yellow-600">{balance.daysUsed} days</p>
              </div>
              <div className="text-center">
                <p className="text-xs text-slate-400">Remaining</p>
                <p className="text-xl font-bold text-emerald-600">{balance.daysRemaining} days</p>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="grid lg:grid-cols-2 gap-6">
        <div className="card">
          <h3 className="font-semibold text-slate-700 mb-4">Apply for Leave</h3>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-lg text-xs mb-4">
              {error}
            </div>
          )}
          {success && (
            <div className="bg-emerald-50 border border-emerald-200 text-emerald-700 p-3 rounded-lg text-xs mb-4">
              {success}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label" htmlFor="leaveType">
                Leave Type *
              </label>
              <select
                id="leaveType"
                className="input"
                value={form.leaveType}
                onChange={handleChange('leaveType')}
                required
              >
                <option value="CASUAL">Casual Leave</option>
                <option value="SICK">Sick Leave</option>
                <option value="EARNED">Earned Leave</option>
                <option value="UNPAID">Unpaid Leave</option>
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="label" htmlFor="fromDate">
                  From Date *
                </label>
                <input
                  id="fromDate"
                  type="date"
                  className="input"
                  value={form.fromDate}
                  onChange={handleChange('fromDate')}
                  required
                />
              </div>
              <div>
                <label className="label" htmlFor="toDate">
                  To Date *
                </label>
                <input
                  id="toDate"
                  type="date"
                  className="input"
                  value={form.toDate}
                  onChange={handleChange('toDate')}
                  required
                />
              </div>
            </div>

            <div>
              <label className="label" htmlFor="reason">
                Reason *
              </label>
              <textarea
                id="reason"
                rows={3}
                className="input resize-none"
                placeholder="State the reason for leave..."
                value={form.reason}
                onChange={handleChange('reason')}
                required
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="btn-primary w-full text-sm py-2.5"
            >
              {submitting ? 'Submitting...' : 'Submit Leave Request'}
            </button>
          </form>
        </div>

        <div className="card">
          <h3 className="font-semibold text-slate-700 mb-4">My Leave Requests</h3>

          {loading ? (
            <p className="text-slate-400 text-sm">Loading history...</p>
          ) : (
            <div className="space-y-3 max-h-96 overflow-y-auto">
              {leaves.length === 0 ? (
                <p className="text-slate-400 text-sm py-4 text-center">No leave requests found.</p>
              ) : (
                leaves.map((item) => (
                  <div
                    key={item.id}
                    className="p-3.5 bg-slate-50 border border-slate-200/80 rounded-xl space-y-1.5"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-sm text-slate-800">
                        {item.leaveType}
                      </span>
                      {statusBadge(item.status)}
                    </div>
                    <p className="text-xs text-slate-500">
                      📅 {item.fromDate} → {item.toDate}
                    </p>
                    {item.reason && (
                      <p className="text-xs text-slate-600 italic">"{item.reason}"</p>
                    )}
                    {item.hrRemarks && (
                      <p className="text-xs text-indigo-600 bg-indigo-50/70 p-1.5 rounded mt-1">
                        <span className="font-medium">HR Remarks:</span> {item.hrRemarks}
                      </p>
                    )}
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
