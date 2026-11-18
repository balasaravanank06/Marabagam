import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { ArtFormDirectoryPage } from '../pages/ArtFormDirectoryPage';
import { AdminArtFormsPage } from '../pages/AdminArtFormsPage';
import { ProtectedRoute } from '../components/common/ProtectedRoute';

describe('Phase 3 Art Form Directory & Governance', () => {
  it('renders Art Form Directory page heading and search controls', async () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <ArtFormDirectoryPage />
        </AuthProvider>
      </MemoryRouter>
    );

    expect(screen.getByText(/Traditional Tamil Arts Directory/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Search art forms, styles, instruments/i)).toBeInTheDocument();
  });

  it('redirects unauthenticated users attempting to access admin art forms page', async () => {
    render(
      <MemoryRouter initialEntries={['/admin/art-forms']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<div>Login Required View</div>} />
            <Route
              path="/admin/art-forms"
              element={
                <ProtectedRoute allowedRoles={['ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN']}>
                  <AdminArtFormsPage />
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Login Required View/i)).toBeInTheDocument();
    });
  });
});
