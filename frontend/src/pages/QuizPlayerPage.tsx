import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { quizService } from '../services/quizService';
import { Quiz, AttemptResult } from '../types/quiz';
import { QuizPlayer } from '../components/quiz/QuizPlayer';
import { QuizResultSummary } from '../components/quiz/QuizResultSummary';

export const QuizPlayerPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

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
      setError(err.message || 'Failed to load quiz');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitQuiz = async (answers: { questionId: string; selectedOptionId: string }[]) => {
    if (!attempt) return;
    setLoading(true);
    try {
      const res = await quizService.submitAttempt(attempt.attemptId, { answers });
      setResult(res);
    } catch (err: any) {
      setError(err.message || 'Failed to submit quiz attempt');
    } finally {
      setLoading(false);
    }
  };

  if (loading && !quiz) {
    return (
      <div className="min-h-screen bg-stone-950 text-stone-200 flex items-center justify-center p-6">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-500 border-t-transparent" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-stone-950 text-stone-200 p-6 flex flex-col items-center justify-center">
        <div className="bg-red-950/80 border border-red-500/50 rounded-xl p-6 max-w-md text-center">
          <p className="text-red-300 font-semibold mb-4">{error}</p>
          <button
            onClick={() => navigate('/courses')}
            className="px-4 py-2 bg-stone-800 text-stone-200 rounded-lg hover:bg-stone-700"
          >
            Back to Courses
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-stone-950 text-stone-200 p-6">
      <div className="max-w-4xl mx-auto">
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
    </div>
  );
};
