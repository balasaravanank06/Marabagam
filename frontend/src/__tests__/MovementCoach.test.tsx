import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { CameraConsentModal } from '../components/movement/CameraConsentModal';
import { CameraCalibration } from '../components/movement/CameraCalibration';
import { FeedbackDashboard } from '../components/movement/FeedbackDashboard';
import { PracticeSession } from '../types/movement';

// Mock i18next
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string, fallback?: string) => fallback || key,
    i18n: { language: 'en' }
  })
}));

describe('Movement Coach Components', () => {
  it('renders CameraConsentModal with privacy explanation and buttons', () => {
    const onAccept = vi.fn();
    const onCancel = vi.fn();

    render(
      <CameraConsentModal isOpen={true} onAccept={onAccept} onCancel={onCancel} />
    );

    expect(screen.getByText(/AI Movement Coach — Camera Privacy Notice/i)).toBeInTheDocument();
    expect(screen.getByText(/On-Device Frame Processing/i)).toBeInTheDocument();

    const acceptBtn = screen.getByText(/Enable Practice Camera/i);
    fireEvent.click(acceptBtn);
    expect(onAccept).toHaveBeenCalledTimes(1);
  });

  it('renders CameraCalibration in aligned state', () => {
    const onProceed = vi.fn();

    render(
      <CameraCalibration
        isCalibrated={true}
        message="Learner in frame. Ready to practice."
        onProceed={onProceed}
      />
    );

    expect(screen.getByText(/Learner in frame. Ready to practice./i)).toBeInTheDocument();
    const proceedBtn = screen.getByText(/Begin Movement Practice/i);
    expect(proceedBtn).not.toBeDisabled();
    fireEvent.click(proceedBtn);
    expect(onProceed).toHaveBeenCalledTimes(1);
  });

  it('renders FeedbackDashboard with overall stance score and disclaimer', () => {
    const mockSession: PracticeSession = {
      id: 'session-123',
      userId: 'user-1',
      movementId: 'move-1',
      movementNameEn: 'Maankombu Subadu',
      movementNameTa: 'மான்கொம்பு சுவடு',
      versionNumber: 'v1.0.0',
      startedAt: new Date().toISOString(),
      status: 'COMPLETED',
      overallScore: 88.5,
      feedbacks: [
        {
          id: 'fb-1',
          bodyPart: 'KNEE',
          metricName: 'KNEE_FLEXION',
          measuredValue: 125.0,
          targetRangeMin: 115.0,
          targetRangeMax: 140.0,
          score: 95.0,
          feedbackTextEn: 'Knee flex angle matches reference stance target.',
          feedbackTextTa: 'முழங்கால் வளைவு சரியானது.',
          severity: 'INFO'
        }
      ],
      createdAt: new Date().toISOString()
    };

    render(
      <FeedbackDashboard session={mockSession} onRetry={vi.fn()} />
    );

    expect(screen.getByText(/88.5%/i)).toBeInTheDocument();
    expect(screen.getByText(/Educational Disclaimer/i)).toBeInTheDocument();
    expect(screen.getByText(/Knee flex angle matches reference stance target./i)).toBeInTheDocument();
  });
});
