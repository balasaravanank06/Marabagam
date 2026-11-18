import { apiClient } from '../lib/api';
import { Movement, PracticeSession, EvaluatePracticeSessionRequest } from '../types/movement';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PaginatedData<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const movementService = {
  async getMovements(artFormId?: string): Promise<Movement[]> {
    const params = artFormId ? { artFormId } : {};
    const res = await apiClient.get<ApiResponse<Movement[]>>('/movements', { params });
    return res.data.data;
  },

  async getMovementById(id: string): Promise<Movement> {
    const res = await apiClient.get<ApiResponse<Movement>>(`/movements/${id}`);
    return res.data.data;
  },

  async startPracticeSession(movementId: string): Promise<PracticeSession> {
    const res = await apiClient.post<ApiResponse<PracticeSession>>('/practice-sessions', { movementId });
    return res.data.data;
  },

  async evaluatePracticeSession(sessionId: string, request: EvaluatePracticeSessionRequest): Promise<PracticeSession> {
    const res = await apiClient.post<ApiResponse<PracticeSession>>(`/practice-sessions/${sessionId}/evaluate`, request);
    return res.data.data;
  },

  async getUserPracticeSessions(page = 0, size = 10): Promise<PaginatedData<PracticeSession>> {
    const res = await apiClient.get<ApiResponse<PaginatedData<PracticeSession>>>('/practice-sessions', {
      params: { page, size }
    });
    return res.data.data;
  },

  async getPracticeSessionById(id: string): Promise<PracticeSession> {
    const res = await apiClient.get<ApiResponse<PracticeSession>>(`/practice-sessions/${id}`);
    return res.data.data;
  },

  async deletePracticeSession(id: string): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`/practice-sessions/${id}`);
  }
};
