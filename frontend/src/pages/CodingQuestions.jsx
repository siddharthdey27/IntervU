import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listQuestions } from "../api/codingApi";
import { getApiErrorMessage } from '../api/errors.js';

const difficultyConfig = {
  EASY:   { text: 'Easy',   classes: 'text-emerald-400 bg-emerald-500/10 border border-emerald-500/20' },
  MEDIUM: { text: 'Medium', classes: 'text-amber-400 bg-amber-500/10 border border-amber-500/20' },
  HARD:   { text: 'Hard',   classes: 'text-rose-400 bg-rose-500/10 border border-rose-500/20' },
};

export default function CodingQuestions() {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadQuestions = () => {
    setLoading(true);
    setError(null);
    listQuestions()
      .then(setQuestions)
      .catch((err) => setError(getApiErrorMessage(err, 'Failed to load questions')))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadQuestions();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <svg className="animate-spin h-8 w-8 text-brand-400" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          <p className="text-sm text-slate-500">Loading questions…</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="glass-card p-8 text-center max-w-md">
          <div className="text-4xl mb-3">😵</div>
          <p className="text-red-400 font-medium mb-1">Failed to load questions</p>
          <p className="text-sm text-slate-500">{error}</p>
          <button onClick={loadQuestions} className="btn-ghost mt-4 !text-xs">Try again</button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-4 sm:px-6 py-10 animate-fade-in">
      {/* Header */}
      <div className="mb-7">
        <div className="eyebrow mb-3">Practice arena</div>
        <div className="flex items-center gap-3">
        <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-emerald-500 to-cyan-500 flex items-center justify-center shadow-lg">
          <span className="text-lg">⟨/⟩</span>
        </div>
        <div>
          <h1 className="page-heading text-2xl">Coding Challenges</h1>
          <p className="text-sm text-slate-400">{questions.length} problems available</p>
        </div>
        </div>
      </div>

      {/* Question list */}
      <div className="glass-card overflow-hidden divide-y divide-white/[0.06]">
        {questions.map((q, i) => {
          const diff = difficultyConfig[q.difficulty] || difficultyConfig.EASY;
          return (
            <Link
              key={q.id}
              to={`/coding-questions/${q.id}`}
              className={`flex items-center justify-between px-5 py-4 sm:px-6 transition-all duration-200 hover:bg-brand-500/[0.06] group
                          animate-fade-in-up`}
              style={{ animationDelay: `${i * 40}ms`, animationFillMode: 'both' }}
            >
              <div className="flex items-center gap-4">
                <span className="text-xs text-slate-600 font-mono w-6 text-right">{i + 1}</span>
                <div>
                  <div className="font-medium text-slate-200 group-hover:text-white transition-colors">{q.title}</div>
                  <div className="text-xs text-slate-500 mt-0.5">{q.category}</div>
                </div>
              </div>
              <span className={`badge ${diff.classes} text-xs`}>
                {diff.text}
              </span>
            </Link>
          );
        })}

        {questions.length === 0 && (
          <div className="px-5 py-12 text-center">
            <div className="text-4xl mb-3">📝</div>
            <p className="text-slate-400 font-medium">No questions yet</p>
            <p className="text-xs text-slate-600 mt-1">Check back later or ask your admin to add coding problems.</p>
          </div>
        )}
      </div>
    </div>
  );
}
