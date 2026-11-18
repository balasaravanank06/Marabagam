import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Award, AlertCircle, RefreshCw } from 'lucide-react';
import { gamificationService } from '../services/gamificationService';
import { LeaderboardEntry } from '../types/gamification';
import { LeaderboardTable } from '../components/gamification/LeaderboardTable';

export const LeaderboardPage: React.FC = () => {
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

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
      setEntries(data || []);
    } catch (err: any) {
      const errorMsg =
        err?.response?.data?.message ||
        err?.message ||
        (isTa ? 'முன்னணிப் பட்டியலை ஏற்றுவதில் தோல்வி' : 'Failed to load leaderboard');
      setError(errorMsg);
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
    <div className="space-y-8 max-w-7xl mx-auto">
      <header className="border-b border-slate-800 pb-5">
        <div className="flex items-center space-x-2 text-amber-400 text-xs font-semibold uppercase tracking-wider mb-1">
          <Award className="w-4 h-4" />
          <span>{isTa ? 'கற்றல் போட்டி & தரவரிசை' : 'Learner Rankings'}</span>
        </div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-white">
          {isTa ? 'முன்னணிப் பட்டியல் (Leaderboard)' : 'Heritage Leaderboard'}
        </h1>
        <p className="text-slate-400 text-xs sm:text-sm mt-1">
          {isTa
            ? 'பாரம்பரியக் கலைகளில் அதிக புள்ளிகள் (XP) பெற்ற மாணவர்களின் பெருமைப் பட்டியல்.'
            : 'Compete with fellow learners on Tamil arts and heritage mastery.'}
        </p>
      </header>

      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 space-y-4">
          <div className="animate-spin rounded-full h-10 w-10 border-4 border-amber-500 border-t-transparent" />
          <p className="text-sm text-slate-400">
            {isTa ? 'தரவரிசை ஏற்றப்படுகிறது...' : 'Loading leaderboard...'}
          </p>
        </div>
      ) : error ? (
        <div className="bg-rose-950/60 border border-rose-500/50 p-6 rounded-2xl text-center space-y-4 shadow-xl max-w-xl mx-auto">
          <div className="w-12 h-12 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center mx-auto">
            <AlertCircle className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-base font-bold text-rose-200">
              {isTa ? 'முன்னணிப் பட்டியலை ஏற்ற முடியவில்லை' : 'Failed to load leaderboard'}
            </h3>
            <p className="text-xs text-rose-300/80 mt-1">{error}</p>
          </div>
          <button
            onClick={() => fetchLeaderboard(type)}
            className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold transition shadow-lg shadow-rose-900/30"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>{isTa ? 'மீண்டும் முயற்சிக்கவும் (Retry)' : 'Retry'}</span>
          </button>
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
  );
};
