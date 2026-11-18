package com.marabagam.learning.dto;

import java.util.UUID;

public class CourseProgressDto {
    private UUID courseId;
    private long totalPublishedLessons;
    private long completedLessons;
    private int completionPercentage;
    private UUID currentLessonId;
    private UUID lastAccessedLessonId;
    private boolean isCourseCompleted;

    public CourseProgressDto() {}

    public CourseProgressDto(UUID courseId, long totalPublishedLessons, long completedLessons,
                             int completionPercentage, UUID currentLessonId, UUID lastAccessedLessonId, boolean isCourseCompleted) {
        this.courseId = courseId;
        this.totalPublishedLessons = totalPublishedLessons;
        this.completedLessons = completedLessons;
        this.completionPercentage = completionPercentage;
        this.currentLessonId = currentLessonId;
        this.lastAccessedLessonId = lastAccessedLessonId;
        this.isCourseCompleted = isCourseCompleted;
    }

    // Getters and Setters
    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }

    public long getTotalPublishedLessons() { return totalPublishedLessons; }
    public void setTotalPublishedLessons(long totalPublishedLessons) { this.totalPublishedLessons = totalPublishedLessons; }

    public long getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(long completedLessons) { this.completedLessons = completedLessons; }

    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }

    public UUID getCurrentLessonId() { return currentLessonId; }
    public void setCurrentLessonId(UUID currentLessonId) { this.currentLessonId = currentLessonId; }

    public UUID getLastAccessedLessonId() { return lastAccessedLessonId; }
    public void setLastAccessedLessonId(UUID lastAccessedLessonId) { this.lastAccessedLessonId = lastAccessedLessonId; }

    public boolean isCourseCompleted() { return isCourseCompleted; }
    public void setCourseCompleted(boolean courseCompleted) { isCourseCompleted = courseCompleted; }
}
