import { apiClient } from '../lib/api';
import { GamificationProfile, LeaderboardEntry } from '../types/gamification';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const gamificationService = {
  async getProfile(): Promise<GamificationProfile> {
    const res = await apiClient.get<ApiResponse<GamificationProfile>>('/gamification/profile');
    return res.data.data;
  },

  async getLeaderboard(type: 'global' | 'weekly' = 'global', page = 0, size = 20): Promise<LeaderboardEntry[]> {
    const res = await apiClient.get<ApiResponse<LeaderboardEntry[]>>('/gamification/leaderboard', {
      params: { type, page, size }
    });
    return res.data.data;
  },

  async togglePrivacy(visible: boolean): Promise<boolean> {
    const res = await apiClient.put<ApiResponse<boolean>>('/gamification/privacy', null, {
      params: { visible }
    });
    return res.data.data;
  }
};
