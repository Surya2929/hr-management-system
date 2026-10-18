import { useState } from 'react';
import axiosInstance from '../api/axiosInstance';

// Step 1 of "Forgot password": ask for the email. The server always gives the same answer.
export default function ForgotPasswordModal({ onClose }) {
  const [email, setEmail]     = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError]     = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const res = await axiosInstance.post('/auth/forgot-password', { email });
      setMessage(res.data.message);
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center p-4 z-50" onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full max-h-full overflow-y-auto p-6"
        onClick={(e) => e.stopPropagation()}>
        <h3 className="text-lg font-bold text-slate-800 mb-3">🔑 Forgot Password</h3>

        {message ? (
          <>
            <p className="text-sm text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-xl p-3">{message}</p>
            <button type="button" onClick={onClose} className="btn-primary w-full py-2.5 mt-5 text-sm">Got it</button>
          </>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <p className="text-sm text-slate-600">Enter your account email and we'll send you a reset link.</p>
            <div>
              <label className="label" htmlFor="forgotEmail">Email Address</label>
              <input id="forgotEmail" type="email" className="input" required
                value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            {error && <p className="text-xs text-red-600 bg-red-50 px-3 py-2 rounded">{error}</p>}
            <div className="flex gap-3">
              <button type="submit" disabled={loading} className="btn-primary flex-1 text-sm">
                {loading ? 'Sending…' : 'Send Reset Link'}
              </button>
              <button type="button" onClick={onClose} className="btn-secondary text-sm">Cancel</button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
