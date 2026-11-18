import React, { useEffect, useState } from 'react';
import { gamificationService } from '../services/gamificationService';
import { LeaderboardEntry } from '../types/gamification';
import { LeaderboardTable } from '../components/gamification/LeaderboardTable';

export const LeaderboardPage: React.FC = () => {
  const [entries, setEntries] = useState<LeaderboardEntry[]>([]);
  const [type, setType] = useState<'global' | 'weekly'>('global');
  const [privacyVisible, setPrivacyVisible] = useState<boolean>(true);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchLeaderboard(type);
  }, [type]);

  const fetchLeaderboard = async (t: 'global' | 'weekly') => {
    setLoading(true);
    setError(null);
    try {
      const data = await gamificationService.getLeaderboard(t);
      setEntries(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load leaderboard');
    } finally {
      setLoading(false);
    }
  };

  const handleTogglePrivacy = async (visible: boolean) => {
    try {
      const res = await gamificationService.togglePrivacy(visible);
      setPrivacyVisible(res);
      fetchLeaderboard(type);
    } catch (err) {
      console.error('Failed to toggle privacy', err);
    }
  };

  return (
    <div className="min-h-screen bg-stone-950 text-stone-200 p-6 md:p-10">
      <div className="max-w-5xl mx-auto space-y-6">
        <header>
          <h1 className="text-3xl font-extrabold text-amber-200">
            முன்னணிப் பட்டியல் (Leaderboard)
          </h1>
          <p className="text-stone-400 text-sm mt-1">
            Compete with fellow learners on Tamil arts and heritage mastery.
          </p>
        </header>

        {loading ? (
          <div className="flex justify-center py-20">
            <div className="animate-spin rounded-full h-10 w-10 border-4 border-amber-500 border-t-transparent" />
          </div>
        ) : error ? (
          <div className="bg-red-950/80 border border-red-500/50 p-4 rounded-lg text-red-300 text-center">
            {error}
          </div>
        ) : (
          <LeaderboardTable
            entries={entries}
            type={type}
            onTypeChange={setType}
            privacyVisible={privacyVisible}
            onTogglePrivacy={handleTogglePrivacy}
          />
        )}
      </div>
    </div>
  );
};
