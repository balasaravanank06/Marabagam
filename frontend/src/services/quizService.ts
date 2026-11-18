import { apiClient } from '../lib/api';
import { Quiz, AttemptResult, QuizSubmissionPayload } from '../types/quiz';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const quizService = {
  async getQuiz(quizId: string): Promise<Quiz> {
    const res = await apiClient.get<ApiResponse<Quiz>>(`/quizzes/${quizId}`);
    return res.data.data;
  },

  async startAttempt(quizId: string): Promise<AttemptResult> {
    const res = await apiClient.post<ApiResponse<AttemptResult>>(`/quizzes/${quizId}/start`);
    return res.data.data;
  },

  async submitAttempt(attemptId: string, payload: QuizSubmissionPayload): Promise<AttemptResult> {
    const res = await apiClient.post<ApiResponse<AttemptResult>>(`/quizzes/attempts/${attemptId}/submit`, payload);
    return res.data.data;
  },

  async getAttemptResult(attemptId: string): Promise<AttemptResult> {
    const res = await apiClient.get<ApiResponse<AttemptResult>>(`/quizzes/attempts/${attemptId}`);
    return res.data.data;
  }
};
