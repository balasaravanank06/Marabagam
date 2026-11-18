import { Quiz, AttemptResult, QuizSubmissionPayload } from '../types/quiz';

const API_BASE = '/api/v1/quizzes';

function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

export const quizService = {
  async getQuiz(quizId: string): Promise<Quiz> {
    const res = await fetch(`${API_BASE}/${quizId}`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error('Failed to fetch quiz');
    const json = await res.json();
    return json.data;
  },

  async startAttempt(quizId: string): Promise<AttemptResult> {
    const res = await fetch(`${API_BASE}/${quizId}/start`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to start quiz attempt');
    const json = await res.json();
    return json.data;
  },

  async submitAttempt(attemptId: string, payload: QuizSubmissionPayload): Promise<AttemptResult> {
    const res = await fetch(`${API_BASE}/attempts/${attemptId}/submit`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error('Failed to submit quiz attempt');
    const json = await res.json();
    return json.data;
  },

  async getAttemptResult(attemptId: string): Promise<AttemptResult> {
    const res = await fetch(`${API_BASE}/attempts/${attemptId}`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error('Failed to fetch attempt result');
    const json = await res.json();
    return json.data;
  }
};
