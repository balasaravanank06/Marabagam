import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { QuizPlayer } from '../components/quiz/QuizPlayer';
import { QuizResultSummary } from '../components/quiz/QuizResultSummary';
import { Quiz, AttemptResult } from '../types/quiz';

describe('Quiz System Components', () => {
  const mockQuiz: Quiz = {
    id: 'quiz-1',
    scope: 'COURSE',
    title: 'Silambam Fundamentals Quiz',
    titleTa: 'சிலம்பம் அடிப்படை தேர்வு',
    description: 'Test your understanding of basic Silambam stances',
    passPercentage: 80,
    maxAttempts: 3,
    timeLimitMinutes: 15,
    mandatory: true,
    status: 'PUBLISHED',
    questions: [
      {
        id: 'q1',
        questionText: 'What is the primary footwork stance in Silambam?',
        questionTextTa: 'சிலம்பத்தில் முதன்மை கால்வைப்பு முறை எது?',
        questionType: 'MULTIPLE_CHOICE',
        points: 10,
        displayOrder: 1,
        options: [
          { id: 'o1', optionText: 'Subadu Stance', optionTextTa: 'சுவடு நிலை', displayOrder: 1 },
          { id: 'o2', optionText: 'Karate Stance', optionTextTa: 'கராத்தே நிலை', displayOrder: 2 }
        ]
      }
    ]
  };

  it('renders QuizPlayer with questions and options', () => {
    const onSubmit = vi.fn();
    render(<QuizPlayer quiz={mockQuiz} attemptId="att-1" onSubmit={onSubmit} />);

    expect(screen.getByText(/Silambam Fundamentals Quiz/i)).toBeInTheDocument();
    expect(screen.getByText(/What is the primary footwork stance in Silambam\?/i)).toBeInTheDocument();
    expect(screen.getByText(/Subadu Stance/i)).toBeInTheDocument();

    const optionBtn = screen.getByRole('radio', { name: /Subadu Stance/i });
    fireEvent.click(optionBtn);

    const submitBtn = screen.getByText(/Submit Quiz/i);
    fireEvent.click(submitBtn);

    expect(onSubmit).toHaveBeenCalledWith([{ questionId: 'q1', selectedOptionId: 'o1' }]);
  });

  it('renders QuizResultSummary with passed status and XP award', () => {
    const mockResult: AttemptResult = {
      attemptId: 'att-1',
      quizId: 'quiz-1',
      attemptNumber: 1,
      scoreEarned: 10,
      totalPossibleScore: 10,
      percentage: 100.0,
      passed: true,
      status: 'SUBMITTED',
      startedAt: new Date().toISOString(),
      xpEarned: 50
    };

    render(<QuizResultSummary result={mockResult} />);

    expect(screen.getByText(/Quiz Passed!/i)).toBeInTheDocument();
    expect(screen.getByText(/100.0%/i)).toBeInTheDocument();
    expect(screen.getByText(/\+50 XP Earned!/i)).toBeInTheDocument();
  });
});
