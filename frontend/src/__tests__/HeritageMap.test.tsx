import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { HeritageMapPage } from '../pages/HeritageMapPage';
import { heritageMapService } from '../services/heritageMapService';

vi.mock('../services/heritageMapService', () => ({
  heritageMapService: {
    getHeritageMapSummary: vi.fn(),
    getDistrictDetail: vi.fn(),
  },
}));

describe('Phase 6 Regional Heritage Mapping & Geographic Discovery', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (heritageMapService.getHeritageMapSummary as any).mockResolvedValue({
      state: 'Tamil Nadu',
      totalDistricts: 38,
      totalPublishedArtForms: 5,
      totalPublishedLocations: 2,
      totalPublishedEvents: 2,
      districts: [
        {
          id: 1,
          name: 'Madurai',
          nameTa: 'மதுரை',
          code: 'TN-MAD',
          latitude: 9.9252,
          longitude: 78.1198,
          artFormCount: 2,
          locationCount: 1,
          eventCount: 1,
        },
      ],
    });

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        type: 'FeatureCollection',
        features: [
          {
            type: 'Feature',
            properties: { code: 'TN-MAD', name: 'Madurai', nameTa: 'மதுரை' },
            geometry: { type: 'Polygon', coordinates: [[[78.0, 9.8], [78.2, 9.8], [78.2, 10.0], [78.0, 10.0], [78.0, 9.8]]] },
          },
        ],
      }),
    });
  });

  it('renders Heritage Map page heading and view mode switcher', async () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <HeritageMapPage />
        </AuthProvider>
      </MemoryRouter>
    );

    expect(screen.getByText(/Tamil Nadu Geographic Discovery/i)).toBeInTheDocument();
    expect(screen.getByText(/Map View/i)).toBeInTheDocument();
    expect(screen.getByText(/Accessible List/i)).toBeInTheDocument();
  });

  it('switches between Map View and Accessible List View mode', async () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <HeritageMapPage />
        </AuthProvider>
      </MemoryRouter>
    );

    const listViewButton = screen.getByText(/Accessible List/i);
    fireEvent.click(listViewButton);

    await waitFor(() => {
      expect(screen.getByRole('region')).toBeInTheDocument();
    });

    const mapViewButton = screen.getByText(/Map View/i);
    fireEvent.click(mapViewButton);

    await waitFor(() => {
      expect(screen.getByText(/Tamil Nadu Interactive Map/i)).toBeInTheDocument();
    });
  });
});
