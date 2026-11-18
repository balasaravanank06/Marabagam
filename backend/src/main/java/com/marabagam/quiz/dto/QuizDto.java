package com.marabagam.quiz.dto;

import com.marabagam.quiz.domain.QuizScope;
import com.marabagam.quiz.domain.QuizStatus;
import java.util.List;
import java.util.UUID;

public class QuizDto {
    private UUID id;
    private QuizScope scope;
    private UUID courseId;
    private UUID moduleId;
    private UUID lessonId;
    private String title;
    private String titleTa;
    private String description;
    private String descriptionTa;
    private Double passPercentage;
    private Integer maxAttempts;
    private Integer timeLimitMinutes;
    private Boolean mandatory;
    private QuizStatus status;
    private List<QuizQuestionDto> questions;

    public QuizDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public QuizScope getScope() {
        return scope;
    }

    public void setScope(QuizScope scope) {
        this.scope = scope;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public UUID getModuleId() {
        return moduleId;
    }

    public void setModuleId(UUID moduleId) {
        this.moduleId = moduleId;
    }

    public UUID getLessonId() {
        return lessonId;
    }

    public void setLessonId(UUID lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleTa() {
        return titleTa;
    }

    public void setTitleTa(String titleTa) {
        this.titleTa = titleTa;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionTa() {
        return descriptionTa;
    }

    public void setDescriptionTa(String descriptionTa) {
        this.descriptionTa = descriptionTa;
    }

    public Double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(Double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(Integer timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public void setStatus(QuizStatus status) {
        this.status = status;
    }

    public List<QuizQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestionDto> questions) {
        this.questions = questions;
    }
}
