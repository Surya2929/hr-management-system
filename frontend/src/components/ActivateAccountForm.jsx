import { useState } from 'react';
import { FiAlertCircle, FiArrowLeft, FiCheck, FiLoader, FiLock, FiMail } from 'react-icons/fi';
import axiosInstance from '../api/axiosInstance';

// Same rule as the backend: at least 8 characters and one number
const isStrong = (pw) => pw.length >= 8 && /\d/.test(pw);

/**
 * New employee activation (2 steps):
 * 1. Enter work email + temporary password from HR  -> POST /auth/activate/verify
 * 2. Set your own password                           -> POST /auth/activate/complete (logs you in)
 */
export default function ActivateAccountForm({ initialEmail = '', initialTempPassword = '', notice = '', onActivated }) {
  const [step, setStep]           = useState(1);
  const [email, setEmail]         = useState(initialEmail);
  const [tempPassword, setTempPassword] = useState(initialTempPassword);
  const [newPassword, setNewPassword]   = useState('');
  const [confirm, setConfirm]     = useState('');
  const [loading, setLoading]     = useState(false);
  const [error, setError]         = useState('');

  const handleVerify = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      await axiosInstance.post('/auth/activate/verify', { email, temporaryPassword: tempPassword });
      setStep(2); // verified -> show "set new password"
    } catch (err) {
      setError(err.response?.data?.message || 'Could not verify. Please check your details.');
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = async (e) => {
    e.preventDefault();
    setError('');
    if (!isStrong(newPassword)) {
      setError('Password must be at least 8 characters and contain a number.');
      return;
    }
    if (newPassword !== confirm) {
      setError('Passwords do not match.');
      return;
    }
    setLoading(true);
    try {
      const res = await axiosInstance.post('/auth/activate/complete', {
        email, temporaryPassword: tempPassword, newPassword,
      });
      onActivated(res.data); // { token, role, userId, email } -> parent logs the user in
    } catch (err) {
      setError(err.response?.data?.message || 'Could not activate your account.');
      setLoading(false);
    }
  };

  return (
    <div>
      {/* Step indicator */}
      <ol className="flex items-center gap-2 mb-6 text-sm" aria-label="Activation steps">
        {['Verify', 'Set password'].map((label, i) => {
          const n = i + 1;
          const done = step > n, current = step === n;
          return (
            <li key={label} className="flex items-center gap-2" aria-current={current ? 'step' : undefined}>
              <span className={`w-7 h-7 rounded-full grid place-items-center text-xs font-medium
                ${done ? 'bg-brand-500 text-white' : current ? 'bg-neutral-900 text-white' : 'bg-neutral-200 text-neutral-500'}`}>
                {done ? <FiCheck className="w-4 h-4" aria-hidden="true" /> : n}
              </span>
              <span className={current ? 'text-neutral-900' : 'text-neutral-500'}>{label}</span>
              {n === 1 && <span className="w-8 h-px bg-neutral-300 mx-1" aria-hidden="true" />}
            </li>
          );
        })}
      </ol>

      {notice && step === 1 && (
        <p className="mb-5 p-3.5 bg-brand-50 border border-brand-200 text-brand-800 text-xs rounded-2xl">{notice}</p>
      )}
      {error && (
        <div className="mb-5 p-3.5 bg-red-50 border border-red-200 text-red-700 text-xs rounded-2xl flex items-start gap-2" role="alert">
          <FiAlertCircle className="w-4 h-4 mt-0.5 shrink-0" aria-hidden="true" />
          <span className="flex-1">{error}</span>
        </div>
      )}

      {step === 1 ? (
        <form onSubmit={handleVerify} className="space-y-4">
          <p className="text-sm text-neutral-500">
            Use the work email and temporary password your HR team gave you.
          </p>
          <div>
            <label className="label" htmlFor="actEmail">Work Email</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-neutral-400">
                <FiMail className="w-4 h-4" aria-hidden="true" />
              </span>
              <input id="actEmail" type="email" className="input pl-11" required autoComplete="username"
                value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
          </div>
          <div>
            <label className="label" htmlFor="actTemp">Temporary Password</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-neutral-400">
                <FiLock className="w-4 h-4" aria-hidden="true" />
              </span>
              <input id="actTemp" type="text" className="input pl-11 font-mono tracking-wide" required
                autoComplete="one-time-code" spellCheck={false}
                value={tempPassword} onChange={(e) => setTempPassword(e.target.value.trim())} />
            </div>
          </div>
          <button type="submit" disabled={loading} className="btn-primary w-full py-3 text-sm">
            {loading ? <><FiLoader className="w-4 h-4 animate-spin" aria-hidden="true" /> Verifying…</> : 'Verify & Continue →'}
          </button>
        </form>
      ) : (
        <form onSubmit={handleComplete} className="space-y-4">
          <p className="text-sm text-neutral-500">
            Verified <span className="text-neutral-900 font-medium">{email}</span>. Now choose your own password.
          </p>
          <div>
            <label className="label" htmlFor="actNew">New Password</label>
            <input id="actNew" type="password" className="input" required autoComplete="new-password" autoFocus
              value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
            <p className="text-xs text-neutral-400 mt-1">At least 8 characters, including a number.</p>
          </div>
          <div>
            <label className="label" htmlFor="actConfirm">Confirm New Password</label>
            <input id="actConfirm" type="password" className="input" required autoComplete="new-password"
              value={confirm} onChange={(e) => setConfirm(e.target.value)} />
          </div>
          <button type="submit" disabled={loading} className="btn-primary w-full py-3 text-sm">
            {loading ? <><FiLoader className="w-4 h-4 animate-spin" aria-hidden="true" /> Activating…</> : 'Activate & Sign In'}
          </button>
          <button type="button" onClick={() => { setStep(1); setError(''); }}
            className="w-full text-sm text-neutral-500 hover:text-neutral-900 inline-flex items-center justify-center gap-1.5">
            <FiArrowLeft className="w-4 h-4" aria-hidden="true" /> Back
          </button>
        </form>
      )}
    </div>
  );
}
