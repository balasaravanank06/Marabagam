import { apiClient } from '../lib/api';
import { CulturalEvent, EventCreateRequest, EventRegistration, EventUpdateRequest } from '../types/event';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const eventService = {
  getPublicEvents: async (districtId?: number, artFormId?: string, page = 0, size = 20): Promise<PageResponse<CulturalEvent>> => {
    const params: Record<string, any> = { page, size };
    if (districtId) params.districtId = districtId;
    if (artFormId) params.artFormId = artFormId;
    const response = await apiClient.get<{ success: boolean; data: PageResponse<CulturalEvent> }>('/events', { params });
    return response.data.data;
  },

  getEventBySlug: async (slug: string): Promise<CulturalEvent> => {
    const response = await apiClient.get<{ success: boolean; data: CulturalEvent }>(`/events/${slug}`);
    return response.data.data;
  },

  getMyRegistrations: async (): Promise<EventRegistration[]> => {
    const response = await apiClient.get<{ success: boolean; data: EventRegistration[] }>('/events/my-registrations');
    return response.data.data;
  },

  createEvent: async (data: EventCreateRequest): Promise<CulturalEvent> => {
    const response = await apiClient.post<{ success: boolean; data: CulturalEvent }>('/events', data);
    return response.data.data;
  },

  updateEvent: async (id: string, data: EventUpdateRequest): Promise<CulturalEvent> => {
    const response = await apiClient.put<{ success: boolean; data: CulturalEvent }>(`/events/${id}`, data);
    return response.data.data;
  },

  submitEvent: async (id: string): Promise<CulturalEvent> => {
    const response = await apiClient.put<{ success: boolean; data: CulturalEvent }>(`/events/${id}/submit`);
    return response.data.data;
  },

  approveEvent: async (id: string): Promise<CulturalEvent> => {
    const response = await apiClient.put<{ success: boolean; data: CulturalEvent }>(`/events/${id}/approve`);
    return response.data.data;
  },

  publishEvent: async (id: string): Promise<CulturalEvent> => {
    const response = await apiClient.put<{ success: boolean; data: CulturalEvent }>(`/events/${id}/publish`);
    return response.data.data;
  },

  cancelEvent: async (id: string): Promise<CulturalEvent> => {
    const response = await apiClient.put<{ success: boolean; data: CulturalEvent }>(`/events/${id}/cancel`);
    return response.data.data;
  },

  registerForEvent: async (id: string): Promise<EventRegistration> => {
    const response = await apiClient.post<{ success: boolean; data: EventRegistration }>(`/events/${id}/register`);
    return response.data.data;
  },

  cancelRegistration: async (id: string): Promise<EventRegistration> => {
    const response = await apiClient.post<{ success: boolean; data: EventRegistration }>(`/events/${id}/cancel-registration`);
    return response.data.data;
  }
};
