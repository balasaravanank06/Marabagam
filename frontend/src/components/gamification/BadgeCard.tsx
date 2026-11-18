import React from 'react';
import { Badge } from '../../types/gamification';

interface BadgeCardProps {
  badge: Badge;
  language?: 'en' | 'ta';
}

export const BadgeCard: React.FC<BadgeCardProps> = ({ badge, language = 'en' }) => {
  const isTa = language === 'ta';

  return (
    <div
      className={`rounded-xl border p-4 transition-all duration-300 relative overflow-hidden flex flex-col justify-between ${
        badge.earned
          ? 'bg-stone-900/90 border-amber-500/50 shadow-lg shadow-amber-950/20'
          : 'bg-stone-950/60 border-stone-800 opacity-60 grayscale'
      }`}
    >
      <div>
        <div className="flex items-center justify-between mb-3">
          <div className="w-12 h-12 rounded-lg bg-stone-950 border border-amber-900/40 flex items-center justify-center text-2xl shadow-inner">
            {badge.iconUrl || '🏆'}
          </div>
          {badge.earned ? (
            <span className="bg-amber-950/80 border border-amber-500/50 text-amber-300 text-[10px] font-bold px-2 py-0.5 rounded-full uppercase">
              {isTa ? 'பெறப்பட்டது' : 'Earned'}
            </span>
          ) : (
            <span className="bg-stone-900 text-stone-500 text-[10px] font-semibold px-2 py-0.5 rounded-full">
              {isTa ? 'பூட்டப்பட்டது' : 'Locked'}
            </span>
          )}
        </div>

        <h4 className="font-bold text-amber-200 text-base mb-1">
          {isTa && badge.nameTa ? badge.nameTa : badge.nameEn}
        </h4>
        <p className="text-xs text-stone-400 leading-relaxed">
          {isTa && badge.descriptionTa ? badge.descriptionTa : badge.descriptionEn}
        </p>
      </div>

      {badge.awardedAt && (
        <div className="mt-4 pt-2 border-t border-stone-800 text-[10px] text-stone-500">
          {isTa ? 'வழங்கப்பட்ட நாள்:' : 'Awarded:'}{' '}
          {new Date(badge.awardedAt).toLocaleDateString()}
        </div>
      )}
    </div>
  );
};
