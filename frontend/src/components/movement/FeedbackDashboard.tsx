import React from 'react';
import { PracticeSession } from '../../types/movement';
import { Award, AlertCircle, CheckCircle2, RotateCcw, ShieldAlert } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface FeedbackDashboardProps {
  session: PracticeSession;
  onRetry: () => void;
}

export const FeedbackDashboard: React.FC<FeedbackDashboardProps> = ({ session, onRetry }) => {
  const { t, i18n } = useTranslation();
  const isTamil = i18n.language === 'ta';

  const score = session.overallScore ?? 75;

  const getScoreColor = (val: number) => {
    if (val >= 85) return 'text-emerald-400 border-emerald-500/40 bg-emerald-950/40';
    if (val >= 70) return 'text-amber-400 border-amber-500/40 bg-amber-950/40';
    return 'text-orange-400 border-orange-500/40 bg-orange-950/40';
  };

  return (
    <div className="space-y-6 bg-amber-950/40 border border-amber-500/30 rounded-2xl p-6 shadow-2xl text-amber-50">
      {/* Header & Overall Score */}
      <div className="flex flex-col md:flex-row items-center justify-between gap-6 border-b border-amber-900/60 pb-6">
        <div className="space-y-1 text-center md:text-left">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/20 text-amber-400 text-xs font-semibold border border-amber-500/30">
            <Award className="w-3.5 h-3.5" />
            <span>{t('movement.evaluationComplete', 'Biomechanical Analysis Completed')}</span>
          </div>
          <h3 className="text-2xl font-bold font-serif text-amber-100">
            {isTamil ? session.movementNameTa : session.movementNameEn}
          </h3>
          <p className="text-xs text-amber-300/70">
            {t('movement.versionTag', 'Evaluated against reference version:')} {session.versionNumber}
          </p>
        </div>

        {/* Overall Score Badge */}
        <div className={`flex flex-col items-center justify-center p-4 rounded-2xl border ${getScoreColor(score)} min-w-[140px]`}>
          <span className="text-3xl font-extrabold font-mono">{score.toFixed(1)}%</span>
          <span className="text-xs text-amber-200/80 font-medium mt-1">
            {t('movement.alignmentScore', 'Stance Match Score')}
          </span>
        </div>
      </div>

      {/* Cultural Non-Claim Disclaimer Box */}
      <div className="flex items-start gap-3 p-3.5 rounded-xl bg-amber-900/30 border border-amber-700/40 text-xs text-amber-300/90">
        <ShieldAlert className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
        <p>
          <strong>{t('movement.disclaimerHeader', 'Educational Disclaimer:')}</strong>{' '}
          {t(
            'movement.disclaimerBody',
            'This evaluation measures specific biomechanical similarity relative to an expert reference sample. It does not judge cultural authenticity, artistic perfection, or traditional correctness.'
          )}
        </p>
      </div>

      {/* Metric Feedback Cards */}
      <div className="space-y-3">
        <h4 className="text-sm font-bold uppercase tracking-wider text-amber-300">
          {t('movement.priorityFeedback', 'Priority Biomechanical Feedback')}
        </h4>

        {session.feedbacks && session.feedbacks.length > 0 ? (
          session.feedbacks.map((fb) => (
            <div
              key={fb.id}
              className="flex items-start gap-4 p-4 rounded-xl bg-black/40 border border-amber-900/50 hover:border-amber-700/50 transition-colors"
            >
              {fb.severity === 'WARNING' ? (
                <AlertCircle className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
              ) : (
                <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
              )}
              <div className="space-y-1 flex-1">
                <div className="flex items-center justify-between text-xs font-semibold text-amber-200">
                  <span className="uppercase tracking-wider text-amber-400">{fb.bodyPart} — {fb.metricName}</span>
                  <span className="font-mono text-amber-300">
                    Measured: {fb.measuredValue} (Target: {fb.targetRangeMin}–{fb.targetRangeMax})
                  </span>
                </div>
                <p className="text-sm text-amber-100 font-sans leading-relaxed">
                  {isTamil ? fb.feedbackTextTa : fb.feedbackTextEn}
                </p>
              </div>
            </div>
          ))
        ) : (
          <p className="text-xs text-amber-400/70 italic">
            {t('movement.noFeedback', 'No specific adjustment suggestions. Good overall stance stability.')}
          </p>
        )}
      </div>

      {/* Action Buttons */}
      <div className="flex items-center justify-end gap-4 pt-4 border-t border-amber-900/60">
        <button
          onClick={onRetry}
          className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-600 hover:to-orange-700 text-amber-950 font-bold transition-all shadow-lg text-sm cursor-pointer"
        >
          <RotateCcw className="w-4 h-4" />
          <span>{t('movement.practiceAgain', 'Practice Again')}</span>
        </button>
      </div>
    </div>
  );
};
