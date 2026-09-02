import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';

export default function OpenPositions() {
  const [jobs, setJobs]         = useState([]);
  const [loading, setLoading]   = useState(true);
  const [referJob, setReferJob] = useState(null); // job currently being referred to

  useEffect(() => {
    axiosInstance.get('/jobs/open')
      .then(r => setJobs(r.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-800 mb-1">Open Positions</h2>
      <p className="text-sm text-slate-500 mb-6">
        Know someone great for one of these roles? Refer them below.
      </p>

      {loading ? (
        <p className="text-slate-400">Loading…</p>
      ) : jobs.length === 0 ? (
        <p className="text-slate-400">No open positions right now.</p>
      ) : (
        <div className="space-y-4">
          {jobs.map(job => (
            <div key={job.id} className="card">
              <div className="flex items-start justify-between gap-4 flex-wrap">
                <div className="flex-1 min-w-[200px]">
                  <h3 className="font-semibold text-slate-800">{job.title}</h3>
                  <p className="text-sm text-slate-500 mt-0.5">
                    {job.location || 'Location not specified'} · {job.employmentType?.replace('_', ' ')}
                  </p>
                  {job.salary && (
                    <p className="text-sm text-emerald-700 font-medium mt-1">💰 {job.salary}</p>
                  )}
                  {job.requiredSkills && (
                    <p className="text-xs text-slate-400 mt-2">Skills: {job.requiredSkills}</p>
                  )}
                  {job.description && (
                    <p className="text-sm text-slate-600 mt-3 whitespace-pre-line">{job.description}</p>
                  )}
                </div>
                <button
                  onClick={() => setReferJob(job)}
                  className="btn-primary text-sm shrink-0"
                >
                  👋 Refer Someone
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {referJob && (
        <ReferModal job={referJob} onClose={() => setReferJob(null)} />
      )}
    </div>
  );
}

function ReferModal({ job, onClose }) {
  const [form, setForm] = useState({ fullName: '', email: '', phone: '' });
  const [file, setFile]       = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');
  const [success, setSuccess] = useState(false);

  const set = (f) => (e) => setForm(prev => ({ ...prev, [f]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) { setError('Please attach the candidate\'s resume (PDF).'); return; }
    setError(''); setLoading(true);

    const data = new FormData();
    data.append('jobPostingId', job.id);
    data.append('fullName', form.fullName);
    data.append('email', form.email);
    data.append('phone', form.phone);
    data.append('resume', file);

    try {
      await axiosInstance.post('/candidates/refer', data, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setSuccess(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit referral. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center p-4 z-50" onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6" onClick={(e) => e.stopPropagation()}>
        {success ? (
          <div className="text-center py-4">
            <div className="text-4xl mb-3">✅</div>
            <h3 className="text-lg font-bold text-slate-800 mb-2">Referral Submitted!</h3>
            <p className="text-sm text-slate-500 mb-5">
              Thanks — HR will review {form.fullName}'s resume for the {job.title} role.
            </p>
            <button onClick={onClose} className="btn-primary w-full py-2.5 text-sm">Done</button>
          </div>
        ) : (
          <>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-bold text-slate-800">Refer for {job.title}</h3>
              <button onClick={onClose} className="text-slate-400 hover:text-slate-600 text-xl leading-none">×</button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-3.5">
              <div>
                <label className="label">Candidate Full Name *</label>
                <input className="input" placeholder="e.g. Rahul Sharma"
                  value={form.fullName} onChange={set('fullName')} required />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="label">Email Address *</label>
                  <input type="email" className="input" placeholder="candidate@email.com"
                    value={form.email} onChange={set('email')} required />
                </div>
                <div>
                  <label className="label">Phone</label>
                  <input className="input" placeholder="+91 98765 43210"
                    value={form.phone} onChange={set('phone')} />
                </div>
              </div>
              <div>
                <label className="label">Resume (PDF Only) *</label>
                <input type="file" accept="application/pdf" className="input"
                  onChange={(e) => setFile(e.target.files[0])} required />
                <p className="text-xs text-slate-400 mt-1">PDF text will be extracted automatically for AI evaluation.</p>
              </div>

              {error && <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded">{error}</p>}

              <div className="flex gap-3 pt-1">
                <button type="submit" disabled={loading} className="btn-primary flex-1">
                  {loading ? 'Submitting…' : 'Submit Referral'}
                </button>
                <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
              </div>
            </form>
          </>
        )}
      </div>
    </div>
  );
}