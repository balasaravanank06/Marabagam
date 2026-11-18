import { apiClient } from '../lib/api';
import {
  CourseSummary,
  CourseDetail,
  LessonDetail,
  Enrollment,
  CourseProgress,
  LessonProgress,
  CreateCourseRequest,
  CreateModuleRequest,
  CreateLessonRequest,
  CourseDifficulty,
  CourseStatus,
  CourseModule
} from '../types/learning';

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export const learningService = {

  // Public APIs
  getPublicCourses: async (params?: {
    page?: number;
    size?: number;
    search?: string;
    artForm?: string;
    difficulty?: CourseDifficulty;
  }): Promise<PageResponse<CourseSummary>> => {
    const response = await apiClient.get('/courses', { params });
    return response.data.data;
  },

  getCourseBySlug: async (slug: string): Promise<CourseDetail> => {
    const response = await apiClient.get(`/courses/${slug}`);
    return response.data.data;
  },

  getLessonDetail: async (courseSlug: string, lessonId: string): Promise<LessonDetail> => {
    const response = await apiClient.get(`/courses/${courseSlug}/lessons/${lessonId}`);
    return response.data.data;
  },

  // Enrollment APIs
  enrollInCourse: async (courseId: string): Promise<Enrollment> => {
    const response = await apiClient.post(`/courses/${courseId}/enroll`);
    return response.data.data;
  },

  getUserEnrollments: async (): Promise<Enrollment[]> => {
    const response = await apiClient.get('/users/me/enrollments');
    return response.data.data;
  },

  getUserEnrollmentForCourse: async (courseId: string): Promise<Enrollment> => {
    const response = await apiClient.get(`/users/me/enrollments/${courseId}`);
    return response.data.data;
  },

  cancelEnrollment: async (courseId: string): Promise<Enrollment> => {
    const response = await apiClient.delete(`/users/me/enrollments/${courseId}`);
    return response.data.data;
  },

  // Progress APIs
  updateLessonProgress: async (
    lessonId: string,
    data: { progressPercent: number; completed?: boolean }
  ): Promise<LessonProgress> => {
    const response = await apiClient.put(`/lessons/${lessonId}/progress`, data);
    return response.data.data;
  },

  getCourseProgress: async (courseId: string): Promise<CourseProgress> => {
    const response = await apiClient.get(`/courses/${courseId}/progress`);
    return response.data.data;
  },

  // Admin / Authoring APIs
  getAdminCourses: async (params?: {
    page?: number;
    size?: number;
    status?: CourseStatus;
    search?: string;
  }): Promise<PageResponse<CourseSummary>> => {
    const response = await apiClient.get('/admin/courses', { params });
    return response.data.data;
  },

  getAdminCourseById: async (id: string): Promise<CourseDetail> => {
    const response = await apiClient.get(`/admin/courses/${id}`);
    return response.data.data;
  },

  createCourse: async (data: CreateCourseRequest): Promise<CourseDetail> => {
    const response = await apiClient.post('/admin/courses', data);
    return response.data.data;
  },

  updateCourse: async (id: string, data: Partial<CreateCourseRequest>): Promise<CourseDetail> => {
    const response = await apiClient.put(`/admin/courses/${id}`, data);
    return response.data.data;
  },

  updateCourseStatus: async (id: string, status: CourseStatus): Promise<CourseDetail> => {
    const response = await apiClient.patch(`/admin/courses/${id}/status`, { status });
    return response.data.data;
  },

  addModule: async (courseId: string, data: CreateModuleRequest): Promise<CourseModule> => {
    const response = await apiClient.post(`/admin/courses/${courseId}/modules`, data);
    return response.data.data;
  },

  addLesson: async (moduleId: string, data: CreateLessonRequest): Promise<LessonDetail> => {
    const response = await apiClient.post(`/admin/courses/modules/${moduleId}/lessons`, data);
    return response.data.data;
  }
};
