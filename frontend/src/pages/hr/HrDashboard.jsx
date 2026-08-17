import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axiosInstance from '../../api/axiosInstance';

export default function HrDashboard() {
  const [stats, setStats] = useState({ employees: 0, openJobs: 0, pending: 0, candidates: 0 });
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
      )}

      {/* Quick actions */}
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
