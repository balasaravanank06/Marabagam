export interface CommunityComment {
  id: string;
  postId: string;
  authorDisplayName: string;
  authorRole: string;
  authorAvatarUrl?: string;
  parentCommentId?: string;
  content: string;
  status: 'VISIBLE' | 'HIDDEN' | 'REMOVED' | 'DELETED';
  createdAt: string;
  replies?: CommunityComment[];
}

export interface CommunityPost {
  id: string;
  authorDisplayName: string;
  authorRole: string;
  authorAvatarUrl?: string;
  artFormId?: string;
  artFormName?: string;
  category: 'DISCUSSION' | 'QUESTION' | 'LEARNING_JOURNEY' | 'EVENT_DISCUSSION' | 'ART_FORM_DISCUSSION';
  title: string;
  content: string;
  status: 'VISIBLE' | 'HIDDEN' | 'REMOVED' | 'DELETED';
  commentCount: number;
  createdAt: string;
  updatedAt: string;
  comments?: CommunityComment[];
}

export interface CommunityPostCreateRequest {
  artFormId?: string;
  category?: string;
  title: string;
  content: string;
}

export interface CommunityPostUpdateRequest {
  title?: string;
  content?: string;
  category?: string;
}


export interface CommunityCommentCreateRequest {
  parentCommentId?: string;
  content: string;
}

export interface CommunityReportCreateRequest {
  targetType: 'POST' | 'COMMENT';
  targetId: string;
  reason: string;
  details?: string;
}

export interface CommunityReport {
  id: string;
  reporterName: string;
  targetType: 'POST' | 'COMMENT';
  targetId: string;
  targetContentSnippet?: string;
  reason: string;
  details?: string;
  status: 'PENDING' | 'DISMISSED' | 'ACTIONED';
  actionTaken?: string;
  actionedByName?: string;
  actionedAt?: string;
  createdAt: string;
}
