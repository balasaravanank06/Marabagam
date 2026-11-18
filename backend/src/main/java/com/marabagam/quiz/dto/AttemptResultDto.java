package com.marabagam.quiz.dto;

import com.marabagam.quiz.domain.AttemptStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AttemptResultDto {
    private UUID attemptId;
    private UUID quizId;
    private Integer attemptNumber;
    private Double scoreEarned;
    private Double totalPossibleScore;
    private Double percentage;
    private Boolean passed;
    private AttemptStatus status;
    private Instant startedAt;
    private Instant submittedAt;
    private Integer xpEarned = 0;
    private List<QuizQuestionDto> questionsWithExplanations;

    public AttemptResultDto() {
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
    }

    public UUID getQuizId() {
        return quizId;
    }

    public void setQuizId(UUID quizId) {
        this.quizId = quizId;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public Double getScoreEarned() {
        return scoreEarned;
    }

    public void setScoreEarned(Double scoreEarned) {
        this.scoreEarned = scoreEarned;
    }

    public Double getTotalPossibleScore() {
        return totalPossibleScore;
    }

    public void setTotalPossibleScore(Double totalPossibleScore) {
        this.totalPossibleScore = totalPossibleScore;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    public List<QuizQuestionDto> getQuestionsWithExplanations() {
        return questionsWithExplanations;
    }

    public void setQuestionsWithExplanations(List<QuizQuestionDto> questionsWithExplanations) {
        this.questionsWithExplanations = questionsWithExplanations;
    }
}
