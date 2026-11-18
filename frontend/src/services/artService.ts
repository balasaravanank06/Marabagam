import { apiClient } from '../lib/api';
import {
  ArtFormDetail,
  ArtFormStatus,
  Category,
  CreateArtFormPayload,
  District,
  PaginatedArtForms,
} from '../types/art';

export const artService = {
  // Public APIs
  getPublishedArtForms: async (params: {
    page?: number;
    size?: number;
    search?: string;
    category?: string;
    district?: string;
  }): Promise<PaginatedArtForms> => {
    const response = await apiClient.get<{ success: boolean; data: PaginatedArtForms }>('/art-forms', {
      params,
    });
    return response.data.data;
  },

  getArtFormBySlug: async (slug: string): Promise<ArtFormDetail> => {
    const response = await apiClient.get<{ success: boolean; data: ArtFormDetail }>(`/art-forms/${slug}`);
    return response.data.data;
  },

  getCategories: async (): Promise<Category[]> => {
    const response = await apiClient.get<{ success: boolean; data: Category[] }>('/categories');
    return response.data.data;
  },

  getDistricts: async (): Promise<District[]> => {
    const response = await apiClient.get<{ success: boolean; data: District[] }>('/districts');
    return response.data.data;
  },

  // Management / Admin APIs
  getArtFormsForManagement: async (params: {
    page?: number;
    size?: number;
    status?: ArtFormStatus;
    search?: string;
    category?: string;
    district?: string;
  }): Promise<PaginatedArtForms> => {
    const response = await apiClient.get<{ success: boolean; data: PaginatedArtForms }>('/admin/art-forms', {
      params,
    });
    return response.data.data;
  },

  getArtFormByIdAdmin: async (id: string): Promise<ArtFormDetail> => {
    const response = await apiClient.get<{ success: boolean; data: ArtFormDetail }>(`/admin/art-forms/${id}`);
    return response.data.data;
  },

  createArtForm: async (payload: CreateArtFormPayload): Promise<ArtFormDetail> => {
    const response = await apiClient.post<{ success: boolean; data: ArtFormDetail }>('/admin/art-forms', payload);
    return response.data.data;
  },

  updateArtForm: async (id: string, payload: Partial<CreateArtFormPayload>): Promise<ArtFormDetail> => {
    const response = await apiClient.put<{ success: boolean; data: ArtFormDetail }>(`/admin/art-forms/${id}`, payload);
    return response.data.data;
  },

  updateArtFormStatus: async (id: string, status: ArtFormStatus): Promise<ArtFormDetail> => {
    const response = await apiClient.patch<{ success: boolean; data: ArtFormDetail }>(`/admin/art-forms/${id}/status`, {
      status,
    });
    return response.data.data;
  },

  deleteArtForm: async (id: string): Promise<void> => {
    await apiClient.delete(`/admin/art-forms/${id}`);
  },
};
