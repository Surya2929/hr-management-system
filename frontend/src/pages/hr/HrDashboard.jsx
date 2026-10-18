import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import axiosInstance from '../../api/axiosInstance';

// Fixed colour per status so the charts are easy to read
const STATUS_COLORS = {
  APPLIED: '#6366f1', SHORTLISTED: '#f59e0b', REJECTED: '#ef4444', HIRED: '#10b981',
  PRESENT: '#10b981', ABSENT: '#ef4444', HALF_DAY: '#f59e0b', ON_LEAVE: '#3b82f6',
};

// { APPLIED: 3, HIRED: 1 } -> [{ name: 'Applied', key: 'APPLIED', value: 3 }, ...]
const toChartData = (obj = {}) =>
  Object.entries(obj).map(([key, value]) => ({
    key,
    name: key.charAt(0) + key.slice(1).toLowerCase().replace('_', ' '),
    value,
  }));

function StatusPie({ data, donut }) {
  const total = data.reduce((sum, d) => sum + d.value, 0);
  if (total === 0) return <p className="text-sm text-slate-400 py-10 text-center">No data yet.</p>;

  return (
    <div className="w-full min-w-0" style={{ height: 260 }}>
      <ResponsiveContainer>
        <PieChart>
          <Pie
            data={data.filter(d => d.value > 0)}
            dataKey="value"
            nameKey="name"
            innerRadius={donut ? 55 : 0}
            outerRadius={90}
            paddingAngle={donut ? 2 : 0}
          >
            {data.filter(d => d.value > 0).map(d => (
              <Cell key={d.key} fill={STATUS_COLORS[d.key] || '#94a3b8'} />
            ))}
          </Pie>
          <Tooltip />
          <Legend verticalAlign="bottom" height={36} />
        </PieChart>
      </ResponsiveContainer>
    </div>
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

  const cards = stats ? [
    { label: 'Total Employees',  value: stats.totalEmployees,  icon: '👥', to: '/hr/employees',  color: 'bg-blue-50 text-blue-700' },
    { label: 'Open Jobs',        value: stats.openJobs,        icon: '💼', to: '/hr/jobs',       color: 'bg-emerald-50 text-emerald-700' },
    { label: 'Pending Leaves',   value: stats.pendingLeaves,   icon: '📅', to: '/hr/leave',      color: 'bg-yellow-50 text-yellow-700' },
    { label: 'Total Candidates', value: stats.totalCandidates, icon: '🤖', to: '/hr/candidates', color: 'bg-purple-50 text-purple-700' },
  ] : [];

  const deptData = stats
    ? Object.entries(stats.employeesByDepartment).map(([name, count]) => ({ name, count }))
    : [];

  return (
    <div className="w-full min-w-0">
      <h2 className="text-2xl font-bold text-slate-800 mb-5 sm:mb-6">HR Dashboard</h2>

      {loading ? (
        <p className="text-slate-500">Loading…</p>
      ) : error ? (
        <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded mb-8">{error}</p>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
            {cards.map((c) => (
              <Link key={c.label} to={c.to}
                className={`card hover:shadow-md transition-shadow min-h-[150px] flex flex-col justify-center ${c.color}`}>
                <div className="text-3xl mb-2">{c.icon}</div>
                <div className="text-3xl font-bold">{c.value}</div>
                <div className="text-sm font-medium mt-1">{c.label}</div>
              </Link>
            ))}
          </div>

          <div className="card mb-6">
            <h3 className="font-semibold text-slate-700 mb-4">Employees by Department</h3>
            {deptData.length === 0 ? (
              <p className="text-sm text-slate-400">No employee data yet.</p>
            ) : (
              <div className="w-full min-w-0 overflow-hidden" style={{ height: 260 }}>
                <ResponsiveContainer>
                  <BarChart data={deptData} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                    <Tooltip />
                    <Bar dataKey="count" name="Employees" fill="#4f46e5" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
            <div className="card">
              <h3 className="font-semibold text-slate-700 mb-4">Hiring Funnel</h3>
              <StatusPie data={toChartData(stats.candidatesByStatus)} donut />
            </div>
            <div className="card">
              <h3 className="font-semibold text-slate-700 mb-4">Today's Attendance</h3>
              <StatusPie data={toChartData(stats.attendanceToday)} />
            </div>
          </div>
        </>
      )}

      <div className="card">
        <h3 className="font-semibold text-slate-700 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 sm:flex sm:flex-wrap gap-3">
          <Link to="/hr/employees/new"  className="btn-primary text-sm text-center">+ Add Employee</Link>
          <Link to="/hr/jobs/new"       className="btn-primary text-sm text-center">+ Post Job</Link>
          <Link to="/hr/leave"          className="btn-secondary text-sm text-center">📋 Review Leaves</Link>
          <Link to="/hr/candidates"     className="btn-secondary text-sm text-center">🤖 View Candidates</Link>
        </div>
      </div>
    </div>
  );
}
