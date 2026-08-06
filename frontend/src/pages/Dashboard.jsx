import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Dashboard() {
  const { user } = useAuth();

  return (
    <div className="mx-auto mt-10 max-w-3xl px-4">
      <h1 className="text-2xl font-bold text-slate-800">Welcome, {user?.fullName} 👋</h1>
      <p className="mt-1 text-slate-600">Ready to sharpen your interview skills?</p>

      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Link to="/resume" className="rounded-lg border border-slate-200 bg-white p-6 hover:shadow-sm">
          <h2 className="font-semibold text-slate-800">Upload Resume & Start Interview</h2>
          <p className="mt-1 text-sm text-slate-500">
            Upload your resume, pick a topic or target company, and start a RAG-powered mock interview.
          </p>
        </Link>
        <div className="rounded-lg border border-slate-200 bg-white p-6 opacity-60">
          <h2 className="font-semibold text-slate-800">Progress Dashboard</h2>
          <p className="mt-1 text-sm text-slate-500">
            Session history, scores and weak-area trends will appear here as you complete interviews.
          </p>
        </div>
      </div>
    </div>
  );
}
