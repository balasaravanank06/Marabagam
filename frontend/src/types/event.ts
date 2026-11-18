export interface CulturalEvent {
  id: string;
  title: string;
  titleTa: string;
  slug: string;
  districtId?: number;
  districtName?: string;
  districtNameTa?: string;
  artFormId?: string;
  artFormName?: string;
  artFormNameTa?: string;
  description: string;
  descriptionTa: string;
  eventType: string;
  eventPeriod: string;
  eventPeriodTa: string;
  startDate?: string;
  endDate?: string;
  isDateApproximate: boolean;
  venueName?: string;
  venueNameTa?: string;
  latitude?: number;
  longitude?: number;
  provenanceSource?: string;
  status: 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'PUBLISHED' | 'CANCELLED' | 'ARCHIVED';
  createdById?: string;
  createdByName?: string;
  createdAt: string;

  capacity?: number;
  registeredCount?: number;
  registrationDeadline?: string;
  registrationRequired?: boolean;
  isOnline?: boolean;
  meetingUrl?: string;
  organizerName?: string;
  instructorId?: string;
  instructorName?: string;
  startAt?: string;
  endAt?: string;
  timezoneIdentifier?: string;
  isUserRegistered?: boolean;
}

export interface EventCreateRequest {
  title: string;
  titleTa: string;
  districtId: number;
  artFormId?: string;
  description: string;
  descriptionTa: string;
  eventType?: string;
  eventPeriod?: string;
  eventPeriodTa?: string;
  isDateApproximate?: boolean;
  venueName?: string;
  venueNameTa?: string;
  latitude?: number;
  longitude?: number;
  provenanceSource?: string;
  capacity?: number;
  registrationDeadline?: string;
  registrationRequired?: boolean;
  isOnline?: boolean;
  meetingUrl?: string;
  organizerName?: string;
  instructorId?: string;
  startAt?: string;
  endAt?: string;
  timezoneIdentifier?: string;
}

export interface EventUpdateRequest extends Partial<EventCreateRequest> {}

export interface EventRegistration {
  id: string;
  eventId: string;
  eventTitle: string;
  eventTitleTa: string;
  userId: string;
  userName: string;
  status: 'REGISTERED' | 'CANCELLED';
  registeredAt: string;
  cancelledAt?: string;
}
