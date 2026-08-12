import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function ProtectedRoute({ children }) {
  const { user, isAuthLoading } = useAuth();
  if (isAuthLoading) return <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center text-sm text-slate-500">Checking session…</div>;
  if (!user) return <Navigate to="/login" replace />;
  return children;
}
