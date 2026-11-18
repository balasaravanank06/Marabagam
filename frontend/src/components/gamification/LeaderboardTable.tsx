import React from 'react';
import { LeaderboardEntry } from '../../types/gamification';

interface LeaderboardTableProps {
  entries: LeaderboardEntry[];
  type: 'global' | 'weekly';
  onTypeChange: (type: 'global' | 'weekly') => void;
  privacyVisible?: boolean;
  onTogglePrivacy?: (visible: boolean) => void;
  language?: 'en' | 'ta';
}

export const LeaderboardTable: React.FC<LeaderboardTableProps> = ({
  entries,
  type,
  onTypeChange,
  privacyVisible = true,
  onTogglePrivacy,
  language = 'en'
}) => {
  const isTa = language === 'ta';

  return (
    <div className="bg-stone-900 border border-amber-900/40 rounded-xl p-6 shadow-xl text-stone-200">
      <div className="flex flex-col sm:flex-row justify-between items-center mb-6 gap-4 border-b border-stone-800 pb-4">
        <div>
          <h2 className="text-xl font-bold text-amber-200">
            {isTa ? 'முன்னணிப் பட்டியல் (Leaderboard)' : 'Learner Leaderboard'}
          </h2>
          <p className="text-xs text-stone-400 mt-0.5">
            {isTa ? 'தமிழர்க் கலைக் கற்றலில் முன்னணி வகிப்போர்' : 'Top achievements across Marabagam learners'}
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="bg-stone-950 p-1 rounded-lg border border-stone-800 flex">
            <button
              onClick={() => onTypeChange('global')}
              className={`px-3 py-1.5 rounded-md text-xs font-semibold transition-colors ${
                type === 'global'
                  ? 'bg-amber-600 text-stone-950 shadow'
                  : 'text-stone-400 hover:text-stone-200'
              }`}
            >
              {isTa ? 'அனைத்து காலம்' : 'All-Time'}
            </button>
            <button
              onClick={() => onTypeChange('weekly')}
              className={`px-3 py-1.5 rounded-md text-xs font-semibold transition-colors ${
                type === 'weekly'
                  ? 'bg-amber-600 text-stone-950 shadow'
                  : 'text-stone-400 hover:text-stone-200'
              }`}
            >
              {isTa ? 'இந்த வாரம்' : 'This Week'}
            </button>
          </div>

          {onTogglePrivacy && (
            <label className="flex items-center gap-2 text-xs text-stone-400 cursor-pointer bg-stone-950 px-3 py-1.5 rounded-lg border border-stone-800">
              <input
                type="checkbox"
                checked={privacyVisible}
                onChange={e => onTogglePrivacy(e.target.checked)}
                className="accent-amber-500 rounded"
              />
              <span>{isTa ? 'பட்டியலில் காட்டு' : 'Public Profile'}</span>
            </label>
          )}
        </div>
      </div>

      {entries.length === 0 ? (
        <div className="text-center py-12 text-stone-500">
          <p>{isTa ? 'தரவுகள் எதுவும் இல்லை' : 'No leaderboard data available yet.'}</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm border-collapse">
            <thead>
              <tr className="border-b border-stone-800 text-stone-400 text-xs uppercase tracking-wider">
                <th className="pb-3 px-3 w-16 text-center">{isTa ? 'தரவரிசை' : 'Rank'}</th>
                <th className="pb-3 px-4">{isTa ? 'மாணவர் பெயர்' : 'Learner'}</th>
                <th className="pb-3 px-4 text-center">{isTa ? 'நிலை' : 'Level'}</th>
                <th className="pb-3 px-4 text-center">{isTa ? 'பதக்கங்கள்' : 'Badges'}</th>
                <th className="pb-3 px-4 text-right">{isTa ? 'XP' : 'XP Points'}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-stone-800/60">
              {entries.map((entry) => {
                const isTop3 = entry.rank <= 3;
                const medal = entry.rank === 1 ? '🥇' : entry.rank === 2 ? '🥈' : entry.rank === 3 ? '🥉' : '';

                return (
                  <tr
                    key={entry.rank}
                    className={`hover:bg-stone-950/40 transition-colors ${
                      isTop3 ? 'font-semibold' : ''
                    }`}
                  >
                    <td className="py-3 px-3 text-center">
                      {medal ? (
                        <span className="text-lg">{medal}</span>
                      ) : (
                        <span className="text-stone-400 text-xs">#{entry.rank}</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-amber-100">{entry.displayName}</td>
                    <td className="py-3 px-4 text-center">
                      <span className="bg-stone-950 border border-stone-800 px-2 py-0.5 rounded text-xs text-amber-400">
                        {entry.level}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-center text-stone-300">
                      🏅 {entry.badgesCount}
                    </td>
                    <td className="py-3 px-4 text-right font-bold text-amber-400">
                      {entry.xp.toLocaleString()} XP
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
