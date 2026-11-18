import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AiCulturalGuidePage } from '../pages/AiCulturalGuidePage';
import { AuthProvider } from '../context/AuthContext';
import { aiService } from '../services/aiService';

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
});
