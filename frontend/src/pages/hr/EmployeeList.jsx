import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axiosInstance from '../../api/axiosInstance';

export default function EmployeeList() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [search, setSearch]       = useState('');

  const fetchEmployees = () => {
    setLoading(true);
    axiosInstance.get('/employees')
      .then(res => setEmployees(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchEmployees(); }, []);

  const handleDelete = async (id, name) => {
    if (!confirm(`Delete ${name}? This cannot be undone.`)) return;
    try {
      await axiosInstance.delete(`/employees/${id}`);
      fetchEmployees();
    } catch (err) {
      alert(err.response?.data?.message || 'Delete failed');
    }
  };

  const filtered = employees.filter(e =>
    `${e.firstName} ${e.lastName} ${e.designation} ${e.department?.name || ''}`
      .toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-slate-800">Employees</h2>
        <Link to="/hr/employees/new" className="btn-primary text-sm">+ Add Employee</Link>
      </div>

      <div className="card">
        <input
          className="input mb-4 max-w-sm"
          placeholder="Search by name, designation, department…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />

        {loading ? <p className="text-slate-500">Loading…</p> : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-slate-500 border-b">
                  <th className="pb-2 pr-4">Name</th>
                  <th className="pb-2 pr-4">Email</th>
                  <th className="pb-2 pr-4">Designation</th>
                  <th className="pb-2 pr-4">Department</th>
                  <th className="pb-2">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filtered.length === 0 ? (
                  <tr><td colSpan={5} className="py-8 text-center text-slate-400">No employees found</td></tr>
                ) : filtered.map(emp => (
                  <tr key={emp.id} className="hover:bg-slate-50">
                    <td className="py-3 pr-4 font-medium">{emp.firstName} {emp.lastName}</td>
                    <td className="py-3 pr-4 text-slate-500">{emp.user?.email}</td>
                    <td className="py-3 pr-4">{emp.designation || '—'}</td>
                    <td className="py-3 pr-4">{emp.department?.name || '—'}</td>
                    <td className="py-3 flex gap-2">
                      <Link to={`/hr/employees/${emp.id}/edit`}
                        className="text-indigo-600 hover:underline text-xs">Edit</Link>
                      <button
                        onClick={() => handleDelete(emp.id, `${emp.firstName} ${emp.lastName}`)}
                        className="text-red-500 hover:underline text-xs">Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
