import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getTranscript, sendMessage } from '../api/interviews.js';

export default function InterviewChat() {
  const { sessionId } = useParams();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    getTranscript(sessionId).then(setMessages).catch(() => {});
  }, [sessionId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;
    const userText = input;
    setInput('');
    setMessages((prev) => [...prev, { sender: 'USER', content: userText }]);
    setSending(true);
    try {
      const res = await sendMessage(sessionId, userText);
      setMessages((prev) => [...prev, { sender: 'AI', content: res.aiMessage }]);
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="mx-auto flex h-[calc(100vh-64px)] max-w-2xl flex-col px-4 py-4">
      <div className="flex-1 space-y-3 overflow-y-auto rounded-lg border border-slate-200 bg-white p-4">
        {messages.map((m, i) => (
          <div key={i} className={`flex ${m.sender === 'USER' ? 'justify-end' : 'justify-start'}`}>
            <div
              className={`max-w-[80%] rounded-lg px-4 py-2 text-sm ${
                m.sender === 'USER' ? 'bg-brand-600 text-white' : 'bg-slate-100 text-slate-800'
              }`}
            >
              {m.content}
            </div>
          </div>
        ))}
        {sending && <p className="text-xs text-slate-400">AI is thinking…</p>}
        <div ref={bottomRef} />
      </div>
      <form onSubmit={handleSend} className="mt-3 flex gap-2">
        <input
          type="text" value={input} onChange={(e) => setInput(e.target.value)}
          placeholder="Type your answer…"
          className="flex-1 rounded border border-slate-300 px-3 py-2 text-sm"
        />
        <button
          disabled={sending}
          className="rounded bg-brand-600 px-4 py-2 text-sm text-white hover:bg-brand-700 disabled:opacity-50"
        >
          Send
        </button>
      </form>
    </div>
  );
}
