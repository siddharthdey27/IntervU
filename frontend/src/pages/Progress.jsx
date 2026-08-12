import { useEffect, useState } from 'react';
import { getProgress } from '../api/progressApi.js';
import { getApiErrorMessage } from '../api/errors.js';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';

const CHART_COLORS = ['#6366f1', '#8b5cf6', '#a78bfa', '#c084fc'];
const STATUS_COLORS = { ACCEPTED: '#34d399', WRONG_ANSWER: '#fb7185', ERROR: '#fbbf24' };

export default function Progress() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getProgress()
      .then(setData)
      .catch((err) => setError(getApiErrorMessage(err, 'Failed to load progress')))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <svg className="animate-spin h-8 w-8 text-brand-400" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          <p className="text-sm text-slate-500">Loading progress…</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="glass-card p-8 text-center max-w-md">
          <div className="text-4xl mb-3">😵</div>
          <p className="text-red-400 font-medium mb-1">Failed to load progress</p>
          <p className="text-sm text-slate-500">{error}</p>
        </div>
      </div>
    );
  }

  // Prepare chart data
  const sessionBreakdown = [];
  const typeMap = {};
  data.recentSessions?.forEach((s) => {
    typeMap[s.sessionType] = (typeMap[s.sessionType] || 0) + 1;
  });
  Object.entries(typeMap).forEach(([name, value]) => sessionBreakdown.push({ name, value }));

  const submissionChart = [];
  const statusMap = {};
  data.recentSubmissions?.forEach((s) => {
    statusMap[s.status] = (statusMap[s.status] || 0) + 1;
  });
  Object.entries(statusMap).forEach(([name, value]) => submissionChart.push({ name, value }));

  const CustomTooltip = ({ active, payload, label }) => {
    if (!active || !payload?.length) return null;
    return (
      <div className="glass-card-light px-3 py-2 text-xs">
        <p className="text-slate-300 font-medium">{label || payload[0]?.name}</p>
        <p className="text-white">{payload[0]?.value}</p>
      </div>
    );
  };

  return (
    <div className="mx-auto max-w-5xl px-4 sm:px-6 py-10 animate-fade-in">
      <div className="mb-8">
        <h1 className="page-heading text-2xl sm:text-3xl mb-1">Progress Dashboard</h1>
        <p className="text-slate-400 text-sm">Your interview preparation stats at a glance.</p>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
        {[
          { label: 'Total Sessions', value: data.totalSessions, icon: '📝', accent: 'from-brand-500 to-purple-500' },
          { label: 'Completed', value: data.completedSessions, icon: '✅', accent: 'from-emerald-500 to-cyan-500' },
          { label: 'Code Submissions', value: data.totalCodingSubmissions, icon: '💻', accent: 'from-amber-500 to-orange-500' },
          { label: 'Accepted', value: data.acceptedSubmissions, icon: '🎯', accent: 'from-rose-500 to-pink-500' },
        ].map((stat, i) => (
          <div key={stat.label} className={`stat-card animate-fade-in-up stagger-${i + 1}`} style={{ animationFillMode: 'both' }}>
            <div className={`inline-flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br ${stat.accent} shadow-lg mb-3`}>
              <span className="text-base">{stat.icon}</span>
            </div>
            <div className="text-3xl font-bold text-white">{stat.value}</div>
            <div className="text-xs text-slate-500 mt-1">{stat.label}</div>
          </div>
        ))}
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        {/* Session type breakdown */}
        <div className="glass-card p-6">
          <h2 className="text-sm font-semibold text-white mb-4">Session Types</h2>
          {sessionBreakdown.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie
                  data={sessionBreakdown}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={4}
                  dataKey="value"
                  stroke="none"
                >
                  {sessionBreakdown.map((_, i) => (
                    <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend
                  iconType="circle"
                  iconSize={8}
                  formatter={(val) => <span className="text-xs text-slate-400">{val}</span>}
                />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-[220px] text-slate-600 text-sm">No session data yet</div>
          )}
        </div>

        {/* Submission status breakdown */}
        <div className="glass-card p-6">
          <h2 className="text-sm font-semibold text-white mb-4">Submission Results</h2>
          {submissionChart.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={submissionChart} barSize={32}>
                <XAxis
                  dataKey="name"
                  tick={{ fill: '#94a3b8', fontSize: 11 }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fill: '#94a3b8', fontSize: 11 }}
                  axisLine={false}
                  tickLine={false}
                  allowDecimals={false}
                />
                <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.03)' }} />
                <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                  {submissionChart.map((entry, i) => (
                    <Cell key={i} fill={STATUS_COLORS[entry.name] || CHART_COLORS[i % CHART_COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-[220px] text-slate-600 text-sm">No submissions yet</div>
          )}
        </div>
      </div>

      {/* Recent sessions table */}
      <div className="glass-card overflow-hidden mb-6">
        <div className="px-6 py-4 border-b border-white/[0.06]">
          <h2 className="text-sm font-semibold text-white">Recent Sessions</h2>
        </div>
        {data.recentSessions?.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-xs text-slate-500 uppercase tracking-wider">
                  <th className="px-6 py-3 font-medium">Type</th>
                  <th className="px-6 py-3 font-medium">Topic</th>
                  <th className="px-6 py-3 font-medium">Company</th>
                  <th className="px-6 py-3 font-medium">Status</th>
                  <th className="px-6 py-3 font-medium">Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/[0.04]">
                {data.recentSessions.map((s) => (
                  <tr key={s.id} className="hover:bg-white/[0.02] transition-colors">
                    <td className="px-6 py-3 text-slate-300">{s.sessionType}</td>
                    <td className="px-6 py-3 text-slate-300">{s.topic || '—'}</td>
                    <td className="px-6 py-3 text-slate-400">{s.companyName || '—'}</td>
                    <td className="px-6 py-3">
                      <span className={`badge text-xs ${
                        s.status === 'COMPLETED'
                          ? 'text-emerald-400 bg-emerald-500/10'
                          : 'text-amber-400 bg-amber-500/10'
                      }`}>{s.status}</span>
                    </td>
                    <td className="px-6 py-3 text-slate-500 text-xs">
                      {new Date(s.startedAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="px-6 py-10 text-center text-slate-600 text-sm">No sessions yet. Start an interview to see your history here.</div>
        )}
      </div>

      {/* Recent submissions table */}
      <div className="glass-card overflow-hidden">
        <div className="px-6 py-4 border-b border-white/[0.06]">
          <h2 className="text-sm font-semibold text-white">Recent Coding Submissions</h2>
        </div>
        {data.recentSubmissions?.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-xs text-slate-500 uppercase tracking-wider">
                  <th className="px-6 py-3 font-medium">Problem</th>
                  <th className="px-6 py-3 font-medium">Language</th>
                  <th className="px-6 py-3 font-medium">Status</th>
                  <th className="px-6 py-3 font-medium">Tests</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/[0.04]">
                {data.recentSubmissions.map((sub) => (
                  <tr key={sub.id} className="hover:bg-white/[0.02] transition-colors">
                    <td className="px-6 py-3 text-slate-300 font-medium">{sub.questionTitle}</td>
                    <td className="px-6 py-3 text-slate-400 capitalize">{sub.language}</td>
                    <td className="px-6 py-3">
                      <span className={`badge text-xs ${
                        sub.status === 'ACCEPTED'
                          ? 'text-emerald-400 bg-emerald-500/10'
                          : 'text-rose-400 bg-rose-500/10'
                      }`}>{sub.status}</span>
                    </td>
                    <td className="px-6 py-3 text-slate-400">{sub.passedTestCount}/{sub.totalTestCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="px-6 py-10 text-center text-slate-600 text-sm">No submissions yet. Solve a coding challenge to see your results here.</div>
        )}
      </div>
    </div>
  );
}
