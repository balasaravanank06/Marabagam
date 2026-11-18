import React, { useEffect, useState } from 'react';
import { gamificationService } from '../services/gamificationService';
import { GamificationProfile } from '../types/gamification';
import { XpProgressBar } from '../components/gamification/XpProgressBar';
import { StreakWidget } from '../components/gamification/StreakWidget';
import { BadgeCard } from '../components/gamification/BadgeCard';

export const AchievementsPage: React.FC = () => {
  const [profile, setProfile] = useState<GamificationProfile | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const data = await gamificationService.getProfile();
      setProfile(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load achievements');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-stone-950 text-stone-200 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-500 border-t-transparent" />
      </div>
    );
  }

  if (error || !profile) {
    return (
      <div className="min-h-screen bg-stone-950 text-stone-200 p-8 text-center">
        <p className="text-red-400 mb-4">{error || 'Achievements profile not available.'}</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-stone-950 text-stone-200 p-6 md:p-10">
      <div className="max-w-5xl mx-auto space-y-8">
        <header className="border-b border-stone-800 pb-4">
          <h1 className="text-3xl font-extrabold text-amber-200">
            என் சாதனைகள் (My Achievements)
          </h1>
          <p className="text-stone-400 text-sm mt-1">
            Track your XP, current level, daily learning streak, and earned Tamil heritage badges.
          </p>
        </header>

        {/* Level and Streak Summary Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <XpProgressBar
            totalXp={profile.totalXp}
            level={profile.level}
            levelTitleEn={profile.levelTitleEn}
            levelTitleTa={profile.levelTitleTa}
          />
          <StreakWidget
            currentStreak={profile.currentStreak}
            longestStreak={profile.longestStreak}
          />
        </div>

        {/* Badges Section */}
        <section className="space-y-4">
          <div className="flex justify-between items-center border-b border-stone-800 pb-3">
            <h2 className="text-xl font-bold text-amber-300">
              பதக்கங்கள் (Badges) ({profile.totalBadges} / {profile.badges.length})
            </h2>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {profile.badges.map(b => (
              <BadgeCard key={b.id || b.code} badge={b} />
            ))}
          </div>
        </section>
      </div>
    </div>
  );
};
