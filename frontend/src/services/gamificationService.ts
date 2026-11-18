import { GamificationProfile, LeaderboardEntry } from '../types/gamification';

const API_BASE = '/api/v1/gamification';

function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

export const gamificationService = {
  async getProfile(): Promise<GamificationProfile> {
    const res = await fetch(`${API_BASE}/profile`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error('Failed to fetch gamification profile');
    const json = await res.json();
    return json.data;
  },

  async getLeaderboard(type: 'global' | 'weekly' = 'global', page = 0, size = 20): Promise<LeaderboardEntry[]> {
    const res = await fetch(`${API_BASE}/leaderboard?type=${type}&page=${page}&size=${size}`);
    if (!res.ok) throw new Error('Failed to fetch leaderboard');
    const json = await res.json();
    return json.data;
  },

  async togglePrivacy(visible: boolean): Promise<boolean> {
    const res = await fetch(`${API_BASE}/privacy?visible=${visible}`, {
      method: 'PUT',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to update leaderboard privacy');
    const json = await res.json();
    return json.data;
  }
};
