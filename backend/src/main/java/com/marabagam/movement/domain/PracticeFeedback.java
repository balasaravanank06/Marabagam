package com.marabagam.movement.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "practice_feedbacks")
public class PracticeFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practice_session_id", nullable = false)
    private PracticeSession practiceSession;

    @Column(name = "body_part", nullable = false, length = 100)
    private String bodyPart;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(name = "measured_value", nullable = false)
    private Double measuredValue;

    @Column(name = "target_range_min", nullable = false)
    private Double targetRangeMin;

    @Column(name = "target_range_max", nullable = false)
    private Double targetRangeMax;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "feedback_text_en", nullable = false, columnDefinition = "TEXT")
    private String feedbackTextEn;

    @Column(name = "feedback_text_ta", nullable = false, columnDefinition = "TEXT")
    private String feedbackTextTa;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private FeedbackSeverity severity = FeedbackSeverity.INFO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public PracticeSession getPracticeSession() { return practiceSession; }
    public void setPracticeSession(PracticeSession practiceSession) { this.practiceSession = practiceSession; }

    public String getBodyPart() { return bodyPart; }
    public void setBodyPart(String bodyPart) { this.bodyPart = bodyPart; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public Double getMeasuredValue() { return measuredValue; }
    public void setMeasuredValue(Double measuredValue) { this.measuredValue = measuredValue; }

    public Double getTargetRangeMin() { return targetRangeMin; }
    public void setTargetRangeMin(Double targetRangeMin) { this.targetRangeMin = targetRangeMin; }

    public Double getTargetRangeMax() { return targetRangeMax; }
    public void setTargetRangeMax(Double targetRangeMax) { this.targetRangeMax = targetRangeMax; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getFeedbackTextEn() { return feedbackTextEn; }
    public void setFeedbackTextEn(String feedbackTextEn) { this.feedbackTextEn = feedbackTextEn; }

    public String getFeedbackTextTa() { return feedbackTextTa; }
    public void setFeedbackTextTa(String feedbackTextTa) { this.feedbackTextTa = feedbackTextTa; }

    public FeedbackSeverity getSeverity() { return severity; }
    public void setSeverity(FeedbackSeverity severity) { this.severity = severity; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
