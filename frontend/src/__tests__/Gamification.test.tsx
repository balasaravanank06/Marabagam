import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { XpProgressBar } from '../components/gamification/XpProgressBar';
import { StreakWidget } from '../components/gamification/StreakWidget';
import { BadgeCard } from '../components/gamification/BadgeCard';
import { LeaderboardTable } from '../components/gamification/LeaderboardTable';
import { Badge, LeaderboardEntry } from '../types/gamification';

describe('Gamification Components', () => {
  it('renders XpProgressBar with level and XP calculations', () => {
    render(
      <XpProgressBar
        totalXp={250}
        level={2}
        levelTitleEn="Apprentice Practitioner"
        levelTitleTa="பயிற்சியாளர்"
      />
    );

    expect(screen.getByText(/Level 2/i)).toBeInTheDocument();
    expect(screen.getByText(/Apprentice Practitioner/i)).toBeInTheDocument();
    expect(screen.getAllByText(/250/i)[0]).toBeInTheDocument();
  });

  it('renders StreakWidget with active days', () => {
    render(<StreakWidget currentStreak={7} longestStreak={14} />);

    expect(screen.getByText(/7 Days/i)).toBeInTheDocument();
    expect(screen.getByText(/14 Days/i)).toBeInTheDocument();
  });

  it('renders BadgeCard in earned state', () => {
    const mockBadge: Badge = {
      id: 'b1',
      code: 'FIRST_STEP',
      nameEn: 'First Step',
      nameTa: 'முதல் அடி',
      descriptionEn: 'Completed your first lesson',
      descriptionTa: 'முதல் பாடத்தை முடித்துள்ளீர்கள்',
      iconUrl: '👣',
      category: 'PROGRESSION',
      earned: true,
      awardedAt: new Date().toISOString()
    };

    render(<BadgeCard badge={mockBadge} />);

    expect(screen.getByText(/First Step/i)).toBeInTheDocument();
    expect(screen.getByText(/Earned/i)).toBeInTheDocument();
  });

  it('renders LeaderboardTable with entries and type switcher', () => {
    const mockEntries: LeaderboardEntry[] = [
      { rank: 1, displayName: 'Kovalan', xp: 1200, level: 4, badgesCount: 5 },
      { rank: 2, displayName: 'Kannagi', xp: 950, level: 3, badgesCount: 3 }
    ];

    const onTypeChange = vi.fn();

    render(
      <LeaderboardTable
        entries={mockEntries}
        type="global"
        onTypeChange={onTypeChange}
      />
    );

    expect(screen.getByText(/Kovalan/i)).toBeInTheDocument();
    expect(screen.getByText(/1,200 XP/i)).toBeInTheDocument();

    const weeklyBtn = screen.getByText(/This Week/i);
    fireEvent.click(weeklyBtn);
    expect(onTypeChange).toHaveBeenCalledWith('weekly');
  });
});
