import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { CertificatesPage } from '../pages/CertificatesPage';
import { AchievementsPage } from '../pages/AchievementsPage';
import { AuthProvider } from '../context/AuthContext';
import { certificateService } from '../services/certificateService';
import { gamificationService } from '../services/gamificationService';

vi.mock('../services/certificateService', () => ({
  certificateService: {
    getUserCertificates: vi.fn(),
    getCertificateById: vi.fn(),
    verifyCertificate: vi.fn(),
  },
}));

vi.mock('../services/gamificationService', () => ({
  gamificationService: {
    getProfile: vi.fn(),
    getLeaderboard: vi.fn(),
    togglePrivacy: vi.fn(),
  },
}));

describe('CertificatesPage Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders exact friendly empty state when user has no certificates', async () => {
    (certificateService.getUserCertificates as any).mockResolvedValue([]);

    render(
      <AuthProvider>
        <BrowserRouter>
          <CertificatesPage />
        </BrowserRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Your learning journey starts here.')).toBeInTheDocument();
    });

    expect(
      screen.getByText(/You haven't earned any certificates yet\. Complete a course and its required assessments to earn your first certificate\./i)
    ).toBeInTheDocument();

    const courseDirLinks = screen.getAllByRole('link', { name: /Course Directory|Explore Course Directory|Explore Courses/i });
    expect(courseDirLinks.length).toBeGreaterThanOrEqual(1);
  });

  it('renders certificate records when user has earned certificates', async () => {
    const mockCerts = [
      {
        id: 'cert-001',
        certificateCode: 'MBG-CERT-2026-A1B2C3D4',
        courseId: 'course-silambam',
        recipientNameSnapshot: 'Anbu Heritage Learner',
        courseTitleSnapshot: 'Silambam Foundations Masterclass',
        courseTitleTaSnapshot: 'சிலம்பம் அடிப்படைக் கற்கை',
        finalScorePercentage: 94.0,
        issuedAt: '2026-09-10T12:00:00Z',
        signatureHmac: 'mock-hmac-signature',
        keyVersion: 1,
        status: 'ISSUED' as const,
        verificationUrl: 'http://localhost:5173/certificate/MBG-CERT-2026-A1B2C3D4/verify'
      }
    ];

    (certificateService.getUserCertificates as any).mockResolvedValue(mockCerts);

    render(
      <AuthProvider>
        <BrowserRouter>
          <CertificatesPage />
        </BrowserRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getAllByText('Silambam Foundations Masterclass').length).toBeGreaterThanOrEqual(1);
    });

    expect(screen.getAllByText('Anbu Heritage Learner').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText(/ID: MBG-CERT-2026-A1B2C3D4/i)).toBeInTheDocument();
    expect(screen.getByText('Verified')).toBeInTheDocument();
    expect(screen.getByText(/Score: 94.0%/i)).toBeInTheDocument();
  });

  it('renders retryable error state on genuine server failure', async () => {
    (certificateService.getUserCertificates as any).mockRejectedValueOnce(
      new Error('Network connection timed out')
    );

    render(
      <AuthProvider>
        <BrowserRouter>
          <CertificatesPage />
        </BrowserRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Failed to fetch certificates')).toBeInTheDocument();
    });

    const retryBtn = screen.getByRole('button', { name: /Retry/i });
    expect(retryBtn).toBeInTheDocument();

    // Clicking retry calls API again
    (certificateService.getUserCertificates as any).mockResolvedValueOnce([]);
    fireEvent.click(retryBtn);

    await waitFor(() => {
      expect(screen.getByText('Your learning journey starts here.')).toBeInTheDocument();
    });
  });
});

describe('AchievementsPage Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders valid starting values and guidance for a new user with 0 XP', async () => {
    (gamificationService.getProfile as any).mockResolvedValue({
      totalXp: 0,
      level: 1,
      levelTitleEn: 'Novice Student',
      levelTitleTa: 'ஆரம்பநிலை மாணவர்',
      currentStreak: 0,
      longestStreak: 0,
      totalBadges: 0,
      badges: [
        {
          id: 'b1',
          code: 'FIRST_STEP',
          nameEn: 'First Step',
          nameTa: 'முதல் படி',
          descriptionEn: 'Completed your first traditional art lesson.',
          descriptionTa: 'முதல் பாரம்பரியப் பாடத்தை நிறைவு செய்துள்ளீர்கள்.',
          iconUrl: 'https://assets.marabagam.org/badges/first-step.png',
          category: 'LEARNING',
          earned: false,
          awardedAt: null
        }
      ]
    });

    render(
      <AuthProvider>
        <BrowserRouter>
          <AchievementsPage />
        </BrowserRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getAllByText(/Level 1/i).length).toBeGreaterThanOrEqual(1);
    });

    expect(screen.getByText(/Novice Student/i)).toBeInTheDocument();
    expect(screen.getByText(/Welcome, Heritage Learner! Start Earning XP/i)).toBeInTheDocument();
    expect(screen.getByText(/Complete Lessons/i)).toBeInTheDocument();
    expect(screen.getByText(/Pass Quizzes/i)).toBeInTheDocument();
  });

  it('renders retryable error state on server failure for gamification profile', async () => {
    (gamificationService.getProfile as any).mockRejectedValueOnce(
      new Error('Internal server error')
    );

    render(
      <AuthProvider>
        <BrowserRouter>
          <AchievementsPage />
        </BrowserRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Failed to fetch gamification profile')).toBeInTheDocument();
    });

    const retryBtn = screen.getByRole('button', { name: /Retry/i });
    expect(retryBtn).toBeInTheDocument();
  });
});
