import { ArtFormSummary } from './art';

export type CourseDifficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type CourseStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'PUBLISHED' | 'ARCHIVED';
export type LessonType = 'TEXT' | 'VIDEO' | 'AUDIO' | 'IMAGE' | 'DOCUMENT' | 'MIXED';
export type LessonStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type MediaType = 'VIDEO' | 'AUDIO' | 'IMAGE' | 'DOCUMENT';
export type EnrollmentStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export interface MediaItem {
  id: string;
  lessonId?: string;
  mediaType: MediaType;
  title: string;
  titleTa?: string;
  url: string;
  thumbnailUrl?: string;
  mimeType?: string;
  fileSizeBytes?: number;
  durationSeconds?: number;
  displayOrder: number;
  altText?: string;
  altTextTa?: string;
}

export interface LessonSummary {
  id: string;
  moduleId: string;
  title: string;
  titleTa: string;
  description?: string;
  descriptionTa?: string;
  lessonType: LessonType;
  estimatedDurationMinutes: number;
  displayOrder: number;
  status: LessonStatus;
}

export interface LessonDetail extends LessonSummary {
  courseSlug: string;
  content?: string;
  contentTa?: string;
  mediaItems: MediaItem[];
}

export interface CourseModule {
  id: string;
  courseId: string;
  title: string;
  titleTa: string;
  description?: string;
  descriptionTa?: string;
  displayOrder: number;
  lessons: LessonSummary[];
}

export interface CourseSummary {
  id: string;
  artForm: ArtFormSummary;
  title: string;
  titleTa: string;
  slug: string;
  shortDescription: string;
  shortDescriptionTa: string;
  difficulty: CourseDifficulty;
  estimatedDurationMinutes: number;
  status: CourseStatus;
  createdById?: string;
  createdAt: string;
}

export interface CourseDetail extends CourseSummary {
  description: string;
  descriptionTa: string;
  modules: CourseModule[];
  totalLessonsCount: number;
}

export interface Enrollment {
  id: string;
  userId: string;
  course: CourseSummary;
  enrolledAt: string;
  completedAt?: string;
  status: EnrollmentStatus;
  completionPercentage: number;
}

export interface LessonProgress {
  id: string;
  userId: string;
  lessonId: string;
  progressPercent: number;
  completed: boolean;
  startedAt: string;
  completedAt?: string;
  lastAccessedAt: string;
}

export interface CourseProgress {
  courseId: string;
  totalPublishedLessons: number;
  completedLessons: number;
  completionPercentage: number;
  currentLessonId?: string;
  lastAccessedLessonId?: string;
  isCourseCompleted: boolean;
}

export interface CreateCourseRequest {
  artFormId: string;
  title: string;
  titleTa: string;
  slug: string;
  shortDescription: string;
  shortDescriptionTa: string;
  description: string;
  descriptionTa: string;
  difficulty: CourseDifficulty;
  estimatedDurationMinutes?: number;
}

export interface CreateModuleRequest {
  title: string;
  titleTa: string;
  description?: string;
  descriptionTa?: string;
  displayOrder: number;
}

export interface CreateLessonRequest {
  title: string;
  titleTa: string;
  description?: string;
  descriptionTa?: string;
  content?: string;
  contentTa?: string;
  lessonType: LessonType;
  estimatedDurationMinutes?: number;
  displayOrder: number;
  status?: LessonStatus;
  mediaItems?: {
    mediaType: MediaType;
    title: string;
    titleTa?: string;
    url: string;
    thumbnailUrl?: string;
    mimeType?: string;
    fileSizeBytes?: number;
    durationSeconds?: number;
    displayOrder?: number;
    altText?: string;
    altTextTa?: string;
  }[];
}
