import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';

export default function CandidateList() {
  const [jobs, setJobs]           = useState([]);
  const [selectedJob, setSelectedJob] = useState('all');
  const [candidates, setCandidates]   = useState([]);
  const [loading, setLoading]     = useState(false);
  const [aiLoading, setAiLoading] = useState({});
  const [expanded, setExpanded]   = useState(null);

  useEffect(() => {
    axiosInstance.get('/jobs').then(r => setJobs(r.data));
    fetchCandidates('all');
  }, []);

  const fetchCandidates = (jobId) => {
    setLoading(true);
    const url = jobId === 'all' ? '/candidates/ranked' : `/candidates/job/${jobId}`;
    axiosInstance.get(url).then(r => setCandidates(r.data)).finally(() => setLoading(false));
  };

  const handleJobChange = (jobId) => {
    setSelectedJob(jobId);
    fetchCandidates(jobId);
  };

  const screenResume = async (candidateId) => {
    setAiLoading(l => ({ ...l, [`screen_${candidateId}`]: true }));
    try {
      const res = await axiosInstance.post(`/ai/screen/${candidateId}`);
      setCandidates(c => c.map(x => x.id === candidateId ? res.data : x));
    } catch (err) {
      alert(err.response?.data?.message || 'AI screening failed');
    } finally {
      setAiLoading(l => ({ ...l, [`screen_${candidateId}`]: false }));
    }
  };

  const generateQuestions = async (candidateId) => {
    setAiLoading(l => ({ ...l, [`q_${candidateId}`]: true }));
    try {
      const res = await axiosInstance.post(`/ai/questions/${candidateId}`);
      setCandidates(c => c.map(x => x.id === candidateId ? res.data : x));
      setExpanded(candidateId);
    } catch (err) {
      alert(err.response?.data?.message || 'Question generation failed');
    } finally {
      setAiLoading(l => ({ ...l, [`q_${candidateId}`]: false }));
    }
  };

  const updateStatus = async (candidateId, status) => {
    try {
      const res = await axiosInstance.put(`/candidates/${candidateId}/status`, { status });
      setCandidates(c => c.map(x => x.id === candidateId ? res.data : x));
    } catch (err) {
      alert('Status update failed');
    }
  };

  const parseJson = (str) => {
    try { return JSON.parse(str); } catch { return []; }
  };

  const scoreColor = (score) => {
    if (score >= 75) return 'text-emerald-600 font-bold';
    if (score >= 50) return 'text-yellow-600 font-bold';
    return 'text-red-500 font-bold';
  };

  const statusColors = {
    APPLIED: 'badge-pending', SHORTLISTED: 'badge-approved',
    REJECTED: 'badge-rejected', HIRED: 'badge-open',
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-800 mb-6">🤖 AI Candidate Screening</h2>

      {/* Job filter */}
      <div className="card mb-5">
        <label className="label" htmlFor="jobFilter">Filter by Job</label>
        <select id="jobFilter" className="input max-w-sm"
          value={selectedJob} onChange={e => handleJobChange(e.target.value)}>
          <option value="all">All Jobs</option>
          {jobs.map(j => <option key={j.id} value={j.id}>{j.title}</option>)}
        </select>
      </div>

      {loading ? <p className="text-slate-400">Loading candidates…</p> : (
        <div className="space-y-4">
          {candidates.length === 0 && <p className="text-slate-400">No candidates found.</p>}
          {candidates.map(c => (
            <div key={c.id} className="card">
              <div className="flex items-start justify-between gap-4">
                {/* Left: info */}
                <div className="flex-1">
                  <div className="flex items-center gap-3 flex-wrap mb-1">
                    <h3 className="font-semibold text-slate-800">{c.fullName}</h3>
                    <span className={statusColors[c.status] || 'badge-pending'}>{c.status}</span>
                    {c.aiScore != null && (
                      <span className={`text-sm ${scoreColor(c.aiScore)}`}>
                        AI Score: {c.aiScore}/100
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-slate-500">{c.email} · {c.phone || '—'}</p>
                  <p className="text-xs text-slate-400 mt-1">
                    Applied for: <span className="font-medium">{c.jobPosting?.title}</span>
                  </p>

                  {/* AI matching/missing skills */}
                  {c.matchingSkills && (
                    <div className="mt-2 flex flex-wrap gap-1">
                      {parseJson(c.matchingSkills).map(s => (
                        <span key={s} className="px-2 py-0.5 bg-emerald-100 text-emerald-700 text-xs rounded-full">✓ {s}</span>
                      ))}
                      {parseJson(c.missingSkills).map(s => (
                        <span key={s} className="px-2 py-0.5 bg-red-100 text-red-600 text-xs rounded-full">✗ {s}</span>
                      ))}
                    </div>
                  )}
                  {c.aiSummary && (
                    <p className="text-xs text-slate-500 mt-2 italic">"{c.aiSummary}"</p>
                  )}
                </div>

                {/* Right: actions */}
                <div className="flex flex-col gap-2 min-w-fit">
                  <button
                    onClick={() => screenResume(c.id)}
                    disabled={aiLoading[`screen_${c.id}`]}
                    className="text-xs btn-primary py-1.5 px-3">
                    {aiLoading[`screen_${c.id}`] ? '⏳ Screening…' : '🤖 Screen Resume'}
                  </button>
                  <button
                    onClick={() => generateQuestions(c.id)}
                    disabled={aiLoading[`q_${c.id}`]}
                    className="text-xs btn-secondary py-1.5 px-3">
                    {aiLoading[`q_${c.id}`] ? '⏳ Generating…' : '❓ Interview Qs'}
                  </button>
                  <select
                    value={c.status}
                    onChange={e => updateStatus(c.id, e.target.value)}
                    className="text-xs border border-slate-300 rounded px-2 py-1.5">
                    <option value="APPLIED">Applied</option>
                    <option value="SHORTLISTED">Shortlisted</option>
                    <option value="REJECTED">Rejected</option>
                    <option value="HIRED">Hired</option>
                  </select>
                </div>
              </div>

              {/* Interview questions (expandable) */}
              {c.interviewQuestions && (
                <div className="mt-4">
                  <button
                    onClick={() => setExpanded(expanded === c.id ? null : c.id)}
                    className="text-xs text-indigo-600 hover:underline">
                    {expanded === c.id ? '▲ Hide' : '▼ Show'} Interview Questions
                  </button>
                  {expanded === c.id && (
                    <ol className="mt-3 space-y-2">
                      {parseJson(c.interviewQuestions).map((q, i) => (
                        <li key={i} className="text-sm text-slate-700 bg-indigo-50 px-4 py-2 rounded-lg">
                          <span className="font-semibold text-indigo-600">Q{i + 1}.</span> {q}
                        </li>
                      ))}
                    </ol>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
