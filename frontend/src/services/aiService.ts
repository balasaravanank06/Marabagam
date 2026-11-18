import { apiClient } from '../lib/api';
import { AiGuideResponse, AiConversation, KnowledgeDocument, PageResponse } from '../types/ai';

export const aiService = {
  askCulturalGuide: async (query: string, conversationId?: string): Promise<AiGuideResponse> => {
    const response = await apiClient.post('/ai/cultural-guide', {
      query,
      conversationId: conversationId || undefined,
    });
    return response.data.data;
  },

  getConversations: async (page = 0, size = 10): Promise<PageResponse<AiConversation>> => {
    const response = await apiClient.get('/ai/conversations', {
      params: { page, size },
    });
    return response.data.data;
  },

  getConversationById: async (id: string): Promise<AiConversation> => {
    const response = await apiClient.get(`/ai/conversations/${id}`);
    return response.data.data;
  },

  deleteConversation: async (id: string): Promise<void> => {
    await apiClient.delete(`/ai/conversations/${id}`);
  },

  getKnowledgeDocuments: async (search?: string, status?: string, page = 0, size = 10): Promise<PageResponse<KnowledgeDocument>> => {
    const response = await apiClient.get('/admin/knowledge-documents', {
      params: { search, status, page, size },
    });
    return response.data.data;
  },

  createKnowledgeDocument: async (data: Partial<KnowledgeDocument>): Promise<KnowledgeDocument> => {
    const response = await apiClient.post('/admin/knowledge-documents', data);
    return response.data.data;
  },

  updateKnowledgeStatus: async (id: string, status: string): Promise<KnowledgeDocument> => {
    const response = await apiClient.patch(`/admin/knowledge-documents/${id}/status`, { status });
    return response.data.data;
  },
};
