export interface Badge {
  id: string;
  code: string;
  nameEn: string;
  nameTa: string;
  descriptionEn: string;
  descriptionTa: string;
  iconUrl: string;
  category: string;
  earned: boolean;
  awardedAt?: string;
}

export interface RecentActivity {
  id: string;
  amount: number;
  sourceActivity: string;
  createdAt: string;
}

export interface GamificationProfile {
  totalXp: number;
  level: number;
  levelTitleEn: string;
  levelTitleTa: string;
  currentStreak: number;
  longestStreak: number;
  totalBadges: number;
  badges: Badge[];
  completedCoursesCount?: number;
  quizzesPassedCount?: number;
  leaderboardRank?: number | null;
  recentActivities?: RecentActivity[];
}

export interface LeaderboardEntry {
  rank: number;
  displayName: string;
  xp: number;
  level: number;
  badgesCount: number;
}
