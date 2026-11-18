import { apiClient } from '../lib/api';
import {
  CommunityComment,
  CommunityCommentCreateRequest,
  CommunityPost,
  CommunityPostCreateRequest,
  CommunityPostUpdateRequest,
  CommunityReport,
  CommunityReportCreateRequest
} from '../types/community';
import { PageResponse } from './eventService';

export const communityService = {
  getPublicPosts: async (category?: string, artFormId?: string, page = 0, size = 20): Promise<PageResponse<CommunityPost>> => {
    const params: Record<string, any> = { page, size };
    if (category) params.category = category;
    if (artFormId) params.artFormId = artFormId;
    const response = await apiClient.get<{ success: boolean; data: PageResponse<CommunityPost> }>('/community/posts', { params });
    return response.data.data;
  },

  getPostDetail: async (id: string): Promise<CommunityPost> => {
    const response = await apiClient.get<{ success: boolean; data: CommunityPost }>(`/community/posts/${id}`);
    return response.data.data;
  },

  createPost: async (data: CommunityPostCreateRequest): Promise<CommunityPost> => {
    const response = await apiClient.post<{ success: boolean; data: CommunityPost }>('/community/posts', data);
    return response.data.data;
  },

  updatePost: async (id: string, data: CommunityPostUpdateRequest): Promise<CommunityPost> => {
    const response = await apiClient.put<{ success: boolean; data: CommunityPost }>(`/community/posts/${id}`, data);
    return response.data.data;
  },

  deletePost: async (id: string): Promise<void> => {
    await apiClient.delete(`/community/posts/${id}`);
  },

  addComment: async (postId: string, data: CommunityCommentCreateRequest): Promise<CommunityComment> => {
    const response = await apiClient.post<{ success: boolean; data: CommunityComment }>(`/community/posts/${postId}/comments`, data);
    return response.data.data;
  },

  deleteComment: async (commentId: string): Promise<void> => {
    await apiClient.delete(`/community/comments/${commentId}`);
  },

  submitReport: async (data: CommunityReportCreateRequest): Promise<CommunityReport> => {
    const response = await apiClient.post<{ success: boolean; data: CommunityReport }>('/community/reports', data);
    return response.data.data;
  },

  getAdminPendingReports: async (page = 0, size = 20): Promise<PageResponse<CommunityReport>> => {
    const response = await apiClient.get<{ success: boolean; data: PageResponse<CommunityReport> }>('/admin/community/reports', { params: { page, size } });
    return response.data.data;
  },

  actionReport: async (reportId: string, action: 'DISMISS' | 'HIDE' | 'REMOVE', notes?: string): Promise<CommunityReport> => {
    const response = await apiClient.put<{ success: boolean; data: CommunityReport }>(`/admin/community/reports/${reportId}/action`, null, {
      params: { action, notes }
    });
    return response.data.data;
  }
};
