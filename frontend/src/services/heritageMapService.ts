import { apiClient } from '../lib/api';

export interface DistrictSummary {
  id: number;
  name: string;
  nameTa: string;
  code: string;
  latitude: number;
  longitude: number;
  shortHistory?: string;
  shortHistoryTa?: string;
  culturalSignificance?: string;
  culturalSignificanceTa?: string;
  geojsonFeatureCode?: string;
  artFormCount: number;
  locationCount: number;
  eventCount: number;
}

export interface HeritageMapSummary {
  state: string;
  totalDistricts: number;
  totalPublishedArtForms: number;
  totalPublishedLocations: number;
  totalPublishedEvents: number;
  districts: DistrictSummary[];
}

export interface AssociatedArtForm {
  id: string;
  name: string;
  nameTa: string;
  slug: string;
  shortDescription: string;
  shortDescriptionTa: string;
  categoryName: string;
}

export interface HeritageLocationItem {
  id: string;
  name: string;
  nameTa: string;
  slug: string;
  locationType: string;
  description: string;
  descriptionTa: string;
  address: string;
  addressTa: string;
  latitude: number;
  longitude: number;
  contactPhone?: string;
  contactEmail?: string;
  websiteUrl?: string;
  provenanceSource?: string;
}

export interface CulturalEventItem {
  id: string;
  title: string;
  titleTa: string;
  slug: string;
  description: string;
  descriptionTa: string;
  eventType: string;
  eventPeriod: string;
  eventPeriodTa: string;
  startDate?: string;
  endDate?: string;
  isDateApproximate: boolean;
  venueName?: string;
  venueNameTa?: string;
  latitude?: number;
  longitude?: number;
  provenanceSource?: string;
}

export interface DistrictDetail {
  id: number;
  name: string;
  nameTa: string;
  state: string;
  code: string;
  latitude: number;
  longitude: number;
  shortHistory: string;
  shortHistoryTa: string;
  culturalSignificance: string;
  culturalSignificanceTa: string;
  geojsonFeatureCode: string;
  artForms: AssociatedArtForm[];
  locations: HeritageLocationItem[];
  events: CulturalEventItem[];
}

export const heritageMapService = {
  getHeritageMapSummary: async (): Promise<HeritageMapSummary> => {
    const response = await apiClient.get<{ success: boolean; data: HeritageMapSummary }>('/heritage-map');
    return response.data.data;
  },

  getDistrictDetail: async (id: number): Promise<DistrictDetail> => {
    const response = await apiClient.get<{ success: boolean; data: DistrictDetail }>(`/districts/${id}`);
    return response.data.data;
  },

  getDistrictsByArtForm: async (slug: string): Promise<DistrictSummary[]> => {
    const response = await apiClient.get<{ success: boolean; data: DistrictSummary[] }>(`/art-forms/${slug}/districts`);
    return response.data.data;
  },
};
