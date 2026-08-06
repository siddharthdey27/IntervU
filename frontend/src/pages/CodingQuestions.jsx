import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listQuestions } from "../api/codingApi";

const difficultyColor = {
  EASY: "text-green-600 bg-green-50",
  MEDIUM: "text-yellow-600 bg-yellow-50",
  HARD: "text-red-600 bg-red-50",
};

export default function CodingQuestions() {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    listQuestions()
      .then(setQuestions)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="p-8 text-gray-500">Loading questions...</div>;
  if (error) return <div className="p-8 text-red-600">Failed to load questions: {error}</div>;

  return (
    <div className="max-w-3xl mx-auto p-6">
      <h1 className="text-2xl font-semibold mb-4">Coding Questions</h1>
      <div className="divide-y divide-gray-200 border rounded-lg overflow-hidden">
        {questions.map((q) => (
          <Link
            key={q.id}
            to={`/coding-questions/${q.id}`}
            className="flex items-center justify-between px-4 py-3 hover:bg-gray-50 transition"
          >
            <div>
              <div className="font-medium">{q.title}</div>
              <div className="text-sm text-gray-500">{q.category}</div>
            </div>
            <span className={`text-xs font-medium px-2 py-1 rounded-full ${difficultyColor[q.difficulty]}`}>
              {q.difficulty}
            </span>
          </Link>
        ))}
        {questions.length === 0 && (
          <div className="px-4 py-6 text-gray-500 text-sm">No questions yet.</div>
        )}
      </div>
    </div>
  );
}
