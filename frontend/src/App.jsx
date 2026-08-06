import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import ResumeUpload from './pages/ResumeUpload.jsx';
import InterviewChat from './pages/InterviewChat.jsx';
import CodingQuestions from './pages/CodingQuestions.jsx';
import CodeEditor from './pages/CodeEditor.jsx';
import Progress from './pages/Progress.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import Navbar from './components/Navbar.jsx';

export default function App() {
  return (
    <div className="min-h-screen relative">
      <div className="ambient-bg" />
      <div className="relative z-10">
        <Navbar />
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/resume" element={<ProtectedRoute><ResumeUpload /></ProtectedRoute>} />
          <Route path="/interview/:sessionId" element={<ProtectedRoute><InterviewChat /></ProtectedRoute>} />
          <Route path="/coding-questions" element={<ProtectedRoute><CodingQuestions /></ProtectedRoute>} />
          <Route path="/coding-questions/:id" element={<ProtectedRoute><CodeEditor /></ProtectedRoute>} />
          <Route path="/progress" element={<ProtectedRoute><Progress /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </div>
  );
}
