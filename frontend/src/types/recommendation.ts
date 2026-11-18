export interface Recommendation {
  id: string;
  itemType: 'COURSE' | 'EVENT' | 'ART_FORM';
  itemId: string;
  title: string;
  titleTa: string;
  description: string;
  descriptionTa: string;
  imageUrl?: string;
  targetUrl: string;
  artFormName?: string;
  artFormNameTa?: string;
  reason: string;
  reasonTa: string;
  rank: number;
}

export interface RecommendationFeed {
  recommendations: Recommendation[];
  generatedAt: string;
  isColdStart: boolean;
}
