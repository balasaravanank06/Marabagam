export type QuizScope = 'COURSE' | 'MODULE' | 'LESSON';
export type QuestionType = 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'IMAGE_IDENTIFICATION';
export type QuizStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'PUBLISHED' | 'ARCHIVED';
export type AttemptStatus = 'STARTED' | 'SUBMITTED' | 'TIMED_OUT' | 'ABANDONED';

export interface QuestionOption {
  id: string;
  optionText: string;
  optionTextTa: string;
  displayOrder: number;
  isCorrect?: boolean;
  explanationEn?: string;
  explanationTa?: string;
}

export interface QuizQuestion {
  id: string;
  questionText: string;
  questionTextTa: string;
  questionType: QuestionType;
  mediaId?: string;
  mediaUrl?: string;
  points: number;
  displayOrder: number;
  options: QuestionOption[];
}

export interface Quiz {
  id: string;
  scope: QuizScope;
  courseId?: string;
  moduleId?: string;
  lessonId?: string;
  title: string;
  titleTa: string;
  description?: string;
  descriptionTa?: string;
  passPercentage: number;
  maxAttempts: number;
  timeLimitMinutes?: number;
  mandatory: boolean;
  status: QuizStatus;
  questions?: QuizQuestion[];
}

export interface SubmitAnswerPayload {
  questionId: string;
  selectedOptionId: string;
}

export interface QuizSubmissionPayload {
  answers: SubmitAnswerPayload[];
}

export interface AttemptResult {
  attemptId: string;
  quizId: string;
  attemptNumber: number;
  scoreEarned: number;
  totalPossibleScore: number;
  percentage: number;
  passed: boolean;
  status: AttemptStatus;
  startedAt: string;
  submittedAt?: string;
  xpEarned?: number;
  questionsWithExplanations?: QuizQuestion[];
}
