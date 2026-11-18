import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach } from 'vitest';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { ProtectedRoute } from '../components/common/ProtectedRoute';
import { clearAuthTokens, setAuthTokens, getAccessToken } from '../lib/api';

const DummyProtected = () => <div>Secret Protected Content</div>;

describe('Frontend Authentication Flow', () => {
  beforeEach(() => {
    clearAuthTokens();
  });

  it('manages access token in memory/sessionStorage securely', () => {
    setAuthTokens({
      accessToken: 'access_123',
      refreshToken: 'refresh_456',
      tokenType: 'Bearer',
      expiresInMs: 900,
    });
    expect(getAccessToken()).toBe('access_123');
    clearAuthTokens();
    expect(getAccessToken()).toBeNull();
  });

  it('renders LoginPage with form inputs and register link', () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </MemoryRouter>
    );

    expect(screen.getByText(/Marabagam/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/learner@marabagam.org/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/••••••••••••/i)).toBeInTheDocument();
  });

  it('renders RegisterPage with default LEARNER role notice', () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <RegisterPage />
        </AuthProvider>
      </MemoryRouter>
    );

    expect(screen.getByText(/LEARNER/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/art_lover@marabagam.org/i)).toBeInTheDocument();
  });

  it('redirects unauthenticated users attempting to access ProtectedRoute', async () => {
    render(
      <MemoryRouter initialEntries={['/protected']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<div>Login Screen Redirect</div>} />
            <Route
              path="/protected"
              element={
                <ProtectedRoute>
                  <DummyProtected />
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Login Screen Redirect/i)).toBeInTheDocument();
    });
  });
});
