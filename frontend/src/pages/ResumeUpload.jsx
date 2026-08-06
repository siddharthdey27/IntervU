import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { uploadResume, listResumes } from '../api/resumes.js';
import { startSession } from '../api/interviews.js';

export default function ResumeUpload() {
  const [file, setFile] = useState(null);
  const [resumes, setResumes] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const [selectedResume, setSelectedResume] = useState(null);
  const [sessionType, setSessionType] = useState('TEXT');
  const [topic, setTopic] = useState('Java');
  const [companyName, setCompanyName] = useState('');
  const [starting, setStarting] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    listResumes().then((data) => {
      setResumes(data);
      if (data.length > 0) setSelectedResume(data[0].id);
    }).catch(() => {});
  }, []);

  const handleUpload = async (e) => {
    e?.preventDefault();
    if (!file) return;
    setUploading(true);
    setError('');
    try {
      const resume = await uploadResume(file);
      setResumes((prev) => [resume, ...prev]);
      setSelectedResume(resume.id);
      setFile(null);
    } catch (err) {
      setError(err.response?.data?.error || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleStartInterview = async () => {
    setStarting(true);
    try {
      const session = await startSession({
        resumeId: selectedResume,
        sessionType,
        topic,
        companyName: companyName || null
      });
      navigate(`/interview/${session.id}`);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to start session');
      setStarting(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragActive(false);
    const dropped = e.dataTransfer.files?.[0];
    if (dropped?.type === 'application/pdf') {
      setFile(dropped);
    }
  };

  return (
    <div className="mx-auto max-w-3xl px-4 sm:px-6 py-10 space-y-6 animate-fade-in">
      <div>
        <h1 className="page-heading text-2xl sm:text-3xl mb-1">Interview Setup</h1>
        <p className="text-slate-400 text-sm">Upload your resume and configure your mock interview session.</p>
      </div>

      {/* Upload card */}
      <div className="glass-card p-6">
        <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
          <span className="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-brand-500/20 text-brand-400 text-sm">📄</span>
          Upload Resume
        </h2>

        <form onSubmit={handleUpload}>
          <div
            className={`relative rounded-xl border-2 border-dashed p-8 text-center transition-all duration-200 cursor-pointer
              ${dragActive
                ? 'border-brand-400 bg-brand-500/5'
                : 'border-white/[0.08] hover:border-white/[0.15] hover:bg-white/[0.02]'
              }`}
            onDragEnter={(e) => { e.preventDefault(); setDragActive(true); }}
            onDragLeave={() => setDragActive(false)}
            onDragOver={(e) => e.preventDefault()}
            onDrop={handleDrop}
            onClick={() => document.getElementById('file-input').click()}
          >
            <input
              id="file-input"
              type="file"
              accept="application/pdf"
              className="hidden"
              onChange={(e) => setFile(e.target.files[0])}
            />
            <div className="text-3xl mb-2">{file ? '✅' : '☁️'}</div>
            {file ? (
              <p className="text-sm text-white font-medium">{file.name}</p>
            ) : (
              <>
                <p className="text-sm text-slate-300">
                  <span className="text-brand-400 font-medium">Click to browse</span> or drag & drop
                </p>
                <p className="text-xs text-slate-500 mt-1">PDF files only</p>
              </>
            )}
          </div>

          {file && (
            <button
              type="submit"
              disabled={uploading}
              className="btn-primary mt-4 w-full !py-3"
            >
              {uploading ? (
                <span className="flex items-center gap-2">
                  <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                  Uploading…
                </span>
              ) : 'Upload resume'}
            </button>
          )}
        </form>

        {error && (
          <div className="mt-3 flex items-center gap-2 rounded-lg bg-red-500/10 border border-red-500/20 px-4 py-2.5 text-sm text-red-400 animate-scale-in">
            <span>⚠</span> {error}
          </div>
        )}

        {/* Resume list */}
        {resumes.length > 0 && (
          <div className="mt-5 space-y-2">
            <p className="text-xs font-medium text-slate-400 uppercase tracking-wider">Your resumes</p>
            {resumes.map((r) => (
              <label
                key={r.id}
                className={`flex items-center gap-3 rounded-lg px-4 py-3 cursor-pointer transition-all duration-200
                  ${selectedResume === r.id
                    ? 'bg-brand-500/10 border border-brand-500/30'
                    : 'bg-white/[0.03] border border-transparent hover:bg-white/[0.05]'
                  }`}
              >
                <input
                  type="radio"
                  name="resume"
                  checked={selectedResume === r.id}
                  onChange={() => setSelectedResume(r.id)}
                  className="accent-brand-500"
                />
                <span className="text-sm text-slate-300">{r.fileName}</span>
              </label>
            ))}
          </div>
        )}
      </div>

      {/* Session config card */}
      <div className="glass-card p-6">
        <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
          <span className="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-purple-500/20 text-purple-400 text-sm">⚙️</span>
          Configure Session
        </h2>

        <div className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1.5">Interview type</label>
            <select
              value={sessionType}
              onChange={(e) => setSessionType(e.target.value)}
              className="select-field"
            >
              <option value="TEXT">Text-based Q&A</option>
              <option value="CODING">Coding questions</option>
              <option value="SYSTEM_DESIGN">System design</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1.5">Topic</label>
            <input
              type="text"
              placeholder="e.g. Java, React, DSA"
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              className="input-field"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1.5">Target company <span className="text-slate-600">(optional)</span></label>
            <input
              type="text"
              placeholder="e.g. Google, Amazon"
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
              className="input-field"
            />
          </div>

          <button
            id="start-interview-btn"
            onClick={handleStartInterview}
            disabled={!selectedResume || starting}
            className="btn-primary w-full !py-3"
          >
            {starting ? (
              <span className="flex items-center gap-2">
                <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                Starting…
              </span>
            ) : '🚀 Start interview'}
          </button>
        </div>
      </div>
    </div>
  );
}
