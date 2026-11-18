import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { movementService } from '../services/movementService';
import { Movement } from '../types/movement';
import { Play, Sparkles, Shield, Camera } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export const MovementCoachDirectoryPage: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isTamil = i18n.language === 'ta';
  const navigate = useNavigate();

  const [movements, setMovements] = useState<Movement[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchMovements = async () => {
      try {
        setLoading(true);
        const data = await movementService.getMovements();
        setMovements(data);
      } catch (err: any) {
        setError('Failed to load movement catalog');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchMovements();
  }, []);

  return (
    <div className="space-y-8 max-w-7xl mx-auto px-4 py-8 text-amber-50">
      {/* Hero Banner */}
      <div className="relative rounded-3xl overflow-hidden bg-gradient-to-r from-amber-950 via-amber-900 to-stone-950 p-8 border border-amber-500/30 shadow-2xl">
        <div className="relative z-10 max-w-2xl space-y-4">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/20 text-amber-400 text-xs font-semibold border border-amber-500/30">
            <Sparkles className="w-4 h-4" />
            <span>{t('movement.heroBadge', 'AI Movement Coach — Computer Vision')}</span>
          </div>
          <h1 className="text-3xl md:text-4xl font-extrabold font-serif text-amber-100 leading-tight">
            {t('movement.heroTitle', 'Tamil Traditional Arts Movement Coach')}
          </h1>
          <p className="text-sm text-amber-200/80 leading-relaxed">
            {t(
              'movement.heroSubtitle',
              'Voluntarily practice traditional postures using your device camera. Experience privacy-first on-device pose estimation and metric-driven biomechanical feedback.'
            )}
          </p>
        </div>
      </div>

      {/* Catalog Grid */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold font-serif text-amber-100 flex items-center gap-2">
            <Camera className="w-5 h-5 text-amber-400" />
            <span>{t('movement.availableCatalog', 'Available Practice Movements')}</span>
          </h2>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {[1, 2, 3].map((n) => (
              <div key={n} className="h-64 rounded-2xl bg-amber-950/20 border border-amber-900/40 animate-pulse" />
            ))}
          </div>
        ) : error ? (
          <div className="p-6 rounded-2xl bg-red-950/40 border border-red-500/40 text-red-300 text-sm">
            {error}
          </div>
        ) : movements.length === 0 ? (
          <div className="p-6 rounded-2xl bg-amber-950/20 border border-amber-900/40 text-amber-400 text-sm text-center">
            {t('movement.noMovements', 'No practice movements currently available.')}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {movements.map((m) => (
              <div
                key={m.id}
                className="bg-amber-950/40 border border-amber-500/30 hover:border-amber-400/60 rounded-2xl p-6 flex flex-col justify-between transition-all duration-300 shadow-xl group hover:-translate-y-1"
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="px-2.5 py-1 rounded-md bg-amber-500/20 text-amber-300 text-xs font-semibold border border-amber-500/30">
                      {m.category}
                    </span>
                    <span className="text-xs text-amber-400/80 font-mono font-medium">
                      {m.difficultyLevel}
                    </span>
                  </div>

                  <h3 className="text-lg font-bold font-serif text-amber-100 group-hover:text-amber-300 transition-colors">
                    {isTamil ? m.nameTa : m.nameEn}
                  </h3>

                  <p className="text-xs text-amber-200/75 line-clamp-3 leading-relaxed">
                    {isTamil ? m.descriptionTa : m.descriptionEn}
                  </p>
                </div>

                <div className="pt-6 mt-4 border-t border-amber-900/50 flex items-center justify-between">
                  <div className="flex items-center gap-1.5 text-xs text-amber-400">
                    <Shield className="w-3.5 h-3.5" />
                    <span>v{m.currentVersionNumber || '1.0.0'}</span>
                  </div>

                  <button
                    onClick={() => navigate(`/movement-coach/${m.id}/practice`)}
                    className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-600 hover:to-orange-700 text-amber-950 font-bold rounded-xl shadow-md text-xs transition-all cursor-pointer"
                  >
                    <Play className="w-3.5 h-3.5 fill-amber-950" />
                    <span>{t('movement.practiceBtn', 'Start Practice')}</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
