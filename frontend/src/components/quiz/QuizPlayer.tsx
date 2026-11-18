import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Quiz, SubmitAnswerPayload } from '../../types/quiz';
import { Clock, HelpCircle, CheckCircle } from 'lucide-react';

interface QuizPlayerProps {
  quiz: Quiz;
  attemptId?: string;
  timeLimitMinutes?: number;
  onSubmit: (answers: SubmitAnswerPayload[]) => void;
  isSubmitting?: boolean;
}

export const QuizPlayer: React.FC<QuizPlayerProps> = ({
  quiz,
  timeLimitMinutes,
  onSubmit,
  isSubmitting = false
}) => {
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const questions = quiz.questions || [];
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedAnswers, setSelectedAnswers] = useState<Record<string, string>>({});
  const [secondsRemaining, setSecondsRemaining] = useState<number | null>(
    timeLimitMinutes ? timeLimitMinutes * 60 : null
  );

  useEffect(() => {
    if (secondsRemaining === null) return;
    if (secondsRemaining <= 0) {
      handleFinalSubmit();
      return;
    }
    const timer = setInterval(() => {
      setSecondsRemaining((prev) => (prev !== null && prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, [secondsRemaining]);

  const currentQuestion = questions[currentQuestionIndex];

  const handleOptionSelect = (questionId: string, optionId: string) => {
    setSelectedAnswers((prev) => ({
      ...prev,
      [questionId]: optionId
    }));
  };

  const handleFinalSubmit = () => {
    const payload: SubmitAnswerPayload[] = Object.entries(selectedAnswers).map(
      ([qId, optId]) => ({
        questionId: qId,
        selectedOptionId: optId
      })
    );
    onSubmit(payload);
  };

  const formatTime = (secs: number) => {
    const mins = Math.floor(secs / 60);
    const remainder = secs % 60;
    return `${mins.toString().padStart(2, '0')}:${remainder.toString().padStart(2, '0')}`;
  };

  if (!currentQuestion) {
    return <div className="p-6 text-center text-amber-200">No questions available in this quiz.</div>;
  }

  return (
    <div className="bg-amber-950/80 border border-amber-500/30 rounded-xl p-6 shadow-2xl backdrop-blur-md max-w-3xl mx-auto text-amber-50">
      {/* Header Info */}
      <div className="flex flex-wrap items-center justify-between border-b border-amber-500/20 pb-4 mb-6 gap-4">
        <div>
          <h2 className="text-xl font-bold text-amber-200">
            {isTa ? quiz.titleTa : quiz.title}
          </h2>
          <p className="text-sm text-amber-400">
            {isTa ? 'கேள்வி' : 'Question'} {currentQuestionIndex + 1} / {questions.length}
          </p>
        </div>

        {secondsRemaining !== null && (
          <div className="flex items-center gap-2 bg-amber-900/60 px-3 py-1.5 rounded-lg border border-amber-500/40 text-amber-300 font-mono font-semibold">
            <Clock className="w-4 h-4 text-amber-400" />
            <span>{formatTime(secondsRemaining)}</span>
          </div>
        )}
      </div>

      {/* Progress Bar */}
      <div className="w-full bg-amber-950/50 rounded-full h-2 mb-6 overflow-hidden border border-amber-500/20">
        <div
          className="bg-gradient-to-r from-amber-500 to-orange-400 h-full transition-all duration-300"
          style={{ width: `${((currentQuestionIndex + 1) / questions.length) * 100}%` }}
        />
      </div>

      {/* Question Media if IMAGE_IDENTIFICATION */}
      {currentQuestion.mediaUrl && (
        <div className="mb-6 rounded-lg overflow-hidden border border-amber-500/30 max-h-64 flex justify-center bg-black/40">
          <img
            src={currentQuestion.mediaUrl}
            alt={isTa ? currentQuestion.questionTextTa : currentQuestion.questionText}
            className="object-contain max-h-64"
          />
        </div>
      )}

      {/* Question Text */}
      <div className="mb-6">
        <h3 className="text-lg font-semibold text-amber-100 flex items-start gap-3">
          <HelpCircle className="w-6 h-6 text-amber-400 flex-shrink-0 mt-0.5" />
          <span>{isTa ? currentQuestion.questionTextTa : currentQuestion.questionText}</span>
        </h3>
      </div>

      {/* Choice Options */}
      <div className="space-y-3 mb-8" role="radiogroup" aria-label="Question choices">
        {currentQuestion.options.map((option) => {
          const isSelected = selectedAnswers[currentQuestion.id] === option.id;
          return (
            <button
              key={option.id}
              type="button"
              onClick={() => handleOptionSelect(currentQuestion.id, option.id)}
              className={`w-full text-left p-4 rounded-xl border transition-all flex items-center justify-between ${
                isSelected
                  ? 'bg-amber-600/30 border-amber-400 text-white ring-2 ring-amber-400/50'
                  : 'bg-amber-900/30 border-amber-500/20 text-amber-200 hover:bg-amber-800/30 hover:border-amber-500/40'
              }`}
              role="radio"
              aria-checked={isSelected}
            >
              <span className="font-medium">
                {isTa ? option.optionTextTa : option.optionText}
              </span>
              {isSelected && <CheckCircle className="w-5 h-5 text-amber-400 flex-shrink-0" />}
            </button>
          );
        })}
      </div>

      {/* Navigation & Submit Controls */}
      <div className="flex items-center justify-between border-t border-amber-500/20 pt-4">
        <button
          type="button"
          onClick={() => setCurrentQuestionIndex((prev) => Math.max(0, prev - 1))}
          disabled={currentQuestionIndex === 0}
          className="px-4 py-2 rounded-lg border border-amber-500/30 text-amber-300 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-amber-900/40 transition-colors"
        >
          {isTa ? 'முந்தையது' : 'Previous'}
        </button>

        {currentQuestionIndex < questions.length - 1 ? (
          <button
            type="button"
            onClick={() => setCurrentQuestionIndex((prev) => Math.min(questions.length - 1, prev + 1))}
            className="px-6 py-2 rounded-lg bg-amber-600 hover:bg-amber-500 text-amber-950 font-bold transition-colors shadow-lg"
          >
            {isTa ? 'அடுத்தது' : 'Next'}
          </button>
        ) : (
          <button
            type="button"
            onClick={handleFinalSubmit}
            disabled={isSubmitting}
            className="px-6 py-2 rounded-lg bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-slate-950 font-bold transition-all shadow-lg shadow-emerald-950/50 disabled:opacity-50"
          >
            {isSubmitting
              ? (isTa ? 'சமர்ப்பிக்கப்படுகிறது...' : 'Submitting...')
              : (isTa ? 'சமர்ப்பி' : 'Submit Quiz')}
          </button>
        )}
      </div>
    </div>
  );
};
