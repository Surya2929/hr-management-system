import { useState } from 'react';
import axiosInstance from '../api/axiosInstance';

// Step 2 of "Forgot password": opened from the link /login?resetToken=...
export default function ResetPasswordModal({ token, onDone }) {
  const [password, setPassword] = useState('');
  const [confirm, setConfirm]   = useState('');
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (password.length < 8 || !/\d/.test(password)) {
      setError('Password must be at least 8 characters and contain a number.');
      return;
    }
    if (password !== confirm) {
      setError('Passwords do not match.');
      return;
    }
    setLoading(true);
    try {
      const res = await axiosInstance.post('/auth/reset-password', { token, newPassword: password });
      onDone(res.data.message); // close and show the success message on the login page
    } catch (err) {
      setError(err.response?.data?.message || 'Could not reset password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-neutral-900/50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-3xl shadow-2xl max-w-sm w-full max-h-full overflow-y-auto p-6">
        <h3 className="text-2xl font-light text-neutral-900 mb-3">Reset Password</h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label" htmlFor="resetPassword">New Password</label>
            <input id="resetPassword" type="password" className="input" autoComplete="new-password" required
              value={password} onChange={(e) => setPassword(e.target.value)} />
            <p className="text-xs text-neutral-400 mt-1">At least 8 characters, including a number.</p>
          </div>
          <div>
            <label className="label" htmlFor="resetConfirm">Confirm New Password</label>
            <input id="resetConfirm" type="password" className="input" autoComplete="new-password" required
              value={confirm} onChange={(e) => setConfirm(e.target.value)} />
          </div>
          {error && <p className="text-xs text-red-600 bg-red-50 px-3 py-2 rounded">{error}</p>}
          <div className="flex gap-3">
            <button type="submit" disabled={loading} className="btn-primary flex-1 text-sm">
              {loading ? 'Saving…' : 'Set New Password'}
            </button>
            <button type="button" onClick={() => onDone('')} className="btn-secondary text-sm">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
}
