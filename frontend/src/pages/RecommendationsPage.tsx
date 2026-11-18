import React, { useEffect, useState } from 'react';
import { RecommendationFeed } from '../types/recommendation';
import { recommendationService } from '../services/recommendationService';
import { RecommendationCard } from '../components/RecommendationCard';
import { Sparkles } from 'lucide-react';

export const RecommendationsPage: React.FC = () => {
  const [feed, setFeed] = useState<RecommendationFeed | null>(null);
  const [loading, setLoading] = useState(true);
  const [isTamil, setIsTamil] = useState(false);

  useEffect(() => {
    fetchRecommendations();
  }, []);

  const fetchRecommendations = async () => {
    setLoading(true);
    try {
      const data = await recommendationService.getRecommendations();
      setFeed(data);
    } catch (err) {
      console.error('Failed to fetch recommendations', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-6 md:p-10 max-w-7xl mx-auto space-y-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-800 pb-6">
        <div>
          <div className="flex items-center gap-2 text-amber-400 text-sm font-semibold mb-1">
            <Sparkles className="w-4 h-4" />
            <span>{isTamil ? 'தனிப்பயனாக்கப்பட்ட பரிந்துரைகள்' : 'Personalized Recommendations'}</span>
          </div>
          <h1 className="text-3xl font-black tracking-tight text-slate-100">
            {isTamil ? 'உங்களுக்கான கற்றல் பரிந்துரைகள்' : 'Curated Learning & Event Discovery'}
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            {isTamil ? 'உங்கள் கற்றல் செயல்பாடுகள் மற்றும் ஆர்வங்களின் அடிப்படையில் விளக்கப்பட்ட பரிந்துரைகள்.' : 'Explainable recommendations tailored to your heritage learning journey and practice activity.'}
          </p>
        </div>

        <button
          onClick={() => setIsTamil(!isTamil)}
          className="self-start md:self-auto text-xs font-semibold px-3 py-1.5 rounded-lg border border-slate-700 text-slate-300 hover:bg-slate-800 transition-colors"
        >
          {isTamil ? 'English' : 'தமிழ்'}
        </button>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-64 bg-slate-900/50 rounded-xl animate-pulse border border-slate-800" />
          ))}
        </div>
      ) : !feed || feed.recommendations.length === 0 ? (
        <div className="p-12 text-center text-slate-400">No recommendations available right now.</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {feed.recommendations.map((rec) => (
            <RecommendationCard key={rec.id} recommendation={rec} isTamil={isTamil} />
          ))}
        </div>
      )}
    </div>
  );
};
