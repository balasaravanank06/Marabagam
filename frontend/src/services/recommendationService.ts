import { apiClient } from '../lib/api';
import { RecommendationFeed } from '../types/recommendation';

export const recommendationService = {
  getRecommendations: async (): Promise<RecommendationFeed> => {
    const response = await apiClient.get<{ success: boolean; data: RecommendationFeed }>('/recommendations');
    return response.data.data;
  }
};
