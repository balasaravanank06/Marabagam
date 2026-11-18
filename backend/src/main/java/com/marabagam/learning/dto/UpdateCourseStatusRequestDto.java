package com.marabagam.learning.dto;

import com.marabagam.learning.domain.CourseStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateCourseStatusRequestDto {

    @NotNull(message = "Status is required")
    private CourseStatus status;

    public UpdateCourseStatusRequestDto() {}

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }
}
