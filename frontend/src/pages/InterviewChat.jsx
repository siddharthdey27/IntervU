import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getTranscript, sendMessage } from '../api/interviews.js';
import { getApiErrorMessage } from '../api/errors.js';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function InterviewChat() {
  const { sessionId } = useParams();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [listening, setListening] = useState(false);
  const [voiceMode, setVoiceMode] = useState(false);
  const [voiceSupported, setVoiceSupported] = useState(false);
  const [error, setError] = useState('');
  const bottomRef = useRef(null);
  const recognitionRef = useRef(null);

  useEffect(() => {
    getTranscript(sessionId).then(setMessages).catch((err) => setError(getApiErrorMessage(err, 'Failed to load transcript')));
  }, [sessionId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    setVoiceSupported(Boolean(SpeechRecognition && window.speechSynthesis));

    return () => {
      recognitionRef.current?.stop();
      window.speechSynthesis?.cancel();
    };
  }, []);

  const toggleListening = () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) return;

    if (listening) {
      recognitionRef.current?.stop();
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.lang = 'en-US';
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.onstart = () => setListening(true);
    recognition.onresult = (event) => {
      const transcript = Array.from(event.results)
        .map((result) => result[0].transcript)
        .join('');
      setInput(transcript);
    };
    recognition.onerror = () => setListening(false);
    recognition.onend = () => setListening(false);
    recognitionRef.current = recognition;
    recognition.start();
  };

  const speak = (text) => {
    if (!voiceMode || !window.speechSynthesis || !text) return;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(new SpeechSynthesisUtterance(text));
  };

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
      speak(res.aiMessage);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to send message'));
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="mx-auto flex h-[calc(100vh-4.5rem)] max-w-3xl flex-col px-4 py-5 animate-fade-in">
      {/* Header */}
      <div className="mb-4 flex items-center gap-3 rounded-2xl border border-white/[0.07] bg-slate-900/30 px-3 py-2.5">
        <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-brand-500 to-purple-600 flex items-center justify-center shadow-glow-sm">
          <span className="text-base">🎙️</span>
        </div>
        <div>
          <h1 className="text-sm font-semibold text-white">Mock Interview</h1>
          <p className="text-xs text-slate-500">Session {sessionId?.slice(0, 8)}…</p>
        </div>
      </div>
      <ErrorBanner message={error} className="mb-3" />

      {/* Messages */}
      <div className="flex-1 overflow-y-auto rounded-2xl glass-card p-4 sm:p-5 space-y-4">
        {messages.length === 0 && !sending && (
          <div className="flex flex-col items-center justify-center h-full text-center text-slate-500">
            <div className="text-4xl mb-3">💬</div>
            <p className="text-sm">Your interview conversation will appear here.</p>
            <p className="text-xs text-slate-600 mt-1">The AI interviewer will ask you questions based on your resume.</p>
          </div>
        )}

        {messages.map((m, i) => (
          <div key={i} className={`flex ${m.sender === 'USER' ? 'justify-end' : 'justify-start'} animate-fade-in`}>
            {m.sender !== 'USER' && (
              <div className="mr-2 mt-1 flex-shrink-0 h-7 w-7 rounded-full bg-gradient-to-br from-brand-500 to-purple-600 flex items-center justify-center text-xs text-white font-bold shadow-glow-sm">
                AI
              </div>
            )}
            <div
              className={`max-w-[78%] rounded-2xl px-4 py-3 text-sm leading-relaxed
                ${m.sender === 'USER'
                  ? 'bg-gradient-to-br from-brand-500 to-brand-600 text-white rounded-br-md shadow-glow-sm'
                  : 'bg-white/[0.06] text-slate-200 rounded-bl-md border border-white/[0.06]'
                }`}
            >
              {m.content}
            </div>
            {m.sender === 'USER' && (
              <div className="ml-2 mt-1 flex-shrink-0 h-7 w-7 rounded-full bg-slate-600 flex items-center justify-center text-xs text-white font-bold">
                U
              </div>
            )}
          </div>
        ))}

        {sending && (
          <div className="flex items-center gap-2">
            <div className="h-7 w-7 rounded-full bg-gradient-to-br from-brand-500 to-purple-600 flex items-center justify-center text-xs text-white font-bold shadow-glow-sm">
              AI
            </div>
            <div className="bg-white/[0.06] rounded-2xl rounded-bl-md border border-white/[0.06] px-4 py-3">
              <div className="flex items-center gap-1.5">
                <div className="h-2 w-2 rounded-full bg-brand-400 animate-bounce" style={{ animationDelay: '0ms' }} />
                <div className="h-2 w-2 rounded-full bg-brand-400 animate-bounce" style={{ animationDelay: '150ms' }} />
                <div className="h-2 w-2 rounded-full bg-brand-400 animate-bounce" style={{ animationDelay: '300ms' }} />
              </div>
            </div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <form onSubmit={handleSend} className="mt-3 flex gap-2">
        <input
          id="chat-input"
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Type your answer…"
          className="input-field flex-1 !rounded-xl"
          disabled={sending}
        />
        {voiceSupported && (
          <button
            type="button"
            onClick={toggleListening}
            disabled={sending}
            aria-label={listening ? 'Stop listening' : 'Use voice input'}
            title={listening ? 'Stop listening' : 'Use voice input'}
            className={`rounded-xl border px-3 transition-colors ${listening
              ? 'border-red-400/50 bg-red-400/15 text-red-300'
              : 'border-white/10 bg-white/[0.06] text-slate-300 hover:bg-white/10'
            }`}
          >
            {listening ? '⏹' : '🎙️'}
          </button>
        )}
        {voiceSupported && (
          <button
            type="button"
            onClick={() => setVoiceMode((enabled) => !enabled)}
            aria-pressed={voiceMode}
            aria-label={voiceMode ? 'Disable spoken AI replies' : 'Enable spoken AI replies'}
            title={voiceMode ? 'Disable spoken AI replies' : 'Enable spoken AI replies'}
            className={`rounded-xl border px-3 transition-colors ${voiceMode
              ? 'border-brand-400/50 bg-brand-400/15 text-brand-300'
              : 'border-white/10 bg-white/[0.06] text-slate-300 hover:bg-white/10'
            }`}
          >
            🔊
          </button>
        )}
        <button
          id="chat-send-btn"
          disabled={sending || !input.trim()}
          className="btn-primary !px-5 !rounded-xl"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
          </svg>
        </button>
      </form>
    </div>
  );
}
