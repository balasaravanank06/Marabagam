import { apiClient } from '../lib/api';
import { NotificationItem, NotificationPreference } from '../types/notification';

export interface NotificationResponse {
  notifications: {
    content: NotificationItem[];
    totalElements: number;
    totalPages: number;
  };
  unreadCount: number;
}

export const notificationService = {
  getUserNotifications: async (page = 0, size = 20): Promise<NotificationResponse> => {
    const response = await apiClient.get<{ success: boolean; data: NotificationResponse }>('/notifications', { params: { page, size } });
    return response.data.data;
  },

  markAsRead: async (id: string): Promise<NotificationItem> => {
    const response = await apiClient.put<{ success: boolean; data: NotificationItem }>(`/notifications/${id}/read`);
    return response.data.data;
  },

  markAllAsRead: async (): Promise<void> => {
    await apiClient.put('/notifications/read-all');
  },

  getPreferences: async (): Promise<NotificationPreference> => {
    const response = await apiClient.get<{ success: boolean; data: NotificationPreference }>('/notifications/preferences');
    return response.data.data;
  },

  updatePreferences: async (data: Partial<NotificationPreference>): Promise<NotificationPreference> => {
    const response = await apiClient.put<{ success: boolean; data: NotificationPreference }>('/notifications/preferences', data);
    return response.data.data;
  }
};
