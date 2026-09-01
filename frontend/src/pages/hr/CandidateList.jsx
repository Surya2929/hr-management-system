import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';

export default function CandidateList() {
  const [jobs, setJobs]               = useState([]);
  const [selectedJob, setSelectedJob] = useState('all');
  const [candidates, setCandidates]   = useState([]);
  const [loading, setLoading]         = useState(false);
  const [aiLoading, setAiLoading]     = useState({});
  const [expanded, setExpanded]       = useState(null);

  // Add Candidate Modal State
  const [showAddModal, setShowAddModal] = useState(false);
  const [addForm, setAddForm]           = useState({
    jobPostingId: '',
    fullName: '',
    email: '',
    phone: '',
    resume: null,
  });
  const [uploading, setUploading] = useState(false);
  const [addError, setAddError]   = useState('');

  useEffect(() => {
    axiosInstance.get('/jobs').then(r => setJobs(r.data));
    fetchCandidates('all');
  }, []);

  const fetchCandidates = (jobId) => {
    setLoading(true);
    const url = jobId === 'all' ? '/candidates/ranked' : `/candidates/job/${jobId}`;
    axiosInstance.get(url)
      .then(r => setCandidates(r.data))
      .catch(console.error)
      .finally(() => setLoading(false));
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

  const handleAddCandidate = async (e) => {
    e.preventDefault();
    setAddError('');

    if (!addForm.jobPostingId) {
      setAddError('Please select a job posting.');
      return;
    }
    if (!addForm.resume) {
      setAddError('Please upload a resume in PDF format.');
      return;
    }

    setUploading(true);

    try {
      const formData = new FormData();
      formData.append('jobPostingId', addForm.jobPostingId);
      formData.append('fullName', addForm.fullName);
      formData.append('email', addForm.email);
      formData.append('phone', addForm.phone || '');
      formData.append('resume', addForm.resume);

      await axiosInstance.post('/candidates/add', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      // Reset & close modal
      setAddForm({ jobPostingId: '', fullName: '', email: '', phone: '', resume: null });
      setShowAddModal(false);
      fetchCandidates(selectedJob);
    } catch (err) {
      setAddError(err.response?.data?.message || 'Failed to add candidate. Ensure file is a valid PDF.');
    } finally {
      setUploading(false);
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
    APPLIED: 'badge-pending',
    SHORTLISTED: 'badge-approved',
    REJECTED: 'badge-rejected',
    HIRED: 'badge-open',
  };

  return (
    <div>
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">🤖 AI Candidate Screening</h2>
          <p className="text-xs text-slate-500 mt-0.5">Upload resumes, screen candidates with Groq AI, and generate interview questions.</p>
        </div>
        <button
          onClick={() => { setShowAddModal(true); setAddError(''); }}
          className="btn-primary text-sm flex items-center justify-center gap-2 cursor-pointer py-2.5 px-4"
        >
          <span>+ Add Candidate Resume</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="card mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <label className="label mb-0 whitespace-nowrap" htmlFor="jobFilter">Filter by Job:</label>
          <select
            id="jobFilter"
            className="input max-w-xs"
            value={selectedJob}
            onChange={e => handleJobChange(e.target.value)}
          >
            <option value="all">All Jobs ({candidates.length})</option>
            {jobs.map(j => (
              <option key={j.id} value={j.id}>{j.title}</option>
            ))}
          </select>
        </div>
        <div className="text-xs text-slate-500">
          Showing <span className="font-semibold text-slate-700">{candidates.length}</span> candidate(s)
        </div>
      </div>

      {/* Candidates List */}
      {loading ? (
        <div className="card text-center py-12">
          <p className="text-slate-400 text-sm">Loading candidates…</p>
        </div>
      ) : (
        <div className="space-y-4">
          {candidates.length === 0 && (
            <div className="card text-center py-12">
              <p className="text-slate-500 font-medium mb-1">No candidates found.</p>
              <p className="text-xs text-slate-400 mb-4">Click "Add Candidate Resume" to manually upload applicant resumes for evaluation.</p>
              <button
                onClick={() => setShowAddModal(true)}
                className="btn-primary text-xs py-2 px-4 inline-block"
              >
                + Add First Candidate
              </button>
            </div>
          )}

          {candidates.map(c => (
            <div key={c.id} className="card hover:shadow-md transition-shadow">
              <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
                
                {/* Candidate Info */}
                <div className="flex-1">
                  <div className="flex items-center gap-3 flex-wrap mb-1">
                    <h3 className="font-bold text-slate-800 text-base">{c.fullName}</h3>
                    <span className={statusColors[c.status] || 'badge-pending'}>{c.status}</span>
                    {c.aiScore != null && (
                      <span className={`text-xs px-2.5 py-1 rounded-full bg-slate-100 ${scoreColor(c.aiScore)}`}>
                        ★ AI Match: {c.aiScore}/100
                      </span>
                    )}
                  </div>

                  <p className="text-xs text-slate-500">
                    {c.email} {c.phone ? `· ${c.phone}` : ''}
                  </p>

                  <div className="mt-2 text-xs text-slate-600">
                    <span className="font-semibold text-slate-700">Applied Job:</span>{' '}
                    <span className="text-indigo-600 font-medium">{c.jobPosting?.title || `Job #${c.jobPosting?.id}`}</span>
                    {c.appliedAt && (
                      <span className="text-slate-400 ml-2">
                        ({new Date(c.appliedAt).toLocaleDateString()})
                      </span>
                    )}
                  </div>

                  {/* Skills Pills */}
                  {c.matchingSkills && (
                    <div className="mt-3 flex flex-wrap gap-1.5 items-center">
                      <span className="text-xs font-medium text-slate-500 mr-1">Skills:</span>
                      {parseJson(c.matchingSkills).map(s => (
                        <span key={s} className="px-2 py-0.5 bg-emerald-50 text-emerald-700 border border-emerald-200 text-xs rounded-md">
                          ✓ {s}
                        </span>
                      ))}
                      {parseJson(c.missingSkills).map(s => (
                        <span key={s} className="px-2 py-0.5 bg-red-50 text-red-600 border border-red-200 text-xs rounded-md">
                          ✗ {s}
                        </span>
                      ))}
                    </div>
                  )}

                  {/* AI Summary Reasoning */}
                  {c.aiSummary && (
                    <div className="mt-3 p-3 bg-indigo-50/60 border border-indigo-100 rounded-xl text-xs text-indigo-950">
                      <p className="font-semibold text-indigo-800 mb-0.5">AI Evaluation Summary:</p>
                      <p className="italic">"{c.aiSummary}"</p>
                    </div>
                  )}
                </div>

                {/* Actions Panel */}
                <div className="flex flex-row lg:flex-col gap-2 min-w-fit items-stretch justify-end pt-2 lg:pt-0 border-t lg:border-t-0 border-slate-100">
                  <button
                    onClick={() => screenResume(c.id)}
                    disabled={aiLoading[`screen_${c.id}`]}
                    className="text-xs btn-primary py-2 px-3 flex items-center justify-center gap-1.5"
                  >
                    {aiLoading[`screen_${c.id}`] ? (
                      <>
                        <span className="inline-block animate-spin">⏳</span>
                        Screening...
                      </>
                    ) : (
                      '🤖 Screen Resume'
                    )}
                  </button>

                  <button
                    onClick={() => generateQuestions(c.id)}
                    disabled={aiLoading[`q_${c.id}`]}
                    className="text-xs btn-secondary py-2 px-3 flex items-center justify-center gap-1.5"
                  >
                    {aiLoading[`q_${c.id}`] ? (
                      <>
                        <span className="inline-block animate-spin">⏳</span>
                        Generating...
                      </>
                    ) : (
                      '❓ Interview Qs'
                    )}
                  </button>

                  <select
                    value={c.status}
                    onChange={e => updateStatus(c.id, e.target.value)}
                    className="text-xs border border-slate-300 rounded-lg px-2.5 py-1.5 bg-white font-medium text-slate-700 cursor-pointer"
                  >
                    <option value="APPLIED">Status: Applied</option>
                    <option value="SHORTLISTED">Status: Shortlisted</option>
                    <option value="REJECTED">Status: Rejected</option>
                    <option value="HIRED">Status: Hired</option>
                  </select>
                </div>

              </div>

              {/* Collapsible Interview Questions */}
              {c.interviewQuestions && (
                <div className="mt-4 pt-3 border-t border-slate-100">
                  <button
                    onClick={() => setExpanded(expanded === c.id ? null : c.id)}
                    className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center gap-1 cursor-pointer"
                  >
                    <span>{expanded === c.id ? '▲ Hide Generated Interview Questions' : '▼ View 5 Personalized Interview Questions'}</span>
                  </button>
                  
                  {expanded === c.id && (
                    <div className="mt-3 space-y-2">
                      {parseJson(c.interviewQuestions).map((q, i) => (
                        <div key={i} className="text-xs text-slate-800 bg-slate-50 border border-slate-200 p-3 rounded-xl">
                          <span className="font-bold text-indigo-600 mr-1.5">Q{i + 1}.</span> {q}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

            </div>
          ))}
        </div>
      )}

      {/* ADD CANDIDATE MODAL */}
      {showAddModal && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-3xl shadow-2xl max-w-lg w-full p-6 sm:p-8 border border-slate-200">
            
            <div className="flex items-center justify-between pb-3 border-b border-slate-100 mb-5">
              <h3 className="text-lg font-bold text-slate-800">Add Candidate Application</h3>
              <button
                onClick={() => setShowAddModal(false)}
                className="text-slate-400 hover:text-slate-600 text-lg cursor-pointer"
              >
                ✕
              </button>
            </div>

            {addError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl">
                {addError}
              </div>
            )}

            <form onSubmit={handleAddCandidate} className="space-y-4">
              <div>
                <label className="label" htmlFor="candJob">Link to Job Posting *</label>
                <select
                  id="candJob"
                  className="input"
                  value={addForm.jobPostingId}
                  onChange={e => setAddForm({ ...addForm, jobPostingId: e.target.value })}
                  required
                >
                  <option value="">— Select Job Role —</option>
                  {jobs.map(j => (
                    <option key={j.id} value={j.id}>{j.title} ({j.location || 'Remote'})</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="label" htmlFor="candName">Candidate Full Name *</label>
                <input
                  id="candName"
                  type="text"
                  className="input"
                  placeholder="e.g. Rahul Sharma"
                  value={addForm.fullName}
                  onChange={e => setAddForm({ ...addForm, fullName: e.target.value })}
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="label" htmlFor="candEmail">Email Address *</label>
                  <input
                    id="candEmail"
                    type="email"
                    className="input"
                    placeholder="candidate@email.com"
                    value={addForm.email}
                    onChange={e => setAddForm({ ...addForm, email: e.target.value })}
                    required
                  />
                </div>
                <div>
                  <label className="label" htmlFor="candPhone">Phone</label>
                  <input
                    id="candPhone"
                    type="tel"
                    className="input"
                    placeholder="+91 98765 43210"
                    value={addForm.phone}
                    onChange={e => setAddForm({ ...addForm, phone: e.target.value })}
                  />
                </div>
              </div>

              <div>
                <label className="label" htmlFor="candResume">Resume (PDF Only) *</label>
                <input
                  id="candResume"
                  type="file"
                  accept=".pdf,application/pdf"
                  className="input file:mr-3 file:py-1 file:px-3 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100"
                  onChange={e => setAddForm({ ...addForm, resume: e.target.files[0] || null })}
                  required
                />
                <p className="text-xs text-slate-400 mt-1">PDF text will be extracted automatically for AI evaluation.</p>
              </div>

              <div className="flex gap-3 pt-3 border-t border-slate-100">
                <button
                  type="submit"
                  disabled={uploading}
                  className="btn-primary flex-1 py-2.5 text-xs flex items-center justify-center gap-2"
                >
                  {uploading ? (
                    <>
                      <span className="inline-block animate-spin">⏳</span>
                      Uploading & Parsing...
                    </>
                  ) : (
                    'Upload & Save Candidate'
                  )}
                </button>
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="btn-secondary text-xs px-4"
                >
                  Cancel
                </button>
              </div>
            </form>

          </div>
        </div>
      )}

    </div>
  );
}
