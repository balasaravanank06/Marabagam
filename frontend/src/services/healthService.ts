import { apiClient, aiClient } from '../lib/api';
import { BackendHealthResponse, AiHealthResponse } from '../types';

export const healthService = {
  async getBackendHealth(): Promise<BackendHealthResponse> {
    const response = await apiClient.get<BackendHealthResponse>('/health');
    return response.data;
  },

  async getAiHealth(): Promise<AiHealthResponse> {
    const response = await aiClient.get<AiHealthResponse>('/health');
    return response.data;
  }
};
