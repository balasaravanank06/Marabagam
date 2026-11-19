import React, { useState } from 'react';
import { Footprints, Award, Sparkles, Swords, Flame, ShieldCheck, Lock, CheckCircle2 } from 'lucide-react';
import { Badge } from '../../types/gamification';

interface BadgeCardProps {
  badge: Badge;
  language?: 'en' | 'ta';
}

const getBadgeSlug = (code?: string, iconUrl?: string): string => {
  if (code === 'FIRST_STEP') return 'first-step';
  if (code === 'FIRST_QUIZ_PASS') return 'first-quiz';
  if (code === 'PERFECT_SCORE') return 'perfect-score';
  if (code === 'SILAMBAM_WARRIOR') return 'silambam-warrior';
  if (code === 'WEEKLY_STREAK_7') return 'streak-7';
  if (code === 'HERITAGE_GUARDIAN_3') return 'guardian';
  if (iconUrl) {
    const filename = iconUrl.split('/').pop()?.replace(/\.(png|jpg|webp|svg)$/, '');
    if (filename) return filename;
  }
  return code ? code.toLowerCase().replace(/_/g, '-') : 'badge';
};

const renderFallbackIcon = (code?: string) => {
  switch (code) {
    case 'FIRST_STEP':
      return <Footprints className="w-7 h-7 text-amber-400" />;
    case 'FIRST_QUIZ_PASS':
      return <Award className="w-7 h-7 text-amber-300" />;
    case 'PERFECT_SCORE':
      return <Sparkles className="w-7 h-7 text-yellow-300" />;
    case 'SILAMBAM_WARRIOR':
      return <Swords className="w-7 h-7 text-amber-500" />;
    case 'WEEKLY_STREAK_7':
      return <Flame className="w-7 h-7 text-orange-400" />;
    case 'HERITAGE_GUARDIAN_3':
      return <ShieldCheck className="w-7 h-7 text-sky-400" />;
    default:
      return <Award className="w-7 h-7 text-amber-400" />;
  }
};

export const BadgeCard: React.FC<BadgeCardProps> = ({ badge, language = 'en' }) => {
  const isTa = language === 'ta';
  const slug = getBadgeSlug(badge.code, badge.iconUrl);
  const localSvgSrc = `/badges/${slug}.svg`;
  const [imageError, setImageError] = useState(false);

  return (
    <div
      className={`rounded-2xl border p-4 sm:p-5 transition-all duration-300 relative flex flex-col justify-between min-h-[195px] ${
        badge.earned
          ? 'bg-gradient-to-b from-stone-900/95 to-stone-950/95 border-amber-500/40 shadow-lg shadow-amber-950/20 hover:border-amber-400/60'
          : 'bg-stone-950/70 border-stone-800/80 hover:border-stone-700/80'
      }`}
    >
      <div>
        {/* Top Header: Badge Art & Earned Status */}
        <div className="flex items-center justify-between gap-3 mb-3">
          <div
            className={`w-14 h-14 sm:w-16 sm:h-16 rounded-2xl flex items-center justify-center p-1 relative flex-shrink-0 border ${
              badge.earned
                ? 'bg-stone-900/90 border-amber-500/40 shadow-inner'
                : 'bg-stone-900/50 border-stone-800 opacity-60 grayscale'
            }`}
          >
            {!imageError ? (
              <img
                src={localSvgSrc}
                alt={badge.nameEn || 'Badge Icon'}
                className="w-full h-full object-contain filter drop-shadow"
                onError={() => setImageError(true)}
              />
            ) : (
              <div className="flex items-center justify-center w-full h-full">
                {renderFallbackIcon(badge.code)}
              </div>
            )}
          </div>

          <div className="flex flex-col items-end gap-1">
            {badge.earned ? (
              <span className="inline-flex items-center gap-1 bg-amber-950/90 border border-amber-500/60 text-amber-300 text-[10px] sm:text-xs font-bold px-2.5 py-1 rounded-full uppercase tracking-wider shadow-sm">
                <CheckCircle2 className="w-3 h-3 text-amber-400" />
                {isTa ? 'பெறப்பட்டது' : 'Earned'}
              </span>
            ) : (
              <span className="inline-flex items-center gap-1 bg-stone-900/90 border border-stone-800 text-stone-400 text-[10px] sm:text-xs font-medium px-2.5 py-1 rounded-full">
                <Lock className="w-3 h-3 text-stone-500" />
                {isTa ? 'பூட்டப்பட்டது' : 'Locked'}
              </span>
            )}
          </div>
        </div>

        {/* Badge Title & Description */}
        <h4 className={`font-bold font-serif text-base sm:text-lg mb-1 leading-snug ${
          badge.earned ? 'text-amber-200' : 'text-stone-300'
        }`}>
          {isTa && badge.nameTa ? badge.nameTa : badge.nameEn}
        </h4>
        <p className="text-xs text-stone-400 leading-relaxed line-clamp-3">
          {isTa && badge.descriptionTa ? badge.descriptionTa : badge.descriptionEn}
        </p>
      </div>

      {/* Footer: Award Date or Locked Requirement Note */}
      <div className="mt-4 pt-2.5 border-t border-stone-800/80 flex items-center justify-between text-[11px]">
        {badge.earned && badge.awardedAt ? (
          <span className="text-stone-400">
            <span className="text-stone-500">{isTa ? 'வழங்கப்பட்டது:' : 'Awarded:'}</span>{' '}
            {new Date(badge.awardedAt).toLocaleDateString(isTa ? 'ta-IN' : 'en-US', {
              year: 'numeric',
              month: 'short',
              day: 'numeric'
            })}
          </span>
        ) : (
          <span className="text-stone-500 italic text-[10px]">
            {isTa ? 'தொடர்ந்து கற்றுத் திறக்கவும்' : 'Complete learning to unlock'}
          </span>
        )}
      </div>
    </div>
  );
};
