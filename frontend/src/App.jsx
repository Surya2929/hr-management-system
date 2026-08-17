import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute, RoleRoute } from './components/ProtectedRoute';

// Public pages
import Login         from './pages/Login';
import JobListings   from './pages/public/JobListings';
import ApplyForm     from './pages/public/ApplyForm';

// HR pages
import HrLayout         from './pages/hr/HrLayout';
import HrDashboard      from './pages/hr/HrDashboard';
import EmployeeList     from './pages/hr/EmployeeList';
import EmployeeForm     from './pages/hr/EmployeeForm';
import DepartmentList   from './pages/hr/DepartmentList';
import AttendanceOverview from './pages/hr/AttendanceOverview';
import LeaveApproval    from './pages/hr/LeaveApproval';
import JobPostingList   from './pages/hr/JobPostingList';
import JobPostingForm   from './pages/hr/JobPostingForm';
import CandidateList    from './pages/hr/CandidateList';

// Employee pages
import EmpLayout        from './pages/employee/EmpLayout';
import EmployeeDashboard from './pages/employee/EmployeeDashboard';
import MyAttendance     from './pages/employee/MyAttendance';
import LeaveRequest     from './pages/employee/LeaveRequest';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Default redirect */}
          <Route path="/" element={<Navigate to="/login" replace />} />

          {/* Auth */}
          <Route path="/login" element={<Login />} />

          {/* Public — no login needed */}
          <Route path="/jobs"       element={<JobListings />} />
          <Route path="/jobs/apply" element={<ApplyForm />} />

          {/* Unauthorized fallback */}
          <Route path="/unauthorized" element={
            <div className="min-h-screen flex items-center justify-center">
              <div className="text-center">
                <h1 className="text-4xl font-bold text-red-500">403</h1>
                <p className="text-slate-600 mt-2">Access Denied</p>
              </div>
            </div>
          } />

          {/* HR routes — nested under HrLayout (Sidebar + outlet) */}
          <Route path="/hr" element={
            <RoleRoute role="HR"><HrLayout /></RoleRoute>
          }>
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route path="dashboard"         element={<HrDashboard />} />
            <Route path="employees"         element={<EmployeeList />} />
            <Route path="employees/new"     element={<EmployeeForm />} />
            <Route path="employees/:id/edit" element={<EmployeeForm />} />
            <Route path="departments"       element={<DepartmentList />} />
            <Route path="attendance"        element={<AttendanceOverview />} />
            <Route path="leave"             element={<LeaveApproval />} />
            <Route path="jobs"              element={<JobPostingList />} />
            <Route path="jobs/new"          element={<JobPostingForm />} />
            <Route path="jobs/:id/edit"     element={<JobPostingForm />} />
            <Route path="candidates"        element={<CandidateList />} />
          </Route>

          {/* Employee routes */}
          <Route path="/emp" element={
            <ProtectedRoute><EmpLayout /></ProtectedRoute>
          }>
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route path="dashboard"  element={<EmployeeDashboard />} />
            <Route path="attendance" element={<MyAttendance />} />
            <Route path="leave"      element={<LeaveRequest />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
