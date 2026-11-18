import React from 'react';

interface XpProgressBarProps {
  totalXp: number;
  level: number;
  levelTitleEn: string;
  levelTitleTa: string;
  language?: 'en' | 'ta';
}

export const XpProgressBar: React.FC<XpProgressBarProps> = ({
  totalXp,
  level,
  levelTitleEn,
  levelTitleTa,
  language = 'en'
}) => {
  const isTa = language === 'ta';
  // Level formula: Level(XP) = floor(sqrt(XP / 100)) + 1
  // XP threshold for level L is 100 * (L - 1)^2
  // XP threshold for next level L + 1 is 100 * L^2
  const currentLevelMinXp = 100 * Math.pow(level - 1, 2);
  const nextLevelXp = 100 * Math.pow(level, 2);
  const xpInCurrentLevel = totalXp - currentLevelMinXp;
  const xpNeededForLevel = nextLevelXp - currentLevelMinXp;
  const progressPercent = Math.min(100, Math.max(0, (xpInCurrentLevel / xpNeededForLevel) * 100));

  return (
    <div className="bg-stone-900 border border-amber-900/40 rounded-xl p-5 text-stone-200 shadow-md">
      <div className="flex justify-between items-center mb-3">
        <div>
          <span className="text-xs uppercase tracking-wider text-amber-500 font-bold">
            {isTa ? 'நிலை' : 'Level'} {level}
          </span>
          <h3 className="text-xl font-bold text-amber-200">
            {isTa ? levelTitleTa : levelTitleEn}
          </h3>
        </div>
        <div className="text-right">
          <span className="text-2xl font-black text-amber-400">{totalXp}</span>
          <span className="text-xs text-stone-400 block">XP</span>
        </div>
      </div>

      <div className="w-full bg-stone-950 rounded-full h-3.5 border border-stone-800 p-0.5 overflow-hidden">
        <div
          className="bg-gradient-to-r from-amber-600 via-amber-500 to-yellow-400 h-full rounded-full transition-all duration-500 shadow-sm"
          style={{ width: `${progressPercent}%` }}
        />
      </div>

      <div className="flex justify-between text-xs text-stone-400 mt-2">
        <span>{totalXp} XP</span>
        <span>{isTa ? `அடுத்த நிலை: ${nextLevelXp} XP` : `Next Level: ${nextLevelXp} XP`}</span>
      </div>
    </div>
  );
};
