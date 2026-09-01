import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import axiosInstance from '../api/axiosInstance';

export default function Login() {
  const [isRegister, setIsRegister] = useState(false);
  const [accountType, setAccountType] = useState('EMPLOYEE'); // 'EMPLOYEE' | 'HR'
  const [showForgotPassword, setShowForgotPassword] = useState(false);
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
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4 sm:p-6 lg:p-8">
      <div className="w-full max-w-5xl bg-white rounded-3xl shadow-2xl overflow-hidden grid grid-cols-1 lg:grid-cols-12 min-h-[640px] border border-slate-200/80">
        
        {/* Left Panel: Branding & Highlights (5 cols on large screens) */}
        <div className="lg:col-span-5 bg-gradient-to-br from-indigo-900 via-indigo-800 to-slate-950 p-8 lg:p-12 text-white flex flex-col justify-between relative overflow-hidden">
          {/* Subtle Background Circles */}
          <div className="absolute -top-24 -left-24 w-72 h-72 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none"></div>
          <div className="absolute -bottom-24 -right-24 w-72 h-72 bg-purple-500/20 rounded-full blur-3xl pointer-events-none"></div>

          {/* Top Logo */}
          <div className="relative z-10">
            <div className="flex items-center gap-3 mb-2">
              <div className="w-10 h-10 rounded-xl bg-indigo-500/30 border border-indigo-400/40 flex items-center justify-center text-xl shadow-inner">
                ⚡
              </div>
              <h1 className="text-2xl font-bold tracking-tight text-white">SmartHR</h1>
            </div>
            <p className="text-xs text-indigo-300 font-medium tracking-wide uppercase">
              AI-Powered Workforce Platform
            </p>
          </div>

          {/* Center Content */}
          <div className="relative z-10 my-8 space-y-6">
            <div>
              <h2 className="text-2xl lg:text-3xl font-extrabold leading-tight text-white mb-2">
                Modern HR & Talent Management
              </h2>
              <p className="text-sm text-indigo-200/90 leading-relaxed">
                Streamline employee operations, attendance tracking, leave requests, and AI-driven candidate screening.
              </p>
            </div>

            <div className="space-y-3 pt-2">
              <div className="flex items-center gap-3 text-xs text-indigo-100 bg-white/5 border border-white/10 rounded-xl p-3 backdrop-blur-sm">
                <span className="text-indigo-400 text-base">🤖</span>
                <span>AI Resume Screening & Skill Matching</span>
              </div>
              <div className="flex items-center gap-3 text-xs text-indigo-100 bg-white/5 border border-white/10 rounded-xl p-3 backdrop-blur-sm">
                <span className="text-indigo-400 text-base">❓</span>
                <span>Personalized Interview Question Generation</span>
              </div>
              <div className="flex items-center gap-3 text-xs text-indigo-100 bg-white/5 border border-white/10 rounded-xl p-3 backdrop-blur-sm">
                <span className="text-indigo-400 text-base">📅</span>
                <span>Automated Leave & Attendance Tracking</span>
              </div>
            </div>
          </div>

          {/* Bottom HR Notice */}
          <div className="relative z-10 pt-4 border-t border-indigo-700/50 text-xs text-indigo-300">
            <p className="flex items-center gap-1.5">
              <span>🔒</span>
              <span>HR Admin access is restricted to authorized credentials.</span>
            </p>
          </div>
        </div>

        {/* Right Panel: Form (7 cols on large screens) */}
        <div className="lg:col-span-7 p-8 sm:p-10 lg:p-12 flex flex-col justify-center bg-white">
          <div className="max-w-md w-full mx-auto">
            
            {/* Tab Switcher */}
            <div className="flex bg-slate-100 p-1.5 rounded-2xl mb-8 border border-slate-200">
              <button
                type="button"
                onClick={() => { setIsRegister(false); setError(''); setSuccess(''); }}
                className={`flex-1 py-2.5 text-xs font-semibold rounded-xl transition-all cursor-pointer ${
                  !isRegister
                    ? 'bg-white text-indigo-700 shadow-sm'
                    : 'text-slate-500 hover:text-slate-800'
                }`}
              >
                Sign In
              </button>
              <button
                type="button"
                onClick={() => { setIsRegister(true); setError(''); setSuccess(''); }}
                className={`flex-1 py-2.5 text-xs font-semibold rounded-xl transition-all cursor-pointer ${
                  isRegister
                    ? 'bg-white text-indigo-700 shadow-sm'
                    : 'text-slate-500 hover:text-slate-800'
                }`}
              >
                Employee Registration
              </button>
            </div>

            {/* Header Info */}
            <div className="mb-6">
              <h3 className="text-2xl font-bold text-slate-800">
                {!isRegister ? 'Welcome back' : 'Create Employee Account'}
              </h3>
              <p className="text-xs text-slate-500 mt-1">
                {!isRegister
                  ? 'Sign in with your HR or Employee account credentials'
                  : 'Self-registration for new company employees only'}
              </p>
            </div>

            {/* Feedback Alerts */}
            {error && (
              <div className="mb-5 p-3.5 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-start gap-2">
                <span className="text-sm">⚠️</span>
                <span className="flex-1">{error}</span>
              </div>
            )}
            {success && (
              <div className="mb-5 p-3.5 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-xl flex items-start gap-2">
                <span className="text-sm">✓</span>
                <span className="flex-1">{success}</span>
              </div>
            )}

            {/* LOGIN FORM */}
            {!isRegister ? (
              <form onSubmit={handleLogin} className="space-y-4">
                <div>
                  <label className="label" htmlFor="loginEmail">Email Address</label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400 text-sm">
                      ✉️
                    </span>
                    <input
                      id="loginEmail"
                      type="email"
                      className="input pl-9"
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
                    <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400 text-sm">
                      🔒
                    </span>
                    <input
                      id="loginPassword"
                      type="password"
                      className="input pl-9"
                      placeholder="••••••••"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="btn-primary w-full py-3 mt-2 flex items-center justify-center gap-2 text-sm shadow-md shadow-indigo-600/20"
                >
                  {loading ? (
                    <>
                      <span className="inline-block animate-spin">⏳</span>
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
                    className="text-xs text-slate-500 hover:text-indigo-600 hover:underline cursor-pointer"
                  >
                    Forgot password?
                  </button>
                </div>

                <div className="mt-4 p-3 bg-slate-50 border border-slate-200/80 rounded-xl text-center">
                  <p className="text-xs text-slate-500">
                    New Employee?{' '}
                    <button
                      type="button"
                      onClick={() => setIsRegister(true)}
                      className="text-indigo-600 font-semibold hover:underline cursor-pointer"
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
                  <div className="flex bg-slate-100 p-1 rounded-xl border border-slate-200">
                    <button
                      type="button"
                      onClick={() => setAccountType('EMPLOYEE')}
                      className={`flex-1 py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
                        accountType === 'EMPLOYEE'
                          ? 'bg-white text-indigo-700 shadow-sm'
                          : 'text-slate-500 hover:text-slate-800'
                      }`}
                    >
                      👤 Employee
                    </button>
                    <button
                      type="button"
                      onClick={() => setAccountType('HR')}
                      className={`flex-1 py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
                        accountType === 'HR'
                          ? 'bg-white text-indigo-700 shadow-sm'
                          : 'text-slate-500 hover:text-slate-800'
                      }`}
                    >
                      🛡️ HR / Admin
                    </button>
                  </div>
                </div>

                {accountType === 'HR' ? (
                  <div className="p-4 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-800 space-y-2">
                    <p className="font-semibold flex items-center gap-1.5">
                      <span>🔒</span> HR accounts are not self-registrable
                    </p>
                    <p>
                      For security, HR/Admin access is set up only by the system
                      administrator. If you are HR staff, please sign in with the
                      credentials you were given, or contact your administrator.
                    </p>
                    <button
                      type="button"
                      onClick={() => { setIsRegister(false); }}
                      className="text-indigo-600 font-semibold hover:underline cursor-pointer mt-1"
                    >
                      Go to Sign In →
                    </button>
                  </div>
                ) : (
              /* EMPLOYEE REGISTRATION FORM */
              <form onSubmit={handleRegister} className="space-y-3.5">
                <div className="grid grid-cols-2 gap-3">
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
                  <input
                    id="regPassword"
                    type="password"
                    className="input"
                    placeholder="Create a strong password"
                    value={regForm.password}
                    onChange={(e) => setRegForm({ ...regForm, password: e.target.value })}
                    required
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
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
                  className="btn-primary w-full py-3 mt-3 flex items-center justify-center gap-2 text-sm shadow-md shadow-indigo-600/20"
                >
                  {loading ? (
                    <>
                      <span className="inline-block animate-spin">⏳</span>
                      Creating Account...
                    </>
                  ) : (
                    'Complete Employee Registration'
                  )}
                </button>

                <div className="text-center pt-2">
                  <p className="text-xs text-slate-500">
                    Already registered?{' '}
                    <button
                      type="button"
                      onClick={() => setIsRegister(false)}
                      className="text-indigo-600 font-semibold hover:underline cursor-pointer"
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

      {/* Forgot Password modal */}
      {showForgotPassword && (
        <div
          className="fixed inset-0 bg-slate-900/50 flex items-center justify-center p-4 z-50"
          onClick={() => setShowForgotPassword(false)}
        >
          <div
            className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="text-lg font-bold text-slate-800 mb-3 flex items-center gap-2">
              🔑 Reset Password
            </h3>
            <div className="space-y-3 text-sm text-slate-600">
              <p>
                <span className="font-semibold text-slate-800">Employees:</span>{' '}
                Please contact your HR administrator to have your password reset.
              </p>
              <p>
                <span className="font-semibold text-slate-800">HR / Admin:</span>{' '}
                Please contact your system administrator to reset your credentials.
              </p>
            </div>
            <button
              type="button"
              onClick={() => setShowForgotPassword(false)}
              className="btn-primary w-full py-2.5 mt-5 text-sm"
            >
              Got it
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
