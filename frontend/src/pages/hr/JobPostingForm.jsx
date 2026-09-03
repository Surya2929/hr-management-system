import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axiosInstance from '../../api/axiosInstance';

export default function JobPostingForm() {
  const { id }   = useParams();
  const isEdit   = Boolean(id);
  const navigate = useNavigate();

  const [form, setForm] = useState({
    title: '', description: '', requiredSkills: '',
    location: '', salary: '', employmentType: 'FULL_TIME',
  });
  const [loading, setLoading]     = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [error, setError]         = useState('');

  useEffect(() => {
    if (isEdit) {
      axiosInstance.get(`/jobs/${id}`).then(r => {
        const j = r.data;
        setForm({
          title:          j.title || '',
          description:    j.description || '',
          requiredSkills: j.requiredSkills || '',
          location:       j.location || '',
          salary:         j.salary || '',
          employmentType: j.employmentType || 'FULL_TIME',
        });
      });
    }
  }, [id]);

  const set = (f) => (e) => setForm(prev => ({ ...prev, [f]: e.target.value }));

  const handleGenerateWithAI = async () => {
    if (!form.title) {
      setError('Please enter a job title first.');
      return;
    }
    setError('');
    setAiLoading(true);
    try {
      const res = await axiosInstance.post('/ai/generate-description', {
        title: form.title,
        skills: form.requiredSkills,
      });
      setForm(prev => ({ ...prev, description: res.data.description }));
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate description. Please try again.');
    } finally {
      setAiLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      if (isEdit) {
        await axiosInstance.put(`/jobs/${id}`, form);
      } else {
        await axiosInstance.post('/jobs', form);
      }
      navigate('/hr/jobs');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-800 mb-6">
        {isEdit ? 'Edit Job Posting' : 'New Job Posting'}
      </h2>

      <div className="card max-w-2xl">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label" htmlFor="jobTitle">Job Title *</label>
            <input id="jobTitle" className="input" value={form.title} onChange={set('title')} required />
          </div>

          <div>
            <label className="label" htmlFor="skills">Required Skills</label>
            <input id="skills" className="input" placeholder="Java, Spring Boot, MySQL…"
              value={form.requiredSkills} onChange={set('requiredSkills')} />
            <p className="text-xs text-slate-400 mt-1">
              Comma-separated. The AI screener uses this to score candidates.
            </p>
          </div>

          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="label mb-0" htmlFor="jobDesc">Description</label>
              <button
                type="button"
                onClick={handleGenerateWithAI}
                disabled={aiLoading}
                className="text-xs font-semibold text-indigo-600 hover:text-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {aiLoading ? '✨ Generating…' : '✨ Generate with AI'}
              </button>
            </div>
            <textarea id="jobDesc" rows={6} className="input resize-none"
              value={form.description} onChange={set('description')}
              placeholder="Enter a job title and skills above, then click 'Generate with AI', or write your own description here." />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="label" htmlFor="location">Location</label>
              <input id="location" className="input" placeholder="Bangalore / Remote"
                value={form.location} onChange={set('location')} />
            </div>
            <div>
              <label className="label" htmlFor="salary">Salary</label>
              <input id="salary" className="input" placeholder="₹6-9 LPA"
                value={form.salary} onChange={set('salary')} />
            </div>
          </div>

          <div>
            <label className="label" htmlFor="empType">Employment Type</label>
            <select id="empType" className="input" value={form.employmentType} onChange={set('employmentType')}>
              <option value="FULL_TIME">Full Time</option>
              <option value="PART_TIME">Part Time</option>
              <option value="CONTRACT">Contract</option>
              <option value="INTERNSHIP">Internship</option>
            </select>
          </div>

          {error && <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded">{error}</p>}

          <div className="flex flex-col sm:flex-row gap-3">
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Saving…' : isEdit ? 'Update Job' : 'Post Job'}
            </button>
            <button type="button" onClick={() => navigate('/hr/jobs')} className="btn-secondary">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
}
