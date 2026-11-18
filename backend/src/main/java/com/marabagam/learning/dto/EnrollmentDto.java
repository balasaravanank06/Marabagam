package com.marabagam.learning.dto;

import com.marabagam.learning.domain.Enrollment;
import com.marabagam.learning.domain.EnrollmentStatus;

import java.time.Instant;
import java.util.UUID;

public class EnrollmentDto {
    private UUID id;
    private UUID userId;
    private CourseSummaryDto course;
    private Instant enrolledAt;
    private Instant completedAt;
    private EnrollmentStatus status;
    private int completionPercentage;

    public EnrollmentDto() {}

    public static EnrollmentDto fromEntity(Enrollment entity) {
        if (entity == null) return null;
        EnrollmentDto dto = new EnrollmentDto();
        dto.setId(entity.getId());
        if (entity.getUser() != null) dto.setUserId(entity.getUser().getId());
        if (entity.getCourse() != null) dto.setCourse(CourseSummaryDto.fromEntity(entity.getCourse()));
        dto.setEnrolledAt(entity.getEnrolledAt());
        dto.setCompletedAt(entity.getCompletedAt());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public CourseSummaryDto getCourse() { return course; }
    public void setCourse(CourseSummaryDto course) { this.course = course; }

    public Instant getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
}
