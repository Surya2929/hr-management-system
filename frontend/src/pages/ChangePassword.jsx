import { useState } from 'react';
import axiosInstance from '../api/axiosInstance';

// Same rule as the backend: at least 8 characters and one number
const isStrong = (pw) => pw.length >= 8 && /\d/.test(pw);

export default function ChangePassword() {
  const [form, setForm]       = useState({ oldPassword: '', newPassword: '', confirm: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');
  const [success, setSuccess] = useState('');

  const set = (f) => (e) => setForm(prev => ({ ...prev, [f]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');

    if (!isStrong(form.newPassword)) {
      setError('New password must be at least 8 characters and contain a number.');
      return;
    }
    if (form.newPassword !== form.confirm) {
      setError('New password and confirmation do not match.');
      return;
    }

    setLoading(true);
    try {
      const res = await axiosInstance.post('/auth/change-password', {
        oldPassword: form.oldPassword,
        newPassword: form.newPassword,
      });
      setSuccess(res.data.message);
      setForm({ oldPassword: '', newPassword: '', confirm: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not change password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2 className="text-3xl sm:text-4xl font-light text-neutral-900 mb-6">Change Password</h2>

      <div className="card max-w-md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label" htmlFor="oldPassword">Current Password *</label>
            <input id="oldPassword" type="password" className="input" autoComplete="current-password"
              value={form.oldPassword} onChange={set('oldPassword')} required />
          </div>
          <div>
            <label className="label" htmlFor="newPassword">New Password *</label>
            <input id="newPassword" type="password" className="input" autoComplete="new-password"
              value={form.newPassword} onChange={set('newPassword')} required minLength={8} />
            <p className="text-xs text-neutral-400 mt-1">At least 8 characters, including a number.</p>
          </div>
          <div>
            <label className="label" htmlFor="confirmPassword">Confirm New Password *</label>
            <input id="confirmPassword" type="password" className="input" autoComplete="new-password"
              value={form.confirm} onChange={set('confirm')} required />
          </div>

          {error && <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded">{error}</p>}
          {success && <p className="text-sm text-emerald-700 bg-emerald-50 px-3 py-2 rounded">{success}</p>}

          <button type="submit" disabled={loading} className="btn-primary w-full sm:w-auto">
            {loading ? 'Saving…' : 'Update Password'}
          </button>
        </form>
      </div>
    </div>
  );
}
