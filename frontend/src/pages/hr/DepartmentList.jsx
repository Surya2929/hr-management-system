import { useEffect, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';

export default function DepartmentList() {
  const [departments, setDepartments] = useState([]);
  const [form, setForm]   = useState({ name: '', description: '' });
  const [editing, setEditing] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetch = () => {
    axiosInstance.get('/departments').then(r => setDepartments(r.data)).finally(() => setLoading(false));
  };
  useEffect(fetch, []);

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      if (editing) {
        await axiosInstance.put(`/departments/${editing}`, form);
      } else {
        await axiosInstance.post('/departments', form);
      }
      setForm({ name: '', description: '' });
      setEditing(null);
      fetch();
    } catch (err) {
      alert(err.response?.data?.message || 'Error saving department');
    }
  };

  const startEdit = (d) => {
    setEditing(d.id);
    setForm({ name: d.name, description: d.description || '' });
  };

  const handleDelete = async (id, name) => {
    if (!confirm(`Delete department "${name}"? Employees will lose their department assignment.`)) return;
    try {
      await axiosInstance.delete(`/departments/${id}`);
      fetch();
    } catch (err) {
      alert(err.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-800 mb-6">Departments</h2>

      <div className="grid lg:grid-cols-2 gap-6">
        {/* Form */}
        <div className="card">
          <h3 className="font-semibold text-slate-700 mb-4">
            {editing ? 'Edit Department' : 'Add Department'}
          </h3>
          <form onSubmit={handleSave} className="space-y-3">
            <div>
              <label className="label" htmlFor="deptName">Name *</label>
              <input id="deptName" className="input" value={form.name}
                onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required />
            </div>
            <div>
              <label className="label" htmlFor="deptDesc">Description</label>
              <input id="deptDesc" className="input" value={form.description}
                onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
            </div>
            <div className="flex gap-2">
              <button type="submit" className="btn-primary text-sm">
                {editing ? 'Update' : 'Add'}
              </button>
              {editing && (
                <button type="button" className="btn-secondary text-sm"
                  onClick={() => { setEditing(null); setForm({ name: '', description: '' }); }}>
                  Cancel
                </button>
              )}
            </div>
          </form>
        </div>

        {/* List */}
        <div className="card">
          <h3 className="font-semibold text-slate-700 mb-4">All Departments</h3>
          {loading ? <p className="text-slate-400">Loading…</p> : (
            <ul className="divide-y divide-slate-100">
              {departments.map(d => (
                <li key={d.id} className="py-3 flex justify-between items-center">
                  <div>
                    <p className="font-medium text-sm">{d.name}</p>
                    <p className="text-xs text-slate-400">{d.description || '—'}</p>
                  </div>
                  <div className="flex gap-3 text-xs">
                    <button onClick={() => startEdit(d)} className="text-indigo-600 hover:underline">Edit</button>
                    <button onClick={() => handleDelete(d.id, d.name)} className="text-red-500 hover:underline">Delete</button>
                  </div>
                </li>
              ))}
              {departments.length === 0 && <li className="text-slate-400 text-sm py-4">No departments yet</li>}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
