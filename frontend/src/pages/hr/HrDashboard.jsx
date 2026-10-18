import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell,
} from 'recharts';
import { FiArrowUpRight, FiPlus, FiTrendingUp } from 'react-icons/fi';
import axiosInstance from '../../api/axiosInstance';

// Design palette: orange highlights, black, greys
const STATUS_COLORS = {
  APPLIED: '#D4D4D4', SHORTLISTED: '#FFAD7D', HIRED: '#F26419', REJECTED: '#171717',
  PRESENT: '#F26419', HALF_DAY: '#FFAD7D', ON_LEAVE: '#A3A3A3', ABSENT: '#171717',
};

// { HALF_DAY: 2 } -> [{ key: 'HALF_DAY', name: 'Half day', value: 2 }]
const toChartData = (obj = {}) =>
  Object.entries(obj).map(([key, value]) => ({
    key,
    name: key.charAt(0) + key.slice(1).toLowerCase().replace('_', ' '),
    value,
  }));

const tooltipStyle = {
  contentStyle: { borderRadius: 16, border: '1px solid #e5e5e5', fontFamily: 'Urbanist, sans-serif' },
};

// Donut/pie with a dot legend underneath
function StatusPie({ data, donut, centerLabel }) {
  const total = data.reduce((sum, d) => sum + d.value, 0);
  if (total === 0) return <p className="text-sm text-neutral-400 py-12 text-center">No data yet.</p>;

  const visible = data.filter(d => d.value > 0);
  return (
    <>
      <div className="relative w-full min-w-0" style={{ height: 220 }}>
        <ResponsiveContainer>
          <PieChart>
            <Pie isAnimationActive={false} data={visible} dataKey="value" nameKey="name"
              innerRadius={donut ? 62 : 0} outerRadius={92} paddingAngle={donut ? 3 : 1}
              stroke="#fff" strokeWidth={2}>
              {visible.map(d => <Cell key={d.key} fill={STATUS_COLORS[d.key] || '#A3A3A3'} />)}
            </Pie>
            <Tooltip {...tooltipStyle} />
          </PieChart>
        </ResponsiveContainer>
        {donut && (
          <div className="absolute inset-0 grid place-items-center pointer-events-none">
            <div className="text-center">
              <p className="text-3xl font-light">{total}</p>
              <p className="text-xs text-neutral-500">{centerLabel}</p>
            </div>
          </div>
        )}
      </div>
      <ul className="grid grid-cols-2 gap-x-4 gap-y-2 mt-4 text-sm">
        {data.map(d => (
          <li key={d.key} className="flex items-center justify-between gap-2">
            <span className="flex items-center gap-2 text-neutral-600">
              <span className="w-2 h-2 rounded-full" style={{ background: STATUS_COLORS[d.key] }} />
              {d.name}
            </span>
            <span className="font-medium">{d.value}</span>
          </li>
        ))}
      </ul>
    </>
  );
}

function StatCard({ label, value, caption, to, highlight }) {
  return (
    <Link
      to={to}
      className={`group rounded-3xl p-5 sm:p-6 min-h-[170px] flex flex-col justify-between transition-transform hover:-translate-y-0.5
        ${highlight ? 'bg-brand-500 text-white' : 'bg-white text-neutral-900'}`}
    >
      <div className="flex items-start justify-between gap-3">
        <p className={`text-sm ${highlight ? 'text-neutral-950' : 'text-neutral-500'}`}>{label}</p>
        <span className={`w-9 h-9 rounded-full grid place-items-center border transition-colors
          ${highlight ? 'border-neutral-950/30 text-neutral-950 group-hover:bg-white'
                      : 'border-neutral-300 group-hover:bg-neutral-900 group-hover:text-white group-hover:border-neutral-900'}`}>
          <FiArrowUpRight className="w-4 h-4" aria-hidden="true" />
        </span>
      </div>
      <div>
        <p className="text-5xl font-light tracking-tight">{value}</p>
        <p className={`text-xs mt-1 ${highlight ? 'text-neutral-900' : 'text-neutral-400'}`}>{caption}</p>
      </div>
    </Link>
  );
}

