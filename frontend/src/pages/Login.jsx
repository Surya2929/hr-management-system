import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import axiosInstance from '../api/axiosInstance';
import ForgotPasswordModal from '../components/ForgotPasswordModal';
import ResetPasswordModal from '../components/ResetPasswordModal';
import ActivateAccountForm from '../components/ActivateAccountForm';
import { FiAlertCircle, FiCheck, FiMail, FiLock, FiEye, FiEyeOff, FiLoader } from 'react-icons/fi';

export default function Login() {
  // 'signin' or 'activate' (new employees activate the account HR created for them)
  const [mode, setMode] = useState('signin');
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [showLoginPassword, setShowLoginPassword] = useState(false);

  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Pre-filled when someone tries to sign in with a temporary password
  const [activation, setActivation] = useState({ email: '', tempPassword: '', notice: '', key: 0 });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const { login } = useAuth();
  const navigate = useNavigate();

  // Reset link from "Forgot password" looks like /login?resetToken=...
  const [searchParams, setSearchParams] = useSearchParams();
  const resetToken = searchParams.get('resetToken');

  const switchMode = (next) => {
    setMode(next);
    setError('');
    setSuccess('');
  };

  const handleResetDone = (message) => {
    setSearchParams({}); // remove the token from the URL
    if (message) {
      switchMode('signin');
      setSuccess(message);
    }
  };

  // Save the session and go to the right dashboard
  const finishLogin = ({ token, role, userId, email }) => {
    login({ userId, role, email }, token);
    navigate(role === 'HR' ? '/hr/dashboard' : '/emp/dashboard', { replace: true });
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const res = await axiosInstance.post('/auth/login', {
        email: loginEmail,
        password: loginPassword,
      });
      finishLogin(res.data);
    } catch (err) {
      const data = err.response?.data;
      if (data?.error === 'ACTIVATION_REQUIRED') {
        // They typed the temporary password from HR -> send them to activation with it filled in
        setActivation(a => ({
          email: loginEmail,
          tempPassword: loginPassword,
          notice: 'This is a temporary password. Activate your account by setting your own password.',
          key: a.key + 1, // remount the form with the new values
        }));
        switchMode('activate');
      } else {
        setError(data?.message || 'Invalid email or password. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const tabClass = (active) =>
    `flex-1 py-2.5 text-sm font-medium rounded-full transition-all cursor-pointer ${
      active ? 'bg-neutral-900 text-white' : 'text-neutral-500 hover:text-neutral-800'
    }`;

  return (
    <div className="min-h-screen bg-neutral-200 flex items-center justify-center p-0 sm:p-6 lg:p-8">
      <div className="w-full max-w-5xl bg-white sm:rounded-4xl overflow-hidden grid grid-cols-1 grid-rows-[auto_1fr] lg:grid-rows-1 lg:grid-cols-12 min-h-screen sm:min-h-[640px] border-0 sm:border border-white">

        {/* Left Panel: orange-to-white brand card (like the design's colour/typography card) */}
        <div className="lg:col-span-5 relative overflow-hidden bg-brand-500 text-white p-6 sm:p-8 lg:p-12 flex flex-col justify-between lg:min-h-[320px]">
          {/* White glow fading in from the bottom-right */}
          <div className="absolute -bottom-32 -right-24 w-[26rem] h-[26rem] rounded-full bg-white/70 blur-3xl pointer-events-none" aria-hidden="true" />
          <div className="absolute inset-0 dot-grid opacity-25 pointer-events-none" aria-hidden="true" />

          <div className="relative z-10 flex items-center gap-3">
            <span className="w-10 h-10 rounded-xl bg-white text-brand-600 grid place-items-center font-semibold">S</span>
            <h1 className="text-2xl font-semibold tracking-tight">SmartHR</h1>
          </div>

          <div className="relative z-10 mt-6 lg:my-8 space-y-6">
            <div>
              <p className="text-sm text-neutral-950 mb-2">AI-Powered Workforce Platform</p>
              <h2 className="text-3xl sm:text-4xl lg:text-5xl font-light leading-[1.05] tracking-tight">
                Modern HR &amp; Talent Management
              </h2>
            </div>
            <ul className="hidden sm:flex flex-wrap gap-2">
              {['AI resume screening', 'Interview questions', 'Leave & attendance'].map(f => (
                <li key={f} className="flex items-center gap-2 text-sm text-neutral-900 bg-white/90 rounded-full px-3.5 py-1.5">
                  <span className="w-1.5 h-1.5 rounded-full bg-brand-500" aria-hidden="true" />
                  {f}
                </li>
              ))}
            </ul>
          </div>

          <p className="relative z-10 text-xs text-neutral-700 hidden lg:block">
            Accounts are created by HR. New employees: use the temporary password HR gave you.
          </p>
        </div>

        {/* Right Panel: Form (7 cols on large screens) */}
        <div className="lg:col-span-7 p-5 pt-8 sm:p-10 lg:p-12 flex flex-col lg:justify-center bg-white">
          <div className="max-w-md w-full mx-auto">

            {/* Tab Switcher */}
            <div className="grid grid-cols-2 bg-neutral-100 p-1.5 rounded-full mb-6 sm:mb-8 border border-neutral-200" role="tablist">
              <button type="button" role="tab" aria-selected={mode === 'signin'}
                onClick={() => switchMode('signin')} className={tabClass(mode === 'signin')}>
                Sign In
              </button>
              <button type="button" role="tab" aria-selected={mode === 'activate'}
                onClick={() => switchMode('activate')} className={tabClass(mode === 'activate')}>
                Activate Account
              </button>
            </div>

            {/* Header Info */}
            <div className="mb-6">
              <h3 className="text-3xl sm:text-4xl font-light text-neutral-900">
                {mode === 'signin' ? 'Welcome back' : 'Activate your account'}
              </h3>
              <p className="text-xs text-neutral-500 mt-1">
                {mode === 'signin'
                  ? 'Sign in with your HR or Employee account credentials'
                  : 'For new employees whose account was created by HR'}
              </p>
            </div>

            {mode === 'signin' ? (
              <>
                {/* Feedback Alerts */}
                {error && (
                  <div className="mb-5 p-3.5 bg-red-50 border border-red-200 text-red-700 text-xs rounded-2xl flex items-start gap-2" role="alert">
                    <FiAlertCircle className="w-4 h-4 mt-0.5 shrink-0" aria-hidden="true" />
                    <span className="flex-1">{error}</span>
                  </div>
                )}
                {success && (
                  <div className="mb-5 p-3.5 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-2xl flex items-start gap-2">
                    <FiCheck className="w-4 h-4 mt-0.5 shrink-0" aria-hidden="true" />
                    <span className="flex-1">{success}</span>
                  </div>
                )}

                <form onSubmit={handleLogin} className="space-y-4">
                  <div>
                    <label className="label" htmlFor="loginEmail">Email Address</label>
                    <div className="relative">
                      <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-neutral-400">
                        <FiMail className="w-4 h-4" aria-hidden="true" />
                      </span>
                      <input
                        id="loginEmail"
                        type="email"
                        className="input pl-11"
                        placeholder="you@company.com"
                        autoComplete="username"
                        value={loginEmail}
                        onChange={(e) => setLoginEmail(e.target.value)}
                        required
                      />
                    </div>
                  </div>

                  <div>
                    <label className="label" htmlFor="loginPassword">Password</label>
                    <div className="relative">
                      <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-neutral-400">
                        <FiLock className="w-4 h-4" aria-hidden="true" />
                      </span>
                      <input
                        id="loginPassword"
                        type={showLoginPassword ? 'text' : 'password'}
                        className="input pl-11 pr-12"
                        placeholder="••••••••"
                        autoComplete="current-password"
                        value={loginPassword}
                        onChange={(e) => setLoginPassword(e.target.value)}
                        required
                      />
                      <button
                        type="button"
                        onClick={() => setShowLoginPassword(v => !v)}
                        className="absolute inset-y-0 right-0 w-11 flex items-center justify-center text-neutral-500 hover:text-neutral-900"
                        aria-label={showLoginPassword ? 'Hide password' : 'Show password'}
                        title={showLoginPassword ? 'Hide password' : 'Show password'}
                      >
                        {showLoginPassword ? <FiEyeOff className="w-4 h-4" /> : <FiEye className="w-4 h-4" />}
                      </button>
                    </div>
                  </div>

                  <button type="submit" disabled={loading} className="btn-primary w-full py-3 mt-2 text-sm">
                    {loading
                      ? <><FiLoader className="w-4 h-4 animate-spin" aria-hidden="true" /> Signing in...</>
                      : 'Sign In to Dashboard →'}
                  </button>

                  <div className="text-center mt-3">
                    <button
                      type="button"
                      onClick={() => setShowForgotPassword(true)}
                      className="text-xs text-neutral-500 hover:text-brand-600 hover:underline cursor-pointer"
                    >
                      Forgot password?
                    </button>
                  </div>

                  <div className="mt-4 p-3 bg-neutral-50 border border-neutral-200/80 rounded-2xl text-center">
                    <p className="text-xs text-neutral-500">
                      New employee?{' '}
                      <button
                        type="button"
                        onClick={() => switchMode('activate')}
                        className="text-brand-600 font-semibold hover:underline cursor-pointer"
                      >
                        Activate your account
                      </button>
                    </p>
                  </div>
                </form>
              </>
            ) : (
              <ActivateAccountForm
                key={activation.key}
                initialEmail={activation.email}
                initialTempPassword={activation.tempPassword}
                notice={activation.notice}
                onActivated={finishLogin}
              />
            )}
          </div>
        </div>
      </div>

      {/* Forgot Password: enter email */}
      {showForgotPassword && <ForgotPasswordModal onClose={() => setShowForgotPassword(false)} />}

      {/* Reset Password: opened from the emailed/logged link */}
      {resetToken && <ResetPasswordModal token={resetToken} onDone={handleResetDone} />}
    </div>
  );
}
