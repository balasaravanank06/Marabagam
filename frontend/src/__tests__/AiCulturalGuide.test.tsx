import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AiCulturalGuidePage } from '../pages/AiCulturalGuidePage';
import { AuthProvider } from '../context/AuthContext';
import { aiService } from '../services/aiService';

vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 'u1', email: 'test@marabagam.org', fullName: 'Test User', role: 'LEARNER' },
    isAuthenticated: true,
    isLoading: false,
  }),
  AuthProvider: ({ children }: any) => <>{children}</>,
}));

vi.mock('../services/aiService', () => ({
  aiService: {
    getConversations: vi.fn(),
    getConversationById: vi.fn(),
    askCulturalGuide: vi.fn(),
    deleteConversation: vi.fn(),
  },
}));

describe('Phase 5 AI Cultural Guide UI', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    (aiService.getConversations as any).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 10,
      number: 0,
    });
  });

  it('renders AI Cultural Guide page title and quick prompts', async () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <AiCulturalGuidePage />
        </BrowserRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText(/Marabagam AI Cultural Guide/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/Welcome to AI Cultural Guide/i)).toBeInTheDocument();
    expect(screen.getByText(/What is Silambam\?/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Ask about Tamil art forms, history, stances/i)).toBeInTheDocument();
  });

  it('renders bold markdown links without literal asterisks', async () => {
    (aiService.getConversations as any).mockResolvedValue({
      content: [{ id: 'conv-1', title: 'Test Conversation', updatedAt: new Date().toISOString() }],
      totalPages: 1,
      totalElements: 1,
      size: 10,
      number: 0,
    });

    (aiService.getConversationById as any).mockResolvedValue({
      id: 'conv-1',
      title: 'Test Conversation',
      messages: [
        {
          id: 'msg-1',
          sender: 'ASSISTANT',
          content: 'Here is the course: **[Silambam Foundations Course](/courses/silambam-foundations)** (Beginner Level).\n\nAlso check **[Courses](/courses)**.',
          createdAt: new Date().toISOString(),
        },
      ],
    });

    render(
      <AuthProvider>
        <BrowserRouter>
          <AiCulturalGuidePage />
        </BrowserRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText(/Test Conversation/i)).toBeInTheDocument();
    });

    // Click conversation
    screen.getByText(/Test Conversation/i).click();

    await waitFor(() => {
      expect(screen.getByRole('link', { name: /Silambam Foundations Course/i })).toBeInTheDocument();
    });

    const link = screen.getByRole('link', { name: /Silambam Foundations Course/i });
    expect(link).toHaveAttribute('href', '/courses/silambam-foundations');
    expect(link.className).toContain('font-bold');

    // Verify there are no literal ** rendered in the message
    const messageContainer = link.closest('.max-w-\\[85\\%\\]');
    expect(messageContainer?.textContent).not.toContain('**');
    expect(messageContainer?.textContent).toContain('Silambam Foundations Course (Beginner Level)');
  });

  it('decodes HTML entities and strips escaped asterisks and standalone svg text', async () => {
    (aiService.getConversations as any).mockResolvedValue({
      content: [{ id: 'conv-2', title: 'Entity Test', updatedAt: new Date().toISOString() }],
      totalPages: 1,
      totalElements: 1,
      size: 10,
      number: 0,
    });

    (aiService.getConversationById as any).mockResolvedValue({
      id: 'conv-2',
      title: 'Entity Test',
      messages: [
        {
          id: 'msg-2',
          sender: 'ASSISTANT',
          content: 'Here is \\*\\*Escaped Bold\\*\\* with entity &#x45;xample &amp; &#39;quotes&#39;.\nsvg\n<svg>stray</svg>\n- List item with **[Course Link](/courses/test)**',
          createdAt: new Date().toISOString(),
        },
      ],
    });

    render(
      <AuthProvider>
        <BrowserRouter>
          <AiCulturalGuidePage />
        </BrowserRouter>
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByText(/Entity Test/i)).toBeInTheDocument();
    });

    screen.getByText(/Entity Test/i).click();

    await waitFor(() => {
      expect(screen.getByText(/Escaped Bold/i)).toBeInTheDocument();
    });

    const textEl = screen.getByText(/Escaped Bold/i);
    const container = textEl.closest('.max-w-\\[85\\%\\]');
    const fullText = container?.textContent || '';

    // Must decode &#x45; to E
    expect(fullText).toContain('Example');
    expect(fullText).not.toContain('&#x45;');

    // Must decode &#39; to '
    expect(fullText).toContain("'quotes'");
    expect(fullText).not.toContain('&#39;');

    // Must decode &amp; to &
    expect(fullText).toContain('&');
    expect(fullText).not.toContain('&amp;');

    // Must not contain escaped \*\* or **
    expect(fullText).not.toContain('\\*\\*');
    expect(fullText).not.toContain('**');

    // Standalone "svg" or "<svg>stray</svg>" line must be filtered
    expect(fullText).not.toContain('<svg>');
    expect(fullText).not.toMatch(/\bsvg\b/);
  });
});
