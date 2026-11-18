import React from 'react';
import { AttemptResult } from '../../types/quiz';

interface QuizResultSummaryProps {
  result: AttemptResult;
  onRetry?: () => void;
  onContinue?: () => void;
  language?: 'en' | 'ta';
}

export const QuizResultSummary: React.FC<QuizResultSummaryProps> = ({
  result,
  onRetry,
  onContinue,
  language = 'en'
}) => {
  const isTa = language === 'ta';

  return (
    <div className="bg-stone-900 border border-amber-900/40 rounded-xl p-6 shadow-xl max-w-2xl mx-auto text-stone-200">
      <div className="text-center mb-6">
        <div
          className={`inline-flex items-center justify-center w-20 h-20 rounded-full text-4xl mb-3 ${
            result.passed
              ? 'bg-emerald-950/80 border-2 border-emerald-500 text-emerald-400'
              : 'bg-red-950/80 border-2 border-red-500 text-red-400'
          }`}
        >
          {result.passed ? '✓' : '✕'}
        </div>
        <h2 className="text-2xl font-bold text-amber-200">
          {result.passed
            ? isTa ? 'தேர்வில் வெற்றி பெற்றுள்ளீர்கள்!' : 'Quiz Passed!'
            : isTa ? 'மீண்டும் முயற்சிக்கவும்' : 'Quiz Failed'}
        </h2>
        <p className="text-stone-400 text-sm mt-1">
          {isTa ? `முயற்சி #${result.attemptNumber}` : `Attempt #${result.attemptNumber}`}
        </p>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-6 bg-stone-950 p-4 rounded-lg border border-stone-800">
        <div className="text-center">
          <div className="text-sm text-stone-400">{isTa ? 'மதிப்பெண்' : 'Score'}</div>
          <div className="text-2xl font-bold text-amber-400">
            {result.scoreEarned} / {result.totalPossibleScore}
          </div>
        </div>
        <div className="text-center">
          <div className="text-sm text-stone-400">{isTa ? 'சதவீதம்' : 'Percentage'}</div>
          <div className="text-2xl font-bold text-amber-400">
            {result.percentage.toFixed(1)}%
          </div>
        </div>
      </div>

      {result.xpEarned !== undefined && result.xpEarned > 0 && (
        <div className="bg-amber-950/40 border border-amber-500/40 rounded-lg p-3 text-center mb-6">
          <span className="text-amber-300 font-semibold">
            🎉 +{result.xpEarned} XP {isTa ? 'பெறப்பட்டது!' : 'Earned!'}
          </span>
        </div>
      )}

      {result.questionsWithExplanations && result.questionsWithExplanations.length > 0 && (
        <div className="mb-6 space-y-4 text-left">
          <h3 className="text-lg font-semibold text-amber-300 border-b border-stone-800 pb-2">
            {isTa ? 'கேள்வி விளக்கங்கள்' : 'Question Breakdown & Explanations'}
          </h3>
          {result.questionsWithExplanations.map((q, idx) => (
            <div key={q.id} className="bg-stone-950/60 p-3 rounded border border-stone-800 text-sm">
              <p className="font-medium text-stone-200">
                {idx + 1}. {isTa && q.questionTextTa ? q.questionTextTa : q.questionText}
              </p>
              <div className="mt-2 space-y-1">
                {q.options.map(opt => (
                  <div
                    key={opt.id}
                    className={`p-2 rounded text-xs flex justify-between ${
                      opt.isCorrect
                        ? 'bg-emerald-950/60 border border-emerald-500/40 text-emerald-300'
                        : 'bg-stone-900 text-stone-400'
                    }`}
                  >
                    <span>{isTa && opt.optionTextTa ? opt.optionTextTa : opt.optionText}</span>
                    {opt.isCorrect && <span className="font-bold">✓ {isTa ? 'சரியான விடை' : 'Correct'}</span>}
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="flex gap-4 justify-center mt-6">
        {onRetry && (
          <button
            onClick={onRetry}
            className="px-5 py-2.5 rounded-lg border border-amber-600/50 text-amber-300 hover:bg-amber-950/40 transition-colors font-medium"
          >
            {isTa ? 'மீண்டும் தேர்வு எழுது' : 'Retake Quiz'}
          </button>
        )}
        {onContinue && (
          <button
            onClick={onContinue}
            className="px-5 py-2.5 rounded-lg bg-amber-600 hover:bg-amber-500 text-stone-950 font-bold transition-colors shadow-lg"
          >
            {isTa ? 'தொடர்க' : 'Continue'}
          </button>
        )}
      </div>
    </div>
  );
};
