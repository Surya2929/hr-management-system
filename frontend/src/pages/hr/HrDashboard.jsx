import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import axiosInstance from '../../api/axiosInstance';

export default function HrDashboard() {
  const [stats, setStats] = useState({ employees: 0, openJobs: 0, pending: 0, candidates: 0 });
  const [deptChartData, setDeptChartData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      axiosInstance.get('/employees'),
      axiosInstance.get('/jobs'),
      axiosInstance.get('/leave/pending'),
      axiosInstance.get('/candidates/ranked'),
    ]).then(([emp, jobs, leaves, cands]) => {
      setStats({
        employees:  emp.data.length,
        openJobs:   jobs.data.filter(j => j.status === 'OPEN').length,
        pending:    leaves.data.length,
        candidates: cands.data.length,
      });

      // Build department-wise employee count for the chart
      const counts = {};
      emp.data.forEach(e => {
        const deptName = e.department?.name || 'Unassigned';
        counts[deptName] = (counts[deptName] || 0) + 1;
      });
      setDeptChartData(
        Object.entries(counts).map(([name, count]) => ({ name, count }))
      );
    }).catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const cards = [
    { label: 'Total Employees',    value: stats.employees,  icon: '👥', to: '/hr/employees',  color: 'bg-blue-50 text-blue-700' },
    { label: 'Open Jobs',          value: stats.openJobs,   icon: '💼', to: '/hr/jobs',        color: 'bg-emerald-50 text-emerald-700' },
    { label: 'Pending Leaves',     value: stats.pending,    icon: '📅', to: '/hr/leave',       color: 'bg-yellow-50 text-yellow-700' },
    { label: 'Total Candidates',   value: stats.candidates, icon: '🤖', to: '/hr/candidates',  color: 'bg-purple-50 text-purple-700' },
  ];

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-800 mb-6">HR Dashboard</h2>

      {loading ? (
        <p className="text-slate-500">Loading…</p>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
            {cards.map((c) => (
              <Link key={c.label} to={c.to}
                className={`card hover:shadow-md transition-shadow ${c.color}`}>
                <div className="text-3xl mb-2">{c.icon}</div>
                <div className="text-3xl font-bold">{c.value}</div>
                <div className="text-sm font-medium mt-1">{c.label}</div>
              </Link>
            ))}
          </div>

          <div className="card mb-8">
            <h3 className="font-semibold text-slate-700 mb-4">Employees by Department</h3>
            {deptChartData.length === 0 ? (
              <p className="text-sm text-slate-400">No employee data yet.</p>
            ) : (
              <div style={{ width: '100%', height: 260 }}>
                <ResponsiveContainer>
                  <BarChart data={deptChartData} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                    <Tooltip />
                    <Bar dataKey="count" fill="#4f46e5" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        </>
      )}

      <div className="card">
        <h3 className="font-semibold text-slate-700 mb-4">Quick Actions</h3>
        <div className="flex flex-wrap gap-3">
          <Link to="/hr/employees/new"  className="btn-primary text-sm">+ Add Employee</Link>
          <Link to="/hr/jobs/new"       className="btn-primary text-sm">+ Post Job</Link>
          <Link to="/hr/leave"          className="btn-secondary text-sm">📋 Review Leaves</Link>
          <Link to="/hr/candidates"     className="btn-secondary text-sm">🤖 View Candidates</Link>
        </div>
      </div>
    </div>
  );
}
