export interface Landmark {
  x: number;
  y: number;
  z?: number;
  visibility?: number;
  presence?: number;
}

export interface FrameLandmark {
  frameIndex?: number;
  timestampMs?: number;
  landmarks: Landmark[];
}

export interface EvaluatePracticeSessionRequest {
  frames: FrameLandmark[];
}

export interface PracticeFeedback {
  id: string;
  bodyPart: string;
  metricName: string;
  measuredValue: number;
  targetRangeMin: number;
  targetRangeMax: number;
  score: number;
  feedbackTextEn: string;
  feedbackTextTa: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
}

export interface PracticeSession {
  id: string;
  userId: string;
  movementId: string;
  movementNameEn: string;
  movementNameTa: string;
  versionNumber: string;
  startedAt: string;
  completedAt?: string;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'FAILED' | 'ABORTED';
  overallScore?: number;
  durationSeconds?: number;
  feedbacks?: PracticeFeedback[];
  createdAt: string;
}

export interface Movement {
  id: string;
  artFormId: string;
  artFormName?: string;
  code: string;
  nameEn: string;
  nameTa: string;
  descriptionEn?: string;
  descriptionTa?: string;
  category: string;
  difficultyLevel: string;
  isActive: boolean;
  currentVersionNumber?: string;
  demonstrationVideoUrl?: string;
  thumbnailUrl?: string;
  createdAt: string;
}
