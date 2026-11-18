import React from 'react';
import { Link } from 'react-router-dom';
import { Recommendation } from '../types/recommendation';
import { Sparkles, ArrowRight, BookOpen, Calendar, Compass } from 'lucide-react';

interface RecommendationCardProps {
  recommendation: Recommendation;
  isTamil?: boolean;
}

export const RecommendationCard: React.FC<RecommendationCardProps> = ({ recommendation, isTamil = false }) => {
  const title = isTamil ? recommendation.titleTa || recommendation.title : recommendation.title;
  const description = isTamil ? recommendation.descriptionTa || recommendation.description : recommendation.description;
  const reason = isTamil ? recommendation.reasonTa || recommendation.reason : recommendation.reason;

  const getItemIcon = () => {
    switch (recommendation.itemType) {
      case 'COURSE':
        return <BookOpen className="w-4 h-4 text-emerald-400" />;
      case 'EVENT':
        return <Calendar className="w-4 h-4 text-amber-400" />;
      case 'ART_FORM':
      default:
        return <Compass className="w-4 h-4 text-sky-400" />;
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 hover:border-amber-500/50 transition-all flex flex-col justify-between shadow-md">
      <div>
        <div className="flex items-center justify-between gap-2 mb-3">
          <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-slate-800 text-slate-300 border border-slate-700 flex items-center gap-1.5">
            {getItemIcon()}
            {recommendation.itemType}
          </span>

          <span className="text-xs font-medium text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded border border-amber-500/20">
            #{recommendation.rank}
          </span>
        </div>

        <Link to={recommendation.targetUrl}>
          <h3 className="text-lg font-bold text-slate-100 hover:text-amber-400 transition-colors line-clamp-1 mb-2">
            {title}
          </h3>
        </Link>

        <p className="text-sm text-slate-400 line-clamp-2 mb-4">
          {description}
        </p>
      </div>

      <div>
        <div className="p-2.5 rounded-lg bg-amber-500/5 border border-amber-500/15 mb-4 flex items-start gap-2">
          <Sparkles className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
          <p className="text-xs text-amber-200/90 italic">
            {reason}
          </p>
        </div>

        <Link
          to={recommendation.targetUrl}
          className="inline-flex items-center justify-between w-full text-xs font-semibold px-3 py-2 rounded-lg bg-slate-800 hover:bg-amber-500 hover:text-slate-950 text-slate-200 transition-all group"
        >
          <span>{isTamil ? 'ஆராயுங்கள்' : 'Explore Now'}</span>
          <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
        </Link>
      </div>
    </div>
  );
};
