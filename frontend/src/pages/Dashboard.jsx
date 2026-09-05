import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const cards = [
  {
    to: '/resume',
    icon: '🎤',
    title: 'Mock Interview',
    description: 'Pick a topic and dive into an AI mock interview, optionally tailored to your uploaded resume.',
    gradient: 'from-brand-500 to-purple-600',
    glowColor: 'rgba(99,102,241,0.15)',
  },
  {
    to: '/coding-questions',
    icon: '⟨/⟩',
    title: 'Coding Challenges',
    description: 'Solve algorithmic problems with an integrated code editor and instant test-case feedback.',
    gradient: 'from-emerald-500 to-cyan-500',
    glowColor: 'rgba(52,211,153,0.15)',
  },
  {
    to: '/progress',
    icon: '📊',
    title: 'Progress Dashboard',
    description: 'Track your session history, coding submissions, and performance trends over time.',
    gradient: 'from-amber-500 to-orange-500',
    glowColor: 'rgba(251,191,36,0.15)',
  },
];

export default function Dashboard() {
  const { user } = useAuth();

  return (
    <div className="mx-auto max-w-5xl px-4 sm:px-6 py-10 animate-fade-in">
      {/* Hero */}
      <div className="mb-10 grid gap-5 lg:grid-cols-[1fr_auto] lg:items-end">
        <div>
        <div className="eyebrow mb-3">Your preparation workspace</div>
        <h1 className="page-heading text-3xl sm:text-4xl mb-2">
          Welcome back, {user?.fullName?.split(' ')[0]} 👋
        </h1>
        <p className="text-slate-400 text-base">
          Ready to sharpen your interview skills? Pick a module below to get started.
        </p>
        </div>
        <div className="hidden lg:flex items-center gap-3 rounded-2xl border border-brand-400/15 bg-brand-500/[0.07] px-4 py-3">
          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-500/20 text-lg">✦</span>
          <p className="text-xs leading-relaxed text-slate-400"><span className="block font-semibold text-slate-200">Build momentum daily</span>Small practice, real progress.</p>
        </div>
      </div>

      {/* Feature cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        {cards.map((card, i) => (
          <Link
            key={card.to}
            to={card.to}
            id={`dashboard-card-${card.to.replace(/\//g, '')}`}
            className={`feature-card glass-card group relative overflow-hidden p-6 animate-fade-in-up stagger-${i + 1}`}
            style={{ animationFillMode: 'both', '--card-glow': card.glowColor }}
          >
            {/* Glow */}
            <div
              className="absolute -top-12 -right-12 w-32 h-32 rounded-full blur-[60px] opacity-0 group-hover:opacity-100 transition-opacity duration-500"
              style={{ background: card.glowColor }}
            />

            {/* Icon */}
            <div className={`inline-flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br ${card.gradient} shadow-lg mb-4
                            group-hover:scale-110 transition-transform duration-300`}>
              <span className="text-xl">{card.icon}</span>
            </div>

            <h2 className="text-lg font-semibold text-white mb-1.5">{card.title}</h2>
            <p className="text-sm text-slate-400 leading-relaxed">{card.description}</p>

            {/* Arrow */}
            <div className="mt-4 flex items-center gap-1 text-sm font-medium text-brand-400 group-hover:text-brand-300 transition-colors">
              Get started
              <svg className="w-4 h-4 transition-transform duration-200 group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </Link>
        ))}
      </div>

      {/* Quick stats row (placeholder for when we have data) */}
      <div className="mt-11">
        <div className="mb-4 flex items-center justify-between"><div><p className="text-sm font-semibold text-slate-200">Your snapshot</p><p className="text-xs text-slate-500 mt-1">Progress begins with your first session.</p></div><span className="text-xs font-medium text-brand-300">Live metrics</span></div>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {[
          { label: 'Total Sessions', value: '—', icon: '📝' },
          { label: 'Coding Solved', value: '—', icon: '✅' },
          { label: 'Streak', value: '—', icon: '🔥' },
          { label: 'Accuracy', value: '—', icon: '🎯' },
        ].map((stat) => (
          <div key={stat.label} className="stat-card text-center">
            <div className="text-xl mb-1">{stat.icon}</div>
            <div className="text-2xl font-bold text-white">{stat.value}</div>
            <div className="text-xs text-slate-500 mt-1">{stat.label}</div>
          </div>
        ))}
      </div>
      </div>
    </div>
  );
}
