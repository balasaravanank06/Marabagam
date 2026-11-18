export interface NotificationItem {
  id: string;
  notificationType: 'EVENT_REMINDER' | 'EVENT_CANCELLED' | 'BADGE_EARNED' | 'CERTIFICATE_ISSUED' | 'COMMUNITY_REPLY' | 'MODERATION_UPDATE' | 'ACCOUNT_SECURITY';
  title: string;
  titleTa: string;
  message: string;
  messageTa: string;
  isRead: boolean;
  relatedEntityType?: string;
  relatedEntityId?: string;
  createdAt: string;
}

export interface NotificationPreference {
  eventsEnabled: boolean;
  communityEnabled: boolean;
  achievementsEnabled: boolean;
}
