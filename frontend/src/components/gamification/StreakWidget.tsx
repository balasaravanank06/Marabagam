import React from 'react';

interface StreakWidgetProps {
  currentStreak: number;
  longestStreak: number;
  language?: 'en' | 'ta';
}

export const StreakWidget: React.FC<StreakWidgetProps> = ({
  currentStreak,
  longestStreak,
  language = 'en'
}) => {
  const isTa = language === 'ta';

  return (
    <div className="bg-stone-900 border border-amber-900/40 rounded-xl p-5 text-stone-200 shadow-md flex items-center justify-between">
      <div className="flex items-center space-x-4">
        <div className={`w-14 h-14 rounded-full flex items-center justify-center text-3xl border ${
          currentStreak > 0 
            ? 'bg-amber-950/60 border-amber-500/60 text-amber-400 animate-pulse'
            : 'bg-stone-950 border-stone-800 text-stone-600'
        }`}>
          🔥
        </div>
        <div>
          <h4 className="text-sm font-semibold text-stone-400 uppercase tracking-wide">
            {isTa ? 'தொடர் கற்றல் (Streak)' : 'Daily Streak'}
          </h4>
          <p className="text-2xl font-bold text-amber-200">
            {currentStreak} {isTa ? 'நாட்கள்' : 'Days'}
          </p>
        </div>
      </div>
      <div className="text-right border-l border-stone-800 pl-4">
        <span className="text-xs text-stone-400 block">{isTa ? 'அதிகபட்ச தொடர்' : 'Longest Streak'}</span>
        <span className="text-lg font-bold text-amber-400">{longestStreak} {isTa ? 'நாட்கள்' : 'Days'}</span>
      </div>
    </div>
  );
};
