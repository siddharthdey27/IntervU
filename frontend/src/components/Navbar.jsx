import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <nav className="bg-white border-b border-slate-200 px-6 py-3 flex items-center justify-between">
      <Link to="/" className="text-xl font-bold text-brand-700">PrepPilot</Link>
      <div className="flex items-center gap-4 text-sm">
        {user ? (
          <>
            <Link to="/" className="text-slate-600 hover:text-brand-600">Dashboard</Link>
            <Link to="/resume" className="text-slate-600 hover:text-brand-600">Resume</Link>
            <span className="text-slate-400">|</span>
            <span className="text-slate-700">{user.fullName}</span>
            <button
              onClick={() => { logout(); navigate('/login'); }}
              className="rounded bg-slate-100 px-3 py-1 text-slate-700 hover:bg-slate-200"
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="text-slate-600 hover:text-brand-600">Login</Link>
            <Link to="/register" className="rounded bg-brand-600 px-3 py-1 text-white hover:bg-brand-700">
              Sign up
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}
