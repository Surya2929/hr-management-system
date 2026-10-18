import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import axiosInstance from '../api/axiosInstance';
import ForgotPasswordModal from '../components/ForgotPasswordModal';
import ResetPasswordModal from '../components/ResetPasswordModal';
import { FiAlertCircle, FiCheck, FiMail, FiLock, FiEye, FiEyeOff, FiUser, FiShield, FiLoader } from 'react-icons/fi';

export default function Login() {
  const [isRegister, setIsRegister] = useState(false);
  const [accountType, setAccountType] = useState('EMPLOYEE'); // 'EMPLOYEE' | 'HR'
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [showLoginPassword, setShowLoginPassword] = useState(false);
  const [showRegisterPassword, setShowRegisterPassword] = useState(false);
  const [departments, setDepartments] = useState([]);
  
  // Login fields
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  
  // Register fields (Employee only)
  const [regForm, setRegForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phone: '',
    designation: '',
    departmentId: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const { login } = useAuth();
  const navigate = useNavigate();

  // Reset link from "Forgot password" looks like /login?resetToken=...
  const [searchParams, setSearchParams] = useSearchParams();
  const resetToken = searchParams.get('resetToken');

  const handleResetDone = (message) => {
    setSearchParams({}); // remove the token from the URL
    if (message) {
      setIsRegister(false);
      setError('');
      setSuccess(message);
    }
  };

  // Load departments for registration dropdown
  useEffect(() => {
    axiosInstance.get('/departments')
      .then(res => setDepartments(res.data))
      .catch(() => {});
  }, []);

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
      const { token, role, userId, email } = res.data;
      login({ userId, role, email }, token);
      
      // Automatic role-based redirect
      if (role === 'HR') {
        navigate('/hr/dashboard', { replace: true });
      } else {
        navigate('/emp/dashboard', { replace: true });
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const res = await axiosInstance.post('/auth/register', {
        firstName: regForm.firstName,
        lastName: regForm.lastName,
        email: regForm.email,
        password: regForm.password,
        phone: regForm.phone,
        designation: regForm.designation || 'Employee',
        departmentId: regForm.departmentId ? Number(regForm.departmentId) : null
      });

      const { token, role, userId, email } = res.data;
      login({ userId, role, email }, token);
      
      // Registered as employee -> direct to employee dashboard
      navigate('/emp/dashboard', { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please check your details.');
    } finally {
      setLoading(false);
    }
  };

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
            HR Admin access is restricted to authorized credentials.
          </p>
        </div>

        {/* Right Panel: Form (7 cols on large screens) */}
        <div className="lg:col-span-7 p-5 pt-8 sm:p-10 lg:p-12 flex flex-col lg:justify-center bg-white">
          <div className="max-w-md w-full mx-auto">
            
            {/* Tab Switcher */}
            <div className="grid grid-cols-2 bg-neutral-100 p-1.5 rounded-full mb-6 sm:mb-8 border border-neutral-200">
              <button
                type="button"
                onClick={() => { setIsRegister(false); setError(''); setSuccess(''); }}
                className={`flex-1 py-2.5 text-sm font-medium rounded-full transition-all cursor-pointer ${
                  !isRegister
                    ? 'bg-neutral-900 text-white'
                    : 'text-neutral-500 hover:text-neutral-800'
                }`}
              >
                Sign In
              </button>
              <button
                type="button"
                onClick={() => { setIsRegister(true); setError(''); setSuccess(''); }}
                className={`flex-1 py-2.5 text-sm font-medium rounded-full transition-all cursor-pointer ${
                  isRegister
                    ? 'bg-neutral-900 text-white'
                    : 'text-neutral-500 hover:text-neutral-800'
                }`}
              >
                Employee Registration
              </button>
            </div>

            {/* Header Info */}
            <div className="mb-6">
              <h3 className="text-3xl sm:text-4xl font-light text-neutral-900">
                {!isRegister ? 'Welcome back' : 'Create Employee Account'}
              </h3>
              <p className="text-xs text-neutral-500 mt-1">
                {!isRegister
                  ? 'Sign in with your HR or Employee account credentials'
                  : 'Self-registration for new company employees only'}
              </p>
            </div>

            {/* Feedback Alerts */}
            {error && (
              <div className="mb-5 p-3.5 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-start gap-2">
                <FiAlertCircle className="w-4 h-4 mt-0.5 shrink-0" aria-hidden="true" />
                <span className="flex-1">{error}</span>
              </div>
            )}
            {success && (
              <div className="mb-5 p-3.5 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-xl flex items-start gap-2">
                <FiCheck className="w-4 h-4 mt-0.5 shrink-0" aria-hidden="true" />
                <span className="flex-1">{success}</span>
              </div>
            )}

            {/* LOGIN FORM */}
            {!isRegister ? (
              <form onSubmit={handleLogin} className="space-y-4">
                <div>
                  <label className="label" htmlFor="loginEmail">Email Address</label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-neutral-400 text-sm">
                      <FiMail className="w-4 h-4" aria-hidden="true" />
                    </span>
                    <input
                      id="loginEmail"
                      type="email"
                      className="input pl-11"
                      placeholder="hr@company.com or employee email"
                      value={loginEmail}
                      onChange={(e) => setLoginEmail(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="label" htmlFor="loginPassword">Password</label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-neutral-400 text-sm">
                      <FiLock className="w-4 h-4" aria-hidden="true" />
                    </span>
                    <input
                      id="loginPassword"
                      type={showLoginPassword ? 'text' : 'password'}
                      className="input pl-11 pr-12"
                      placeholder="••••••••"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      required
                    />
                    <button
                      type="button"
                      onClick={() => setShowLoginPassword(v => !v)}
                      className="absolute inset-y-0 right-0 w-11 flex items-center justify-center text-neutral-500 hover:text-brand-600"
                      aria-label={showLoginPassword ? 'Hide password' : 'Show password'}
                      title={showLoginPassword ? 'Hide password' : 'Show password'}
                    >
                      {showLoginPassword ? <FiEyeOff className="w-4 h-4" /> : <FiEye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="btn-primary w-full py-3 mt-2 flex items-center justify-center gap-2 text-sm"
                >
                  {loading ? (
                    <>
                      <FiLoader className="w-4 h-4 animate-spin" aria-hidden="true" />
                      Signing in...
                    </>
                  ) : (
                    'Sign In to Dashboard →'
                  )}
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

                <div className="mt-4 p-3 bg-neutral-50 border border-neutral-200/80 rounded-xl text-center">
                  <p className="text-xs text-neutral-500">
                    New Employee?{' '}
                    <button
                      type="button"
                      onClick={() => setIsRegister(true)}
                      className="text-brand-600 font-semibold hover:underline cursor-pointer"
                    >
                      Register here
                    </button>
                  </p>
                </div>
              </form>
            ) : (
              /* REGISTRATION: role toggle + conditional form */
              <div className="space-y-4">
                <div>
                  <label className="label">I am registering as</label>
                  <div className="flex bg-neutral-100 p-1 rounded-full border border-neutral-200">
                    <button
                      type="button"
                      onClick={() => setAccountType('EMPLOYEE')}
                      className={`flex-1 py-2 text-sm font-medium rounded-full transition-all cursor-pointer ${
                        accountType === 'EMPLOYEE'
                          ? 'bg-neutral-900 text-white'
                          : 'text-neutral-500 hover:text-neutral-800'
                      }`}
                    >
                      <FiUser className="inline w-4 h-4 mr-1 -mt-0.5" aria-hidden="true" />Employee
                    </button>
                    <button
                      type="button"
                      onClick={() => setAccountType('HR')}
                      className={`flex-1 py-2 text-sm font-medium rounded-full transition-all cursor-pointer ${
                        accountType === 'HR'
                          ? 'bg-neutral-900 text-white'
                          : 'text-neutral-500 hover:text-neutral-800'
                      }`}
                    >
                      <FiShield className="inline w-4 h-4 mr-1 -mt-0.5" aria-hidden="true" />HR / Admin
                    </button>
                  </div>
                </div>

                {accountType === 'HR' ? (
                  <div className="p-4 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-800 space-y-2">
                    <p className="font-semibold flex items-center gap-1.5">
                      <FiLock className="w-4 h-4" aria-hidden="true" /> HR accounts are not self-registrable
                    </p>
                    <p>
                      For security, HR/Admin access is set up only by the system
                      administrator. If you are HR staff, please sign in with the
                      credentials you were given, or contact your administrator.
                    </p>
                    <button
                      type="button"
                      onClick={() => { setIsRegister(false); }}
                      className="text-brand-600 font-semibold hover:underline cursor-pointer mt-1"
                    >
                      Go to Sign In →
                    </button>
                  </div>
                ) : (
              /* EMPLOYEE REGISTRATION FORM */
              <form onSubmit={handleRegister} className="space-y-3.5">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="label" htmlFor="regFirstName">First Name *</label>
                    <input
                      id="regFirstName"
                      type="text"
                      className="input"
                      placeholder="John"
                      value={regForm.firstName}
                      onChange={(e) => setRegForm({ ...regForm, firstName: e.target.value })}
                      required
                    />
                  </div>
                  <div>
                    <label className="label" htmlFor="regLastName">Last Name *</label>
                    <input
                      id="regLastName"
                      type="text"
                      className="input"
                      placeholder="Doe"
                      value={regForm.lastName}
                      onChange={(e) => setRegForm({ ...regForm, lastName: e.target.value })}
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="label" htmlFor="regEmail">Company Email *</label>
                  <input
                    id="regEmail"
                    type="email"
                    className="input"
                    placeholder="john.doe@company.com"
                    value={regForm.email}
                    onChange={(e) => setRegForm({ ...regForm, email: e.target.value })}
                    required
                  />
                </div>

                <div>
                  <label className="label" htmlFor="regPassword">Password *</label>
                  <div className="relative">
                    <input
                      id="regPassword"
                      type={showRegisterPassword ? 'text' : 'password'}
                      className="input pr-12"
                      placeholder="Create a strong password"
                      value={regForm.password}
                      onChange={(e) => setRegForm({ ...regForm, password: e.target.value })}
                      required
                    />
                    <button
                      type="button"
                      onClick={() => setShowRegisterPassword(v => !v)}
                      className="absolute inset-y-0 right-0 w-11 flex items-center justify-center text-neutral-500 hover:text-brand-600"
                      aria-label={showRegisterPassword ? 'Hide password' : 'Show password'}
                      title={showRegisterPassword ? 'Hide password' : 'Show password'}
                    >
                      {showRegisterPassword ? <FiEyeOff className="w-4 h-4" /> : <FiEye className="w-4 h-4" />}
                    </button>
                  </div>
                  <p className="text-xs text-neutral-400 mt-1">At least 8 characters, including a number.</p>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="label" htmlFor="regPhone">Phone</label>
                    <input
                      id="regPhone"
                      type="tel"
                      className="input"
                      placeholder="+1 555-0123"
                      value={regForm.phone}
                      onChange={(e) => setRegForm({ ...regForm, phone: e.target.value })}
                    />
                  </div>
                  <div>
                    <label className="label" htmlFor="regDesignation">Designation</label>
                    <input
                      id="regDesignation"
                      type="text"
                      className="input"
                      placeholder="Software Engineer"
                      value={regForm.designation}
                      onChange={(e) => setRegForm({ ...regForm, designation: e.target.value })}
                    />
                  </div>
                </div>

                <div>
                  <label className="label" htmlFor="regDept">Department</label>
                  <select
                    id="regDept"
                    className="input"
                    value={regForm.departmentId}
                    onChange={(e) => setRegForm({ ...regForm, departmentId: e.target.value })}
                  >
                    <option value="">— Select Department —</option>
                    {departments.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.name}
                      </option>
                    ))}
                  </select>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="btn-primary w-full py-3 mt-3 flex items-center justify-center gap-2 text-sm"
                >
                  {loading ? (
                    <>
                      <FiLoader className="w-4 h-4 animate-spin" aria-hidden="true" />
                      Creating Account...
                    </>
                  ) : (
                    'Complete Employee Registration'
                  )}
                </button>

                <div className="text-center pt-2">
                  <p className="text-xs text-neutral-500">
                    Already registered?{' '}
                    <button
                      type="button"
                      onClick={() => setIsRegister(false)}
                      className="text-brand-600 font-semibold hover:underline cursor-pointer"
                    >
                      Sign In instead
                    </button>
                  </p>
                </div>
              </form>
                )}
              </div>
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