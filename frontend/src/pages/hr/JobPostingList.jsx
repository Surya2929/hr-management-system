import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axiosInstance from '../../api/axiosInstance';

export default function JobPostingList() {
  const [jobs, setJobs]       = useState([]);
  const [loading, setLoading] = useState(true);

  const fetch = () => {
    axiosInstance.get('/jobs').then(r => setJobs(r.data)).finally(() => setLoading(false));
  };
  useEffect(fetch, []);

  const toggleStatus = async (job) => {
    const action = job.status === 'OPEN' ? 'close' : 'reopen';
    try {
      await axiosInstance.put(`/jobs/${job.id}/${action}`);
      fetch();
    } catch (err) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  const handleDelete = async (id, title) => {
    if (!confirm(`Delete job "${title}"? All candidate applications will be lost.`)) return;
    try {
      await axiosInstance.delete(`/jobs/${id}`);
      fetch();
    } catch (err) {
      alert(err.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-slate-800">Job Postings</h2>
        <Link to="/hr/jobs/new" className="btn-primary text-sm">+ Post New Job</Link>
      </div>

      {loading ? <p className="text-slate-400">Loading…</p> : (
        <div className="space-y-4">
          {jobs.length === 0 && <p className="text-slate-400">No job postings yet.</p>}
          {jobs.map(job => (
            <div key={job.id} className="card flex items-start justify-between gap-4">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-1">
                  <h3 className="font-semibold text-slate-800">{job.title}</h3>
                  <span className={job.status === 'OPEN' ? 'badge-open' : 'badge-closed'}>
                    {job.status}
                  </span>
                  <span className="text-xs text-slate-400">{job.employmentType}</span>
                </div>
                <p className="text-sm text-slate-500 mb-2">{job.location || 'Location not specified'}</p>
                {job.requiredSkills && (
                  <p className="text-xs text-slate-400">Skills: {job.requiredSkills}</p>
                )}
              </div>
              <div className="flex flex-col gap-2 min-w-fit">
                <Link to={`/hr/jobs/${job.id}/edit`} className="text-xs btn-secondary py-1 px-3 text-center">Edit</Link>
                <button onClick={() => toggleStatus(job)}
                  className={`text-xs py-1 px-3 rounded-lg font-medium transition-colors
                    ${job.status === 'OPEN'
                      ? 'bg-yellow-100 text-yellow-700 hover:bg-yellow-200'
                      : 'bg-emerald-100 text-emerald-700 hover:bg-emerald-200'}`}>
                  {job.status === 'OPEN' ? 'Close' : 'Reopen'}
                </button>
                <button onClick={() => handleDelete(job.id, job.title)}
                  className="text-xs btn-danger py-1 px-3">Delete</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
