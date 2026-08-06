import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Editor from "@monaco-editor/react";
import { getQuestion, runCode, submitCode } from "../api/codingApi";

const LANGUAGES = [
  { value: "python", label: "Python", monaco: "python" },
  { value: "java", label: "Java", monaco: "java" },
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

  useEffect(() => {
    getQuestion(id).then((q) => {
      setQuestion(q);
      const boilerplate = JSON.parse(q.boilerplate || "{}");
      setCode(boilerplate[language] || "");
    });
  }, [id]);

  const handleLanguageChange = (lang) => {
    setLanguage(lang);
    if (question) {
      const boilerplate = JSON.parse(question.boilerplate || "{}");
      setCode(boilerplate[lang] || "");
    }
  };

  const handleRun = async () => {
    setBusy(true);
    setOutput(null);
    try {
      const result = await runCode(id, { language, sourceCode: code, stdin });
      setOutput(result);
    } catch (err) {
      setOutput({ status: "Error", stderr: err.message });
    } finally {
      setBusy(false);
    }
  };

  const handleSubmit = async () => {
    setBusy(true);
    setSubmission(null);
    try {
      const result = await submitCode(id, { language, sourceCode: code });
      setSubmission(result);
    } catch (err) {
      setSubmission({ status: "ERROR", passedTestCount: 0, totalTestCount: 0, visibleResults: [] });
    } finally {
      setBusy(false);
    }
  };

  if (!question) return <div className="p-8 text-gray-500">Loading question...</div>;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 h-[calc(100vh-4rem)]">
      {/* Left: problem statement */}
      <div className="overflow-y-auto p-6 border-r">
        <h1 className="text-xl font-semibold mb-2">{question.title}</h1>
        <span className="text-xs font-medium px-2 py-1 rounded-full bg-gray-100 mb-4 inline-block">
          {question.difficulty}
        </span>
        <p className="whitespace-pre-wrap text-sm text-gray-700">{question.description}</p>

        {submission && (
          <div className="mt-6 border rounded-lg p-4">
            <div
              className={`font-medium ${
                submission.status === "ACCEPTED" ? "text-green-600" : "text-red-600"
              }`}
            >
              {submission.status.replace("_", " ")} — {submission.passedTestCount}/{submission.totalTestCount} tests passed
            </div>
            {submission.visibleResults?.map((r, i) => (
              <div key={i} className="mt-3 text-sm">
                <div className="font-mono text-xs text-gray-500">Input: {r.input}</div>
                <div className="font-mono text-xs">
                  Expected: {r.expectedOutput} | Got: {r.actualOutput}{" "}
                  <span className={r.passed ? "text-green-600" : "text-red-600"}>
                    {r.passed ? "✓" : "✗"}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Right: editor + console */}
      <div className="flex flex-col h-full">
        <div className="flex items-center justify-between px-4 py-2 border-b bg-gray-50">
          <select
            value={language}
            onChange={(e) => handleLanguageChange(e.target.value)}
            className="border rounded px-2 py-1 text-sm"
          >
            {LANGUAGES.map((l) => (
              <option key={l.value} value={l.value}>{l.label}</option>
            ))}
          </select>
          <div className="space-x-2">
            <button
              onClick={handleRun}
              disabled={busy}
              className="px-3 py-1.5 text-sm rounded bg-gray-200 hover:bg-gray-300 disabled:opacity-50"
            >
              Run
            </button>
            <button
              onClick={handleSubmit}
              disabled={busy}
              className="px-3 py-1.5 text-sm rounded bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50"
            >
              Submit
            </button>
          </div>
        </div>

        <div className="flex-1 min-h-[300px]">
          <Editor
            height="100%"
            language={LANGUAGES.find((l) => l.value === language)?.monaco}
            value={code}
            onChange={(v) => setCode(v ?? "")}
            theme="vs-dark"
            options={{ minimap: { enabled: false }, fontSize: 14 }}
          />
        </div>

        <div className="border-t p-3 bg-black text-green-400 font-mono text-xs h-40 overflow-y-auto">
          <div className="text-gray-400 mb-1">Custom input:</div>
          <textarea
            value={stdin}
            onChange={(e) => setStdin(e.target.value)}
            className="w-full bg-gray-900 text-green-300 p-1 mb-2 rounded text-xs"
            rows={2}
          />
          {output && (
            <div>
              <div className="text-gray-400">Status: {output.status}</div>
              {output.stdout && <pre className="whitespace-pre-wrap">{output.stdout}</pre>}
              {output.stderr && <pre className="whitespace-pre-wrap text-red-400">{output.stderr}</pre>}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
