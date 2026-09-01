import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute, RoleRoute } from './components/ProtectedRoute';

// Auth Page (Login & Employee Registration)
import Login from './pages/Login';

// HR Pages
import HrLayout           from './pages/hr/HrLayout';
import HrDashboard        from './pages/hr/HrDashboard';
import EmployeeList       from './pages/hr/EmployeeList';
import EmployeeForm       from './pages/hr/EmployeeForm';
import DepartmentList     from './pages/hr/DepartmentList';
import AttendanceOverview from './pages/hr/AttendanceOverview';
import LeaveApproval      from './pages/hr/LeaveApproval';
import JobPostingList     from './pages/hr/JobPostingList';
import JobPostingForm     from './pages/hr/JobPostingForm';
import CandidateList      from './pages/hr/CandidateList';

// Employee Pages
import EmpLayout         from './pages/employee/EmpLayout';
import EmployeeDashboard from './pages/employee/EmployeeDashboard';
import MyAttendance      from './pages/employee/MyAttendance';
import LeaveRequest      from './pages/employee/LeaveRequest';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Default redirect to Login */}
          <Route path="/" element={<Navigate to="/login" replace />} />

          {/* Authentication (Login + Employee Self-Registration) */}
          <Route path="/login" element={<Login />} />

          {/* Unauthorized Fallback */}
          <Route path="/unauthorized" element={
            <div className="min-h-screen flex items-center justify-center bg-slate-50">
              <div className="text-center p-8 bg-white rounded-2xl shadow-md border border-slate-200 max-w-sm">
                <h1 className="text-5xl font-bold text-red-500 mb-2">403</h1>
                <p className="text-slate-800 font-semibold text-lg">Access Denied</p>
                <p className="text-slate-500 text-sm mt-1">You do not have permission to view this section.</p>
                <a href="/login" className="btn-primary inline-block mt-5 text-xs">Back to Login</a>
              </div>
            </div>
          } />

          {/* HR Routes (Protected with Role 'HR') */}
          <Route path="/hr" element={
            <RoleRoute role="HR">
              <HrLayout />
            </RoleRoute>
          }>
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route path="dashboard"          element={<HrDashboard />} />
            <Route path="employees"          element={<EmployeeList />} />
            <Route path="employees/new"      element={<EmployeeForm />} />
            <Route path="employees/:id/edit" element={<EmployeeForm />} />
            <Route path="departments"        element={<DepartmentList />} />
            <Route path="attendance"         element={<AttendanceOverview />} />
            <Route path="leave"              element={<LeaveApproval />} />
            <Route path="jobs"               element={<JobPostingList />} />
            <Route path="jobs/new"           element={<JobPostingForm />} />
            <Route path="jobs/:id/edit"      element={<JobPostingForm />} />
            <Route path="candidates"         element={<CandidateList />} />
          </Route>

          {/* Employee Routes (Protected with Role 'EMPLOYEE') */}
          <Route path="/emp" element={
            <ProtectedRoute>
              <EmpLayout />
            </ProtectedRoute>
          }>
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route path="dashboard"  element={<EmployeeDashboard />} />
            <Route path="attendance" element={<MyAttendance />} />
            <Route path="leave"      element={<LeaveRequest />} />
          </Route>

          {/* Catch-all Route */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
