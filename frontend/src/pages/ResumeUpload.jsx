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
  const navigate = useNavigate();

  useEffect(() => {
    listResumes().then(setResumes).catch(() => {});
  }, []);

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) return;
    setUploading(true);
    setError('');
    try {
      const resume = await uploadResume(file);
      setResumes((prev) => [resume, ...prev]);
      setSelectedResume(resume.id);
    } catch (err) {
      setError(err.response?.data?.error || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleStartInterview = async () => {
    const session = await startSession({
      resumeId: selectedResume,
      sessionType,
      topic,
      companyName: companyName || null
    });
    navigate(`/interview/${session.id}`);
  };

  return (
    <div className="mx-auto mt-10 max-w-2xl space-y-8 px-4">
      <div className="rounded-lg border border-slate-200 bg-white p-6">
        <h2 className="mb-4 text-lg font-semibold text-slate-800">Upload your resume (PDF)</h2>
        <form onSubmit={handleUpload} className="flex items-center gap-3">
          <input
            type="file" accept="application/pdf"
            onChange={(e) => setFile(e.target.files[0])}
            className="text-sm"
          />
          <button
            disabled={uploading}
            className="rounded bg-brand-600 px-4 py-2 text-sm text-white hover:bg-brand-700 disabled:opacity-50"
          >
            {uploading ? 'Uploading…' : 'Upload'}
          </button>
        </form>
        {error && <p className="mt-2 text-sm text-red-600">{error}</p>}

        <ul className="mt-4 space-y-1 text-sm text-slate-600">
          {resumes.map((r) => (
            <li key={r.id}>
              <label className="flex items-center gap-2">
                <input
                  type="radio" name="resume" checked={selectedResume === r.id}
                  onChange={() => setSelectedResume(r.id)}
                />
                {r.fileName}
              </label>
            </li>
          ))}
        </ul>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-6">
        <h2 className="mb-4 text-lg font-semibold text-slate-800">Start an interview</h2>
        <div className="space-y-3">
          <select value={sessionType} onChange={(e) => setSessionType(e.target.value)}
                  className="w-full rounded border border-slate-300 px-3 py-2 text-sm">
            <option value="TEXT">Text-based Q&A</option>
            <option value="CODING">Coding questions</option>
            <option value="SYSTEM_DESIGN">System design</option>
          </select>
          <input
            type="text" placeholder="Topic (e.g. Java, React, DSA)" value={topic}
            onChange={(e) => setTopic(e.target.value)}
            className="w-full rounded border border-slate-300 px-3 py-2 text-sm"
          />
          <input
            type="text" placeholder="Target company (optional)" value={companyName}
            onChange={(e) => setCompanyName(e.target.value)}
            className="w-full rounded border border-slate-300 px-3 py-2 text-sm"
          />
          <button
            onClick={handleStartInterview}
            disabled={!selectedResume}
            className="w-full rounded bg-brand-600 py-2 text-sm text-white hover:bg-brand-700 disabled:opacity-50"
          >
            Start interview
          </button>
        </div>
      </div>
    </div>
  );
}
