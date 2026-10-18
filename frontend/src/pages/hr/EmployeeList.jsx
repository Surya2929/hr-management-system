import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axiosInstance from '../../api/axiosInstance';
import Pagination from '../../components/Pagination';
import useDebounce from '../../hooks/useDebounce';
import TempPasswordModal from '../../components/TempPasswordModal';

const PAGE_SIZE = 10;

export default function EmployeeList() {
  const [employees, setEmployees] = useState([]);
  const [pageInfo, setPageInfo]   = useState(null); // Spring Page: number, totalPages, totalElements
  const [loading, setLoading]     = useState(true);

  const [search, setSearch]           = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [page, setPage]               = useState(0);
  const [departments, setDepartments] = useState([]);
  const [reload, setReload]           = useState(0); // bump to refetch the current page

  const debouncedSearch = useDebounce(search, 400); // wait 400 ms after typing stops

  useEffect(() => {
    axiosInstance.get('/departments').then(r => setDepartments(r.data)).catch(console.error);
  }, []);

  useEffect(() => {
    let ignore = false; // drop the answer of an older request if a newer one was sent
    setLoading(true);
    axiosInstance.get('/employees', {
      params: { search: debouncedSearch || undefined, departmentId: departmentId || undefined, page, size: PAGE_SIZE },
    })
      .then(res => {
        if (ignore) return;
        // Last row of the last page was deleted -> step back one page
        if (res.data.content.length === 0 && page > 0) { setPage(page - 1); return; }
        setEmployees(res.data.content);
        setPageInfo(res.data);
      })
      .catch(console.error)
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, [debouncedSearch, departmentId, page, reload]);

  const [tempPw, setTempPw] = useState(null); // { name, email, password } to show HR

  // HR issues a fresh temporary password (employee lost it or forgot their password)
  const handleNewTempPassword = async (emp) => {
    const name = `${emp.firstName} ${emp.lastName}`;
    if (!confirm(`Create a new temporary password for ${name}? Their current password will stop working.`)) return;
    try {
      const res = await axiosInstance.post(`/employees/${emp.id}/reset-temp-password`);
      setTempPw({ name, email: emp.user?.email, password: res.data.temporaryPassword });
      setReload(r => r + 1); // refresh the "Pending activation" badge
    } catch (err) {
      alert(err.response?.data?.message || 'Could not create a temporary password');
    }
  };

  const handleDelete = async (id, name) => {
    if (!confirm(`Remove ${name}?

This deletes their login, attendance and leave history. It cannot be undone.`)) return;
    try {
      await axiosInstance.delete(`/employees/${id}`);
      setReload(r => r + 1);
    } catch (err) {
      alert(err.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-6">
        <h2 className="text-3xl sm:text-4xl font-light text-neutral-900">Employees</h2>
        <Link to="/hr/employees/new" className="btn-primary text-sm text-center w-full sm:w-auto">+ Add Employee</Link>
      </div>

      <div className="card">
        <div className="flex flex-col sm:flex-row gap-3 mb-4">
          <input
            type="search"
            className="input sm:flex-1"
            placeholder="Search by name or designation…"
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(0); }}
            aria-label="Search employees"
          />
          <select
            className="input sm:w-56"
            value={departmentId}
            onChange={e => { setDepartmentId(e.target.value); setPage(0); }}
            aria-label="Filter by department"
          >
            <option value="">All departments</option>
            {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
          </select>
        </div>

        {loading ? <p className="text-neutral-500">Loading…</p> : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-neutral-500 border-b">
                  <th className="pb-2 pr-4">Name</th>
                  <th className="pb-2 pr-4">Email</th>
                  <th className="pb-2 pr-4">Designation</th>
                  <th className="pb-2 pr-4">Department</th>
                  <th className="pb-2">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-100">
                {employees.length === 0 ? (
                  <tr><td colSpan={5} className="py-8 text-center text-neutral-400">No employees found</td></tr>
                ) : employees.map(emp => (
                  <tr key={emp.id} className="hover:bg-neutral-50">
                    <td className="py-3 pr-4 font-medium">
                      {emp.firstName} {emp.lastName}
                      {emp.user?.mustChangePassword && (
                        <span className="badge-pending ml-2" title="Has not activated the account yet">Pending activation</span>
                      )}
                    </td>
                    <td className="py-3 pr-4 text-neutral-500">{emp.user?.email}</td>
                    <td className="py-3 pr-4">{emp.designation || '—'}</td>
                    <td className="py-3 pr-4">{emp.department?.name || '—'}</td>
                    <td className="py-3 flex flex-wrap gap-x-3 gap-y-1">
                      <Link to={`/hr/employees/${emp.id}/edit`}
                        className="text-brand-600 hover:underline text-xs">Edit</Link>
                      <button
                        onClick={() => handleNewTempPassword(emp)}
                        className="text-neutral-700 hover:underline text-xs whitespace-nowrap">New temp password</button>
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
        <Pagination page={pageInfo} onChange={setPage} />
      </div>

      {tempPw && (
        <TempPasswordModal
          name={tempPw.name}
          email={tempPw.email}
          password={tempPw.password}
          onClose={() => setTempPw(null)}
        />
      )}
    </div>
  );
}
