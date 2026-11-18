import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { CourseDirectoryPage } from '../pages/CourseDirectoryPage';
import { AuthProvider } from '../context/AuthContext';
import { learningService } from '../services/learningService';
import { artService } from '../services/artService';

vi.mock('../services/learningService', () => ({
  learningService: {
    getPublicCourses: vi.fn(),
  },
}));

vi.mock('../services/artService', () => ({
  artService: {
    getArtForms: vi.fn(),
    getPublishedArtForms: vi.fn(),
  },
}));

describe('Phase 4 Course Directory & E-Learning UI', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    (artService.getPublishedArtForms as any).mockResolvedValue({
      content: [
        { id: 'af-1', name: 'Silambam', nameTa: 'சிலம்பம்', slug: 'silambam' },
      ],
    });

    (learningService.getPublicCourses as any).mockResolvedValue({
      content: [
        {
          id: 'course-1',
          title: 'Silambam Mastery',
          titleTa: 'சிலம்ப கலை',
          slug: 'silambam-mastery',
          shortDescription: 'Master ancient staff fencing',
          shortDescriptionTa: 'பண்டைய கலை',
          difficulty: 'BEGINNER',
          estimatedDurationMinutes: 120,
          status: 'PUBLISHED',
          artForm: { id: 'af-1', name: 'Silambam', nameTa: 'சிலம்பம்', slug: 'silambam' },
          createdAt: new Date().toISOString(),
        },
      ],
      totalPages: 1,
      totalElements: 1,
      size: 9,
      number: 0,
    });
  });

  it('renders Course Directory page title and search controls', async () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <CourseDirectoryPage />
        </BrowserRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText(/Tamil Heritage Art Courses/i)).toBeInTheDocument();
    });

    expect(screen.getByPlaceholderText(/Search courses by title or description/i)).toBeInTheDocument();
  });

  it('displays published course cards properly', async () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <CourseDirectoryPage />
        </BrowserRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Silambam Mastery')).toBeInTheDocument();
    });

    expect(screen.getByText('BEGINNER')).toBeInTheDocument();
    expect(screen.getByText(/120 mins/i)).toBeInTheDocument();
  });
});
