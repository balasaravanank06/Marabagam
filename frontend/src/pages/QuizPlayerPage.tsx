import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { quizService } from '../services/quizService';
import { Quiz, AttemptResult } from '../types/quiz';
import { QuizPlayer } from '../components/quiz/QuizPlayer';
import { QuizResultSummary } from '../components/quiz/QuizResultSummary';

export const QuizPlayerPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const [quiz, setQuiz] = useState<Quiz | null>(null);
  const [attempt, setAttempt] = useState<AttemptResult | null>(null);
  const [result, setResult] = useState<AttemptResult | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    loadQuizAndStart();
  }, [id]);

  const loadQuizAndStart = async () => {
    setLoading(true);
    setError(null);
    try {
      const q = await quizService.getQuiz(id!);
      setQuiz(q);
      const att = await quizService.startAttempt(id!);
      setAttempt(att);
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
        err?.message ||
        (isTa ? 'வினாடி வினாவை ஏற்றுவதில் தோல்வி' : 'Failed to load quiz attempt')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitQuiz = async (answers: { questionId: string; selectedOptionId: string }[]) => {
    if (!attempt) return;
    setLoading(true);
    setError(null);
    try {
      const res = await quizService.submitAttempt(attempt.attemptId, { answers });
      setResult(res);
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
        err?.message ||
        (isTa ? 'விடைகளைச் சமர்ப்பிப்பதில் தோல்வி' : 'Failed to submit quiz attempt')
      );
    } finally {
      setLoading(false);
    }
  };

  if (loading && !quiz) {
    return (
      <div className="min-h-[50vh] flex flex-col items-center justify-center space-y-4">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-500 border-t-transparent" />
        <p className="text-sm text-slate-400">
          {isTa ? 'வினாடி வினா ஏற்றப்படுகிறது...' : 'Preparing your assessment...'}
        </p>
      </div>
    );
  }

  if (error && !result) {
    return (
      <div className="max-w-md mx-auto my-12 bg-rose-950/60 border border-rose-500/50 rounded-2xl p-6 text-center space-y-4 shadow-xl">
        <div className="w-12 h-12 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center mx-auto">
          <AlertCircle className="w-6 h-6" />
        </div>
        <div>
          <h3 className="text-base font-bold text-rose-200">
            {isTa ? 'வினாடி வினாப் பிழை' : 'Quiz System Error'}
          </h3>
          <p className="text-xs text-rose-300/80 mt-1">{error}</p>
        </div>
        <div className="flex justify-center gap-3 pt-2">
          <button
            onClick={loadQuizAndStart}
            className="px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white rounded-xl text-xs font-bold transition flex items-center space-x-1.5"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>{isTa ? 'மீண்டும் முயற்சிக்கவும்' : 'Retry'}</span>
          </button>
          <Link
            to="/courses"
            className="px-4 py-2 bg-slate-800 text-slate-300 hover:bg-slate-700 rounded-xl text-xs font-semibold transition"
          >
            {isTa ? 'பாடங்களுக்குத் திரும்பு' : 'Back to Courses'}
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {result ? (
        <QuizResultSummary
          result={result}
          onRetry={() => {
            setResult(null);
            loadQuizAndStart();
          }}
          onContinue={() => navigate('/my-learning')}
        />
      ) : quiz && attempt ? (
        <QuizPlayer
          quiz={quiz}
          attemptId={attempt.attemptId}
          onSubmit={handleSubmitQuiz}
        />
      ) : null}
    </div>
  );
};
