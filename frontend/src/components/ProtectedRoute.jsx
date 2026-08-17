import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Redirect to /login if not authenticated
export function ProtectedRoute({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

// Redirect if role doesn't match (e.g. EMPLOYEE trying to access HR page)
export function RoleRoute({ children, role }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== role) return <Navigate to="/unauthorized" replace />;
  return children;
}
