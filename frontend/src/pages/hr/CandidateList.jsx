import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';
import Pagination from '../../components/Pagination';
import useDebounce from '../../hooks/useDebounce';
import { FiCpu, FiFileText, FiHelpCircle, FiLoader } from 'react-icons/fi';

const PAGE_SIZE = 10;

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
  const [uploading, setUploading]   = useState(false);
  const [addError, setAddError]     = useState('');
  const [extracting, setExtracting] = useState(false);
  const [extractNote, setExtractNote] = useState('');

  // Search, filters and paging (all handled by GET /candidates/ranked)
  const [search, setSearch]             = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [page, setPage]                 = useState(0);
  const [pageInfo, setPageInfo]         = useState(null);
  const [reload, setReload]             = useState(0); // bump to refetch the current page
  const debouncedSearch = useDebounce(search, 400);

  useEffect(() => {
    axiosInstance.get('/jobs').then(r => setJobs(r.data));
  }, []);

  useEffect(() => {
    let ignore = false; // drop the answer of an older request if a newer one was sent
    setLoading(true);
    axiosInstance.get('/candidates/ranked', {
      params: {
        search: debouncedSearch || undefined,
        status: selectedStatus || undefined,
        jobId: selectedJob === 'all' ? undefined : selectedJob,
        page,
        size: PAGE_SIZE,
      },
    })
      .then(r => {
        if (ignore) return;
        setCandidates(r.data.content);
        setPageInfo(r.data);
      })
      .catch(console.error)
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, [debouncedSearch, selectedStatus, selectedJob, page, reload]);

  const fetchCandidates = () => setReload(r => r + 1);

  const handleJobChange = (jobId) => {
    setSelectedJob(jobId);
    setPage(0);
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

  // Opens the stored resume PDF in a new tab. The API needs the JWT header,
  // so we download it with axios and show it from a temporary blob URL.
  const viewResume = async (candidateId) => {
    const tab = window.open('', '_blank'); // open now (inside the click) so popup blockers allow it
    try {
      const res = await axiosInstance.get(`/candidates/${candidateId}/resume`, { responseType: 'blob' });
      const url = URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      if (tab) tab.location.href = url; else window.location.href = url;
      setTimeout(() => URL.revokeObjectURL(url), 60_000); // free memory after a minute
    } catch (err) {
      if (tab) tab.close();
      // Error body is a Blob here, so read the JSON message out of it
      let message = 'Could not open the resume';
      try { message = JSON.parse(await err.response.data.text()).message || message; } catch { /* keep default */ }
      alert(message);
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

  // Fired the moment HR picks a resume file — auto-fills name/email/phone
  const handleResumeSelect = async (file) => {
    setAddForm(f => ({ ...f, resume: file }));
    setExtractNote('');
    if (!file) return;

    setExtracting(true);
    try {
      const data = new FormData();
      data.append('resume', file);
      const res = await axiosInstance.post('/candidates/extract', data, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      const { fullName, email, phone } = res.data;

      setAddForm(f => ({
        ...f,
        fullName: fullName || f.fullName,
        email:    email    || f.email,
        phone:    phone    || f.phone,
      }));

      if (!fullName && !email) {
        setExtractNote('Could not auto-read details from this PDF — please fill them in manually.');
      }
    } catch (err) {
      setExtractNote('Auto-extraction failed — please fill in the details manually.');
    } finally {
      setExtracting(false);
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

      setAddForm({ jobPostingId: '', fullName: '', email: '', phone: '', resume: null });
      setExtractNote('');
      setShowAddModal(false);
      fetchCandidates();
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
          <h2 className="text-3xl sm:text-4xl font-light text-neutral-900">AI Candidate Screening</h2>
          <p className="text-xs text-neutral-500 mt-0.5">Upload resumes, screen candidates with Groq AI, and generate interview questions.</p>
        </div>
        <button
          onClick={() => { setShowAddModal(true); setAddError(''); setExtractNote(''); }}
          className="btn-primary text-sm flex items-center justify-center gap-2 cursor-pointer py-2.5 px-4"
        >
          <span>+ Add Candidate Resume</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="card mb-6">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <input
            type="search"
            className="input"
            placeholder="Search name or email…"
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(0); }}
            aria-label="Search candidates"
          />
          <select
            id="jobFilter"
            className="input"
            value={selectedJob}
            onChange={e => handleJobChange(e.target.value)}
            aria-label="Filter by job"
          >
            <option value="all">All Jobs</option>
            {jobs.map(j => (
              <option key={j.id} value={j.id}>{j.title}</option>
            ))}
          </select>
          <select
            className="input"
            value={selectedStatus}
            onChange={e => { setSelectedStatus(e.target.value); setPage(0); }}
            aria-label="Filter by status"
          >
            <option value="">All Statuses</option>
            <option value="APPLIED">Applied</option>
            <option value="SHORTLISTED">Shortlisted</option>
            <option value="REJECTED">Rejected</option>
            <option value="HIRED">Hired</option>
          </select>
        </div>
        <div className="text-xs text-neutral-500 mt-3">
          Showing <span className="font-semibold text-neutral-700">{candidates.length}</span> of{' '}
          <span className="font-semibold text-neutral-700">{pageInfo?.totalElements ?? 0}</span> candidate(s)
        </div>
      </div>

      {/* Candidates List */}
      {loading ? (
        <div className="card text-center py-12">
          <p className="text-neutral-400 text-sm">Loading candidates…</p>
        </div>
      ) : (
        <div className="space-y-4">
          {candidates.length === 0 && (
            <div className="card text-center py-12">
              <p className="text-neutral-500 font-medium mb-1">No candidates found.</p>
              <p className="text-xs text-neutral-400 mb-4">Click "Add Candidate Resume" to manually upload applicant resumes for evaluation.</p>
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
                    <h3 className="font-bold text-neutral-800 text-base">{c.fullName}</h3>
                    <span className={statusColors[c.status] || 'badge-pending'}>{c.status}</span>
                    {c.aiScore != null && (
                      <span className={`text-xs px-2.5 py-1 rounded-full bg-neutral-100 ${scoreColor(c.aiScore)}`}>
                        AI Match {c.aiScore}/100
                      </span>
                    )}
                    {c.referredByEmployee && (
                      <span className="text-xs px-2.5 py-1 rounded-full bg-white text-neutral-700 border border-neutral-300 font-medium">
                        Referred by {c.referredByEmployee}
                      </span>
                    )}
                  </div>

                  <p className="text-xs text-neutral-500">
                    {c.email} {c.phone ? `· ${c.phone}` : ''}
                  </p>

                  <div className="mt-2 text-xs text-neutral-600">
                    <span className="font-semibold text-neutral-700">Applied Job:</span>{' '}
                    <span className="text-brand-600 font-medium">{c.jobPosting?.title || `Job #${c.jobPosting?.id}`}</span>
                    {c.appliedAt && (
                      <span className="text-neutral-400 ml-2">
                        ({new Date(c.appliedAt).toLocaleDateString()})
                      </span>
                    )}
                  </div>

                  {c.matchingSkills && (
                    <div className="mt-3 flex flex-wrap gap-1.5 items-center">
                      <span className="text-xs font-medium text-neutral-500 mr-1">Skills:</span>
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

                  {c.aiSummary && (
                    <div className="mt-3 p-3 bg-brand-50/60 border border-brand-100 rounded-xl text-xs text-brand-950">
                      <p className="font-semibold text-brand-800 mb-0.5">AI Evaluation Summary:</p>
                      <p className="italic">"{c.aiSummary}"</p>
                    </div>
                  )}
                </div>

                {/* Actions Panel */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:flex lg:flex-col gap-2 lg:min-w-fit items-stretch pt-2 lg:pt-0 border-t lg:border-t-0 border-neutral-100">
                  <button
                    onClick={() => screenResume(c.id)}
                    disabled={aiLoading[`screen_${c.id}`]}
                    className="text-xs btn-primary py-2 px-3 flex items-center justify-center gap-1.5"
                  >
                    {aiLoading[`screen_${c.id}`] ? (
                      <>
                        <FiLoader className="w-4 h-4 animate-spin" aria-hidden="true" />
                        Screening...
                      </>
                    ) : (
                      <><FiCpu className="w-4 h-4" aria-hidden="true" /> Screen Resume</>
                    )}
                  </button>

                  <button
                    onClick={() => generateQuestions(c.id)}
                    disabled={aiLoading[`q_${c.id}`]}
                    className="text-xs btn-secondary py-2 px-3 flex items-center justify-center gap-1.5"
                  >
                    {aiLoading[`q_${c.id}`] ? (
                      <>
                        <FiLoader className="w-4 h-4 animate-spin" aria-hidden="true" />
                        Generating...
                      </>
                    ) : (
                      <><FiHelpCircle className="w-4 h-4" aria-hidden="true" /> Interview Qs</>
                    )}
                  </button>

                  <button
                    onClick={() => viewResume(c.id)}
                    className="text-xs btn-secondary py-2 px-3 flex items-center justify-center gap-1.5"
                  >
                    <FiFileText className="w-4 h-4" aria-hidden="true" /> View Resume
                  </button>

                  <select
                    value={c.status}
                    onChange={e => updateStatus(c.id, e.target.value)}
                    className="text-xs border border-neutral-300 rounded-full px-3 py-2 bg-white font-medium text-neutral-700 cursor-pointer"
                  >
                    <option value="APPLIED">Status: Applied</option>
                    <option value="SHORTLISTED">Status: Shortlisted</option>
                    <option value="REJECTED">Status: Rejected</option>
                    <option value="HIRED">Status: Hired</option>
                  </select>
                </div>

              </div>

              {c.interviewQuestions && (
                <div className="mt-4 pt-3 border-t border-neutral-100">
                  <button
                    onClick={() => setExpanded(expanded === c.id ? null : c.id)}
                    className="text-xs font-semibold text-brand-600 hover:text-brand-800 flex items-center gap-1 cursor-pointer"
                  >
                    <span>{expanded === c.id ? '▲ Hide Generated Interview Questions' : '▼ View 5 Personalized Interview Questions'}</span>
                  </button>

                  {expanded === c.id && (
                    <div className="mt-3 space-y-2">
                      {parseJson(c.interviewQuestions).map((q, i) => (
                        <div key={i} className="text-xs text-neutral-800 bg-neutral-50 border border-neutral-200 p-3 rounded-xl">
                          <span className="font-bold text-brand-600 mr-1.5">Q{i + 1}.</span> {q}
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

      <Pagination page={pageInfo} onChange={setPage} />

      {/* ADD CANDIDATE MODAL */}
      {showAddModal && (
        <div className="fixed inset-0 bg-neutral-900/60 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-3xl shadow-2xl max-w-lg w-full max-h-full overflow-y-auto p-5 sm:p-8 border border-neutral-200">

            <div className="flex items-center justify-between pb-3 border-b border-neutral-100 mb-5">
              <h3 className="text-lg font-bold text-neutral-800">Add Candidate Application</h3>
              <button
                onClick={() => setShowAddModal(false)}
                className="text-neutral-400 hover:text-neutral-600 text-lg cursor-pointer"
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
                <label className="label" htmlFor="candResume">Resume (PDF Only) *</label>
                <input
                  id="candResume"
                  type="file"
                  accept=".pdf,application/pdf"
                  className="input file:mr-3 file:py-1 file:px-3 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-brand-50 file:text-brand-700 hover:file:bg-brand-100"
                  onChange={e => handleResumeSelect(e.target.files[0] || null)}
                  required
                />
                {extracting ? (
                  <p className="text-xs text-brand-600 mt-1 flex items-center gap-1.5">
                    <FiLoader className="w-4 h-4 animate-spin" aria-hidden="true" />
                    Reading resume — auto-filling details below…
                  </p>
                ) : extractNote ? (
                  <p className="text-xs text-yellow-600 mt-1">{extractNote}</p>
                ) : (
                  <p className="text-xs text-neutral-400 mt-1">
                    Name, email, and phone below will be auto-filled from the resume — just review before saving.
                  </p>
                )}
              </div>

              <div>
                <label className="label" htmlFor="candName">Candidate Full Name *</label>
                <input
                  id="candName"
                  type="text"
                  className="input"
                  placeholder="Auto-filled from resume — edit if needed"
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
                    placeholder="Auto-filled from resume"
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
                    placeholder="Auto-filled if found"
                    value={addForm.phone}
                    onChange={e => setAddForm({ ...addForm, phone: e.target.value })}
                  />
                </div>
              </div>

              <div className="flex gap-3 pt-3 border-t border-neutral-100">
                <button
                  type="submit"
                  disabled={uploading || extracting}
                  className="btn-primary flex-1 py-2.5 text-xs flex items-center justify-center gap-2"
                >
                  {uploading ? (
                    <>
                      <FiLoader className="w-4 h-4 animate-spin" aria-hidden="true" />
                      Saving...
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