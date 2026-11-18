import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { EventCard } from '../components/EventCard';
import { RecommendationCard } from '../components/RecommendationCard';
import { PostCard } from '../components/PostCard';
import { CulturalEvent } from '../types/event';
import { Recommendation } from '../types/recommendation';
import { CommunityPost } from '../types/community';
import { exportEventToIcs } from '../utils/icsExporter';
import { BrowserRouter } from 'react-router-dom';

const mockEvent: CulturalEvent = {
  id: 'event-123',
  title: 'Karagattam Masterclass',
  titleTa: 'கரகாட்டம் மாஸ்டர்கிளாஸ்',
  slug: 'karagattam-masterclass',
  description: 'Master Karagattam dance',
  descriptionTa: 'கரகாட்ட நடனத்தைக் கற்றுக்கொள்ளுங்கள்',
  eventType: 'WORKSHOP',
  eventPeriod: 'October 2026',
  eventPeriodTa: 'அக்டோபர் 2026',
  isDateApproximate: false,
  status: 'PUBLISHED',
  capacity: 25,
  registeredCount: 5,
  isOnline: true,
  meetingUrl: 'https://meet.marabagam.com/test',
  isUserRegistered: false,
  startAt: '2026-10-15T10:00:00Z',
  endAt: '2026-10-15T12:00:00Z',
  timezoneIdentifier: 'Asia/Kolkata',
  districtName: 'Madurai',
  createdAt: '2026-09-09T10:00:00Z'
};

const mockRecommendation: Recommendation = {
  id: 'rec-1',
  rank: 1,
  itemType: 'COURSE',
  itemId: 'course-1',
  title: 'Silambam Fundamentals',
  titleTa: 'சிலம்பம் அடிப்படை',
  targetUrl: '/course/silambam-fundamentals',
  description: 'Master ancient staff fighting techniques.',
  descriptionTa: 'பண்டைய சிலம்பக் கலை முறைகளைக் கற்றுக்கொள்ளுங்கள்.',
  reason: 'Based on your recent interest in martial arts',
  reasonTa: 'தற்காப்புக் கலைகள் பற்றிய உங்கள் ஆர்வத்தின் அடிப்படையில்'
};

const mockPost: CommunityPost = {
  id: 'post-1',
  authorDisplayName: 'Anand Kumar',
  authorRole: 'LEARNER',
  category: 'DISCUSSION',
  title: 'History of Parai Attam',
  content: 'Parai Attam is one of the oldest traditional drum performances in Tamil Nadu.',
  status: 'VISIBLE',
  commentCount: 3,
  createdAt: '2026-09-09T10:00:00Z',
  updatedAt: '2026-09-09T10:00:00Z'
};

describe('Phase 9 Frontend Components & Export Utilities', () => {
  it('renders EventCard with capacity and online badges', () => {
    render(
      <BrowserRouter>
        <EventCard event={mockEvent} />
      </BrowserRouter>
    );

    expect(screen.getByText('Karagattam Masterclass')).toBeInTheDocument();
    expect(screen.getByText('Online')).toBeInTheDocument();
    expect(screen.getByText(/Registered/i)).toBeInTheDocument();

  });

  it('renders RecommendationCard with explanation reason', () => {
    render(
      <BrowserRouter>
        <RecommendationCard recommendation={mockRecommendation} isTamil={false} />
      </BrowserRouter>
    );

    expect(screen.getByText('Silambam Fundamentals')).toBeInTheDocument();
    expect(screen.getByText('Based on your recent interest in martial arts')).toBeInTheDocument();
  });

  it('renders PostCard with title and author name', () => {
    render(
      <BrowserRouter>
        <PostCard post={mockPost} />
      </BrowserRouter>
    );

    expect(screen.getByText('History of Parai Attam')).toBeInTheDocument();
    expect(screen.getByText('Anand Kumar')).toBeInTheDocument();
    expect(screen.getByText(/Replies/i)).toBeInTheDocument();
  });

  it('exports client-side .ics without throwing errors', () => {
    window.URL.createObjectURL = vi.fn().mockReturnValue('blob:mock-url');
    expect(() => exportEventToIcs(mockEvent)).not.toThrow();
  });
});
