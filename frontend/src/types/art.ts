export type ArtFormStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'PUBLISHED' | 'ARCHIVED';

export interface Category {
  id: number;
  name: string;
  nameTa: string;
  description?: string;
  descriptionTa?: string;
}

export interface District {
  id: number;
  name: string;
  nameTa: string;
  state: string;
}

export interface ArtFormSummary {
  id: string;
  name: string;
  nameTa: string;
  slug: string;
  shortDescription: string;
  shortDescriptionTa: string;
  origin?: string;
  originTa?: string;
  status: ArtFormStatus;
  categories: Category[];
  districts: District[];
}

export interface ArtFormDetail extends ArtFormSummary {
  description: string;
  descriptionTa: string;
  historicalPeriod?: string;
  historicalPeriodTa?: string;
  culturalSignificance?: string;
  culturalSignificanceTa?: string;
  performanceStyle?: string;
  performanceStyleTa?: string;
  costume?: string;
  costumeTa?: string;
  instruments?: string;
  instrumentsTa?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaginatedArtForms {
  content: ArtFormSummary[];
  totalElements: number;
  totalPages: number;
  number: number; // current page 0-indexed
  size: number;
}

export interface CreateArtFormPayload {
  name: string;
  nameTa: string;
  slug: string;
  shortDescription: string;
  shortDescriptionTa: string;
  description: string;
  descriptionTa: string;
  origin?: string;
  originTa?: string;
  historicalPeriod?: string;
  historicalPeriodTa?: string;
  culturalSignificance?: string;
  culturalSignificanceTa?: string;
  performanceStyle?: string;
  performanceStyleTa?: string;
  costume?: string;
  costumeTa?: string;
  instruments?: string;
  instrumentsTa?: string;
  categoryIds?: number[];
  districtIds?: number[];
}