export default function HrDashboard() {
  const [stats, setStats]     = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  useEffect(() => {
    // One request gives all numbers (counted with GROUP BY on the server)
    axiosInstance.get('/dashboard/stats')
      .then(r => setStats(r.data))
      .catch(() => setError('Could not load dashboard data.'))
      .finally(() => setLoading(false));
  }, []);

  const deptData = stats
    ? Object.entries(stats.employeesByDepartment).map(([name, count]) => ({ name, count }))
    : [];
  const maxDept = Math.max(0, ...deptData.map(d => d.count));

  // Share of employees marked present today
  const presentToday = stats?.attendanceToday?.PRESENT ?? 0;
  const presentRate = stats?.totalEmployees ? Math.round((presentToday / stats.totalEmployees) * 100) : 0;

  const today = new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long' });

  return (
    <div className="w-full min-w-0">
      <div className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4 mb-6">
        <div>
          <p className="text-sm text-neutral-500 mb-1">{today}</p>
          <h2 className="text-3xl sm:text-4xl font-light text-neutral-900">HR Dashboard</h2>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link to="/hr/employees/new" className="btn-primary text-sm"><FiPlus aria-hidden="true" /> Employee</Link>
          <Link to="/hr/jobs/new" className="btn-secondary text-sm"><FiPlus aria-hidden="true" /> Job</Link>
        </div>
      </div>

      {loading ? (
        <p className="text-neutral-500">Loading…</p>
      ) : error ? (
        <p className="text-sm text-red-600 bg-red-50 px-4 py-3 rounded-2xl">{error}</p>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-4">
            <StatCard label="Total Employees"  value={stats.totalEmployees}  caption="Active profiles"      to="/hr/employees" highlight />
            <StatCard label="Open Jobs"        value={stats.openJobs}        caption="Accepting applicants" to="/hr/jobs" />
            <StatCard label="Pending Leaves"   value={stats.pendingLeaves}   caption="Waiting for review"   to="/hr/leave" />
            <StatCard label="Total Candidates" value={stats.totalCandidates} caption="Across all jobs"      to="/hr/candidates" />
          </div>

          <div className="grid grid-cols-1 xl:grid-cols-3 gap-4">
            <div className="card xl:col-span-2">
              <div className="mb-5">
                <p className="stat-label">Headcount</p>
                <h3 className="text-xl font-normal">Employees by Department</h3>
              </div>
              {deptData.length === 0 ? (
                <p className="text-sm text-neutral-400 py-12 text-center">No employee data yet.</p>
              ) : (
                <div className="w-full min-w-0 overflow-hidden" style={{ height: 280 }}>
                  <ResponsiveContainer>
                    <BarChart data={deptData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                      <CartesianGrid strokeDasharray="2 6" stroke="#d4d4d4" vertical={false} />
                      <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#737373' }} axisLine={false} tickLine={false} />
                      <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: '#a3a3a3' }} axisLine={false} tickLine={false} />
                      <Tooltip cursor={{ fill: 'rgba(0,0,0,0.04)' }} {...tooltipStyle} />
                      <Bar isAnimationActive={false} dataKey="count" name="Employees" radius={[12, 12, 12, 12]} maxBarSize={48}>
                        {/* Biggest department in orange, the rest in black */}
                        {deptData.map(d => <Cell key={d.name} fill={d.count === maxDept ? '#F26419' : '#171717'} />)}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              )}
            </div>

            {/* Attendance rate on a dotted texture, like the design's payload chart */}
            <div className="card relative overflow-hidden">
              <div className="absolute inset-0 dot-grid opacity-60 pointer-events-none" aria-hidden="true" />
              <div className="relative">
                <p className="stat-label">Today</p>
                <h3 className="text-xl font-normal mb-6">Attendance Rate</h3>
                <p className="flex items-baseline gap-2">
                  <FiTrendingUp className="w-5 h-5 text-emerald-600 self-center" aria-hidden="true" />
                  <span className="text-6xl font-light tracking-tight">{presentRate}</span>
                  <span className="text-xl text-neutral-500">%</span>
                </p>
                <p className="text-sm text-neutral-500 mt-1">
                  {presentToday} of {stats.totalEmployees} employees present
                </p>
                <div className="mt-6 h-2 rounded-full bg-neutral-200 overflow-hidden">
                  <div className="h-full rounded-full bg-brand-500 transition-all" style={{ width: `${presentRate}%` }} />
                </div>
              </div>
            </div>

            <div className="card">
              <p className="stat-label">Candidates</p>
              <h3 className="text-xl font-normal mb-2">Hiring Funnel</h3>
              <StatusPie data={toChartData(stats.candidatesByStatus)} donut centerLabel="candidates" />
            </div>

            <div className="card">
              <p className="stat-label">Today</p>
              <h3 className="text-xl font-normal mb-2">Attendance Breakdown</h3>
              <StatusPie data={toChartData(stats.attendanceToday)} />
            </div>

            <div className="card">
              <p className="stat-label">Shortcuts</p>
              <h3 className="text-xl font-normal mb-4">Quick Actions</h3>
              <div className="flex flex-col gap-2">
                {[
                  { to: '/hr/leave', label: 'Review leave requests', count: stats.pendingLeaves },
                  { to: '/hr/candidates', label: 'Screen candidates', count: stats.candidatesByStatus?.APPLIED },
                  { to: '/hr/attendance', label: 'Attendance overview' },
                  { to: '/hr/departments', label: 'Manage departments' },
                ].map(a => (
                  <Link key={a.to} to={a.to}
                    className="flex items-center justify-between gap-3 px-4 py-3 rounded-full border border-neutral-200 hover:border-neutral-900 transition-colors text-sm">
                    <span>{a.label}</span>
                    <span className="flex items-center gap-2">
                      {a.count > 0 && <span className="px-2 py-0.5 rounded-full bg-brand-500 text-white text-xs">{a.count}</span>}
                      <FiArrowUpRight className="w-4 h-4 text-neutral-500" aria-hidden="true" />
                    </span>
                  </Link>
                ))}
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
