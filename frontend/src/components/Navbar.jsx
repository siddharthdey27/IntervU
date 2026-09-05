import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useState } from 'react';

const navLinks = [
  { to: '/',                  label: 'Overview',   icon: '⌂' },
  { to: '/resume',            label: 'Interviews', icon: '◉' },
  { to: '/coding-questions',  label: 'Challenges', icon: '⌘' },
  { to: '/progress',          label: 'Insights',   icon: '↗' },
];

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <nav className="sticky top-0 z-50 border-b border-white/[0.08]"
         style={{ background: 'rgba(7,11,24,0.88)', backdropFilter: 'blur(22px)', WebkitBackdropFilter: 'blur(22px)' }}>
      <div className="mx-auto flex h-[4.75rem] max-w-6xl items-center justify-between px-4 sm:px-6">

        {/* Logo */}
        <Link to="/" className="flex items-center gap-2.5 group">
          <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-violet-400 via-brand-500 to-indigo-600 shadow-glow-sm
                          transition-shadow duration-300 group-hover:shadow-glow">
            <span className="text-white font-black text-sm">↗</span>
          </div>
          <span className="text-lg font-bold text-white tracking-tight hidden sm:inline">
            Prep<span className="text-gradient">Pilot</span><span className="ml-1 text-[9px] font-bold tracking-[0.18em] text-slate-500">STUDIO</span>
          </span>
        </Link>

        {/* Desktop links */}
        {user && (
          <div className="hidden md:flex items-center gap-1 rounded-2xl border border-white/[0.06] bg-white/[0.025] p-1">
            {navLinks.map((link) => {
              const active = location.pathname === link.to;
              return (
                <Link
                  key={link.to}
                  to={link.to}
                  className={`relative px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200
                    ${active
                      ? 'text-white bg-white/[0.1] shadow-sm'
                      : 'text-slate-400 hover:text-white hover:bg-white/[0.05]'
                    }`}
                >
                  <span className="mr-1.5 text-xs">{link.icon}</span>
                  {link.label}
                </Link>
              );
            })}
          </div>
        )}

        {/* Right section */}
        <div className="flex items-center gap-3">
          {user ? (
            <>
              <div className="hidden sm:flex items-center gap-2 text-sm">
                <div className="h-8 w-8 rounded-full bg-gradient-to-br from-brand-400 to-purple-500 flex items-center justify-center text-white text-xs font-bold shadow-glow-sm">
                  {user.fullName?.charAt(0)?.toUpperCase() || '?'}
                </div>
                <span className="text-slate-300 font-medium max-w-28 truncate">{user.fullName}</span>
              </div>
              <button
                onClick={() => { logout(); navigate('/login'); }}
                className="btn-ghost !px-3 !py-1.5 text-xs"
              >
                Logout
              </button>

              {/* Mobile menu toggle */}
              <button
                className="md:hidden text-slate-400 hover:text-white transition-colors"
                onClick={() => setMenuOpen(!menuOpen)}
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  {menuOpen
                    ? <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    : <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                  }
                </svg>
              </button>
            </>
          ) : (
            <div className="flex items-center gap-2">
              <Link to="/login" className="text-sm text-slate-400 hover:text-white transition-colors px-3 py-1.5">
                Log in
              </Link>
              <Link to="/register" className="btn-primary !text-xs !px-4 !py-2">
                Sign up
              </Link>
            </div>
          )}
        </div>
      </div>

      {/* Mobile menu */}
      {user && menuOpen && (
        <div className="md:hidden border-t border-white/[0.06] animate-fade-in-down">
          <div className="px-4 py-3 space-y-1">
            {navLinks.map((link) => {
              const active = location.pathname === link.to;
              return (
                <Link
                  key={link.to}
                  to={link.to}
                  onClick={() => setMenuOpen(false)}
                  className={`block px-4 py-2.5 rounded-lg text-sm font-medium transition-colors
                    ${active ? 'text-white bg-white/[0.08]' : 'text-slate-400 hover:text-white hover:bg-white/[0.04]'}`}
                >
                  <span className="mr-2">{link.icon}</span>{link.label}
                </Link>
              );
            })}
          </div>
        </div>
      )}
    </nav>
  );
}
