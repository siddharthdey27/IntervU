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
    <div className="mx-auto max-w-6xl px-4 sm:px-6 py-8 sm:py-10 animate-fade-in">
      {/* Hero */}
      <div className="dashboard-hero mb-9 px-6 py-8 sm:px-9 sm:py-10">
        <div className="relative z-10 max-w-2xl">
          <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-violet-300/20 bg-violet-300/10 px-3 py-1 text-[11px] font-bold uppercase tracking-[.14em] text-violet-200"><span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />Your learning studio</div>
          <h1 className="text-3xl font-bold tracking-tight text-white sm:text-5xl">Hi, {user?.fullName?.split(' ')[0]}. <span className="text-violet-300">Ready to grow?</span></h1>
          <p className="mt-4 max-w-xl text-sm leading-6 text-slate-300 sm:text-base">Practice with intent. Simulate the interview, solve real problems, and turn every session into measurable progress.</p>
          <div className="mt-7 flex flex-wrap gap-3"><Link to="/resume" className="btn-primary !px-5">Start a mock interview <span className="ml-2 text-base">→</span></Link><Link to="/coding-questions" className="btn-ghost !border-white/15 !bg-white/[0.08]">Explore challenges</Link></div>
        </div>
      </div>

      {/* Feature cards */}
      <div className="mb-4 flex items-end justify-between"><div><p className="eyebrow">Choose your focus</p><h2 className="mt-2 text-xl font-semibold text-white">Make today count</h2></div><span className="hidden text-xs text-slate-500 sm:block">Your tools, in one place</span></div>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {cards.map((card, i) => (
          <Link
            key={card.to}
            to={card.to}
            id={`dashboard-card-${card.to.replace(/\//g, '')}`}
            className={`action-tile group p-6 animate-fade-in-up stagger-${i + 1}`}
            style={{ animationFillMode: 'both', '--tile-color': i === 0 ? '#a78bfa' : i === 1 ? '#22d3ee' : '#fbbf24' }}
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

            <div className="mb-2 flex items-center justify-between"><h2 className="text-lg font-semibold text-white">{card.title}</h2><span className="text-slate-500 transition-transform duration-200 group-hover:translate-x-1">↗</span></div>
            <p className="text-sm text-slate-400 leading-relaxed">{card.description}</p>

            {/* Arrow */}
            <div className="mt-5 text-xs font-bold uppercase tracking-[.12em] text-violet-300 group-hover:text-violet-200">Open workspace</div>
          </Link>
        ))}
      </div>

      {/* Quick stats row (placeholder for when we have data) */}
      <div className="mt-10">
        <div className="mb-4 flex items-center justify-between"><div><p className="text-sm font-semibold text-slate-200">Your snapshot</p><p className="text-xs text-slate-500 mt-1">Progress begins with your first session.</p></div><span className="text-xs font-medium text-violet-300">Live metrics</span></div>
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
