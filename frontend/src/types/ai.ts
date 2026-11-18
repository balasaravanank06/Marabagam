export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface SourceAttribution {
  documentId: string;
  title: string;
  titleTa?: string;
  author?: string;
  sourceName: string;
  sourceUrl?: string;
  sourceType?: string;
}

export interface AiGuideResponse {
  conversationId: string;
  messageId: string;
  answer: string;
  language: string;
  sources: SourceAttribution[];
  relevanceScore?: number;
  createdAt: string;
}

export interface AiMessage {
  id: string;
  sender: 'USER' | 'ASSISTANT';
  content: string;
  sources?: SourceAttribution[];
  relevanceScore?: number;
  createdAt: string;
}

export interface AiConversation {
  id: string;
  title: string;
  messages?: AiMessage[];
  createdAt: string;
  updatedAt: string;
}

export interface KnowledgeDocument {
  id: string;
  title: string;
  titleTa?: string;
  content: string;
  contentTa?: string;
  sourceType: 'ARTICLE' | 'EDUCATIONAL' | 'HISTORICAL' | 'BIOGRAPHY' | 'LESSON';
  sourceName: string;
  sourceUrl?: string;
  author?: string;
  language: string;
  status: 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED' | 'ARCHIVED' | 'OUTDATED';
  indexingStatus: 'NOT_INDEXED' | 'INDEXING' | 'INDEXED' | 'FAILED';
  createdByDisplayName?: string;
  reviewedByDisplayName?: string;
  version: number;
  createdAt: string;
  updatedAt: string;
}
