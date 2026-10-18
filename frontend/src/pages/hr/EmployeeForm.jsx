import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axiosInstance from '../../api/axiosInstance';
import TempPasswordModal from '../../components/TempPasswordModal';

// Defined outside the form so inputs keep focus while typing
const Field = ({ label, id, type = 'text', value, onChange, required }) => (
  <div>
    <label className="label" htmlFor={id}>{label}{required && ' *'}</label>
    <input id={id} type={type} className="input" value={value} onChange={onChange} required={required} />
  </div>
);

export default function EmployeeForm() {
  const { id }   = useParams();
  const isEdit   = Boolean(id);
  const navigate = useNavigate();

  const [departments, setDepartments] = useState([]);
  const [form, setForm] = useState({
    email: '', firstName: '', lastName: '',
    phone: '', designation: '', salary: '', dateJoined: '', departmentId: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');
  const [created, setCreated] = useState(null); // { name, email, password } after HR adds someone

  useEffect(() => {
    axiosInstance.get('/departments').then(r => setDepartments(r.data));
    if (isEdit) {
      axiosInstance.get(`/employees/${id}`).then(res => {
        const e = res.data;
        setForm({
          email:        e.user?.email || '',
          firstName:    e.firstName   || '',
          lastName:     e.lastName    || '',
          phone:        e.phone       || '',
          designation:  e.designation || '',
          salary:       e.salary      || '',
          dateJoined:   e.dateJoined  || '',
          departmentId: e.department?.id || '',
        });
      });
    }
  }, [id]);

  const set = (field) => (e) => setForm(f => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      if (isEdit) {
        await axiosInstance.put(`/employees/${id}`, {
          firstName:    form.firstName,
          lastName:     form.lastName,
          phone:        form.phone,
          designation:  form.designation,
          salary:       form.salary || undefined,
          dateJoined:   form.dateJoined || undefined,
          departmentId: form.departmentId || undefined,
        });
      } else {
        // HR creates the account; the server returns a one-time temporary password
        const res = await axiosInstance.post('/employees', {
          email:        form.email,
          firstName:    form.firstName,
          lastName:     form.lastName,
          phone:        form.phone,
          designation:  form.designation,
          departmentId: form.departmentId || undefined,
        });
        setCreated({
          name: `${form.firstName} ${form.lastName}`,
          email: form.email,
          password: res.data.temporaryPassword,
        });
        return; // stay here until HR has copied the password
      }
      navigate('/hr/employees');
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2 className="text-3xl sm:text-4xl font-light text-neutral-900 mb-6">
        {isEdit ? 'Edit Employee' : 'Add New Employee'}
      </h2>

      <div className="card max-w-2xl">
        <form onSubmit={handleSubmit} className="space-y-4">
          {!isEdit && (
            <>
              <Field label="Work Email" id="email" type="email" value={form.email} onChange={set('email')} required />
              <p className="text-xs text-neutral-400 -mt-2">
                A temporary password is generated automatically. The employee sets their own password when activating.
              </p>
            </>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Field label="First Name" id="firstName" value={form.firstName} onChange={set('firstName')} required />
            <Field label="Last Name"  id="lastName"  value={form.lastName}  onChange={set('lastName')}  required />
          </div>

          <Field label="Phone"       id="phone"       value={form.phone}       onChange={set('phone')} />
          <Field label="Designation" id="designation" value={form.designation} onChange={set('designation')} />

          {isEdit && (
            <>
              <Field label="Salary"      id="salary"     type="number" value={form.salary}     onChange={set('salary')} />
              <Field label="Date Joined" id="dateJoined" type="date"   value={form.dateJoined} onChange={set('dateJoined')} />
            </>
          )}

          <div>
            <label className="label" htmlFor="departmentId">Department</label>
            <select id="departmentId" className="input" value={form.departmentId} onChange={set('departmentId')}>
              <option value="">— Select Department —</option>
              {departments.map(d => (
                <option key={d.id} value={d.id}>{d.name}</option>
              ))}
            </select>
          </div>

          {error && <p className="text-sm text-red-600 bg-red-50 px-4 py-2.5 rounded-2xl" role="alert">{error}</p>}

          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Saving…' : isEdit ? 'Update Employee' : 'Add Employee'}
            </button>
            <button type="button" onClick={() => navigate('/hr/employees')} className="btn-secondary">
              Cancel
            </button>
          </div>
        </form>
      </div>

      {created && (
        <TempPasswordModal
          name={created.name}
          email={created.email}
          password={created.password}
          onClose={() => navigate('/hr/employees')}
        />
      )}
    </div>
  );
}
