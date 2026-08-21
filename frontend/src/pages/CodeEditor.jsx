import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import Editor from "@monaco-editor/react";
import { getQuestion, runCode, submitCode } from "../api/codingApi";
import { getApiErrorMessage } from '../api/errors.js';

const LANGUAGES = [
  { value: "python",     label: "Python",     monaco: "python" },
  { value: "java",       label: "Java",       monaco: "java" },
  { value: "javascript", label: "JavaScript", monaco: "javascript" },
];

export default function CodeEditor() {
  const { id } = useParams();
  const [question, setQuestion] = useState(null);
  const [language, setLanguage] = useState("python");
  const [code, setCode] = useState("");
  const [stdin, setStdin] = useState("");
  const [output, setOutput] = useState(null);
  const [submission, setSubmission] = useState(null);
  const [busy, setBusy] = useState(false);
  const [activeTab, setActiveTab] = useState("output"); // output | submission
  const [loadError, setLoadError] = useState("");

  const boilerplateFor = (q, lang) => {
    try {
      const boilerplate = JSON.parse(q.boilerplate || "{}");
      return boilerplate[lang] || "";
    } catch {
      return "";
    }
  };

  useEffect(() => {
    setQuestion(null);
    setLoadError("");
    getQuestion(id)
      .then((q) => {
        setQuestion(q);
        setCode(boilerplateFor(q, language));
      })
      .catch((err) => setLoadError(getApiErrorMessage(err, "Failed to load this coding question")));
  }, [id]);

  const handleLanguageChange = (lang) => {
    setLanguage(lang);
    if (question) {
      setCode(boilerplateFor(question, lang));
    }
  };

  const handleRun = async () => {
    setBusy(true);
    setOutput(null);
    setActiveTab("output");
    try {
      const result = await runCode(id, { language, sourceCode: code, stdin });
      setOutput(result);
    } catch (err) {
      setOutput({ status: "Error", stderr: getApiErrorMessage(err, 'Code execution failed') });
    } finally {
      setBusy(false);
    }
  };

  const handleSubmit = async () => {
    setBusy(true);
    setSubmission(null);
    setActiveTab("submission");
    try {
      const result = await submitCode(id, { language, sourceCode: code });
      setSubmission(result);
    } catch (err) {
      setSubmission({ status: "ERROR", errorMessage: getApiErrorMessage(err, 'Code submission failed'), passedTestCount: 0, totalTestCount: 0, visibleResults: [] });
    } finally {
      setBusy(false);
    }
  };

  const difficultyConfig = {
    EASY:   { classes: 'text-emerald-400 bg-emerald-500/10 border border-emerald-500/20' },
    MEDIUM: { classes: 'text-amber-400 bg-amber-500/10 border border-amber-500/20' },
    HARD:   { classes: 'text-rose-400 bg-rose-500/10 border border-rose-500/20' },
  };

  if (loadError) {
    return (
      <div className="flex items-center justify-center h-[calc(100vh-4rem)] px-4">
        <div className="glass-card max-w-md p-8 text-center">
          <div className="text-4xl mb-3">😵</div>
          <p className="text-red-400 font-medium">Unable to open the challenge</p>
          <p className="mt-1 text-sm text-slate-500">{loadError}</p>
          <Link to="/coding-questions" className="btn-ghost mt-5 inline-flex !text-xs">Back to challenges</Link>
        </div>
      </div>
    );
  }

  if (!question) {
    return (
      <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
        <svg className="animate-spin h-8 w-8 text-brand-400" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
      </div>
    );
  }

  const diff = difficultyConfig[question.difficulty] || difficultyConfig.EASY;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 h-[calc(100vh-4rem)] animate-fade-in">
      {/* Left: Problem statement */}
      <div className="overflow-y-auto p-6 border-r border-white/[0.06]"
           style={{ background: 'rgba(15,23,42,0.5)' }}>
        {/* Back link */}
        <Link to="/coding-questions" className="inline-flex items-center gap-1 text-xs text-slate-500 hover:text-slate-300 transition-colors mb-4">
          <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          All problems
        </Link>

        <h1 className="text-xl font-bold text-white mb-2">{question.title}</h1>
        <span className={`badge ${diff.classes} text-xs mb-4 inline-block`}>
          {question.difficulty}
        </span>
        <div className="prose prose-invert prose-sm max-w-none">
          <p className="whitespace-pre-wrap text-sm text-slate-300 leading-relaxed">{question.description}</p>
        </div>

        {/* Submission results */}
        {submission && (
          <div className="mt-6 glass-card-light p-4 animate-scale-in">
            <div className={`font-semibold text-sm flex items-center gap-2
              ${submission.status === "ACCEPTED" ? "text-emerald-400" : "text-rose-400"}`}>
              <span>{submission.status === "ACCEPTED" ? "✅" : "❌"}</span>
              {submission.status.replace("_", " ")} — {submission.passedTestCount}/{submission.totalTestCount} tests
            </div>
            {submission.visibleResults?.map((r, i) => (
              <div key={i} className="mt-3 rounded-lg bg-black/20 p-3 text-xs font-mono">
                <div className="text-slate-500">Input: <span className="text-slate-300">{r.input}</span></div>
                <div className="mt-1">
                  Expected: <span className="text-slate-300">{r.expectedOutput}</span>
                  {' | '}Got: <span className="text-slate-300">{r.actualOutput}</span>
                  {' '}
                  <span className={r.passed ? "text-emerald-400" : "text-rose-400"}>
                    {r.passed ? "✓" : "✗"}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Right: Editor + console */}
      <div className="flex flex-col h-full">
        {/* Toolbar */}
        <div className="flex items-center justify-between px-4 py-2.5 border-b border-white/[0.06]"
             style={{ background: 'rgba(15,23,42,0.8)' }}>
          <div className="flex items-center gap-3">
            {LANGUAGES.map((l) => (
              <button
                key={l.value}
                onClick={() => handleLanguageChange(l.value)}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all duration-200
                  ${language === l.value
                    ? 'bg-brand-500/20 text-brand-300 border border-brand-500/30'
                    : 'text-slate-500 hover:text-slate-300 hover:bg-white/[0.04]'
                  }`}
              >
                {l.label}
              </button>
            ))}
          </div>
          <div className="flex items-center gap-2">
            <button
              id="code-run-btn"
              onClick={handleRun}
              disabled={busy}
              className="btn-ghost !text-xs !px-3 !py-1.5"
            >
              {busy && activeTab === 'output' ? (
                <span className="flex items-center gap-1">
                  <svg className="animate-spin h-3 w-3" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                  Running…
                </span>
              ) : '▶ Run'}
            </button>
            <button
              id="code-submit-btn"
              onClick={handleSubmit}
              disabled={busy}
              className="btn-primary !text-xs !px-4 !py-1.5"
            >
              {busy && activeTab === 'submission' ? (
                <span className="flex items-center gap-1">
                  <svg className="animate-spin h-3 w-3" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                  Submitting…
                </span>
              ) : '🚀 Submit'}
            </button>
          </div>
        </div>

        {/* Monaco Editor */}
        <div className="flex-1 min-h-[300px]">
          <Editor
            height="100%"
            language={LANGUAGES.find((l) => l.value === language)?.monaco}
            value={code}
            onChange={(v) => setCode(v ?? "")}
            theme="vs-dark"
            options={{
              minimap: { enabled: false },
              fontSize: 14,
              fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
              padding: { top: 16 },
              scrollBeyondLastLine: false,
              smoothScrolling: true,
              cursorSmoothCaretAnimation: 'on',
              renderLineHighlight: 'gutter',
              lineNumbers: 'on',
            }}
          />
        </div>

        {/* Console panel */}
        <div className="border-t border-white/[0.06] bg-black/40 font-mono text-xs h-44 flex flex-col">
          {/* Tabs */}
          <div className="flex items-center gap-1 px-3 pt-2">
            <button
              onClick={() => setActiveTab('output')}
              className={`px-3 py-1 rounded-md text-xs transition-colors
                ${activeTab === 'output' ? 'bg-white/[0.08] text-white' : 'text-slate-500 hover:text-slate-300'}`}
            >
              Output
            </button>
            <button
              onClick={() => setActiveTab('submission')}
              className={`px-3 py-1 rounded-md text-xs transition-colors
                ${activeTab === 'submission' ? 'bg-white/[0.08] text-white' : 'text-slate-500 hover:text-slate-300'}`}
            >
              Custom Input
            </button>
          </div>

          <div className="flex-1 overflow-y-auto p-3">
            {activeTab === 'submission' && (
              <textarea
                value={stdin}
                onChange={(e) => setStdin(e.target.value)}
                className="w-full h-full bg-transparent text-emerald-300 resize-none outline-none placeholder-slate-600"
                placeholder="Enter custom input here…"
              />
            )}
            {activeTab === 'output' && output && (
              <div>
                <div className="text-slate-500 mb-1">Status: <span className="text-slate-300">{output.status}</span></div>
                {output.stdout && <pre className="whitespace-pre-wrap text-emerald-400">{output.stdout}</pre>}
                {output.stderr && <pre className="whitespace-pre-wrap text-rose-400">{output.stderr}</pre>}
              </div>
            )}
            {activeTab === 'output' && !output && (
              <div className="text-slate-600">Run your code to see output here.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
