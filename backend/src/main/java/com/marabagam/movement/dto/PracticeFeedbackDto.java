package com.marabagam.movement.dto;

import java.util.UUID;

public class PracticeFeedbackDto {

    private UUID id;
    private String bodyPart;
    private String metricName;
    private Double measuredValue;
    private Double targetRangeMin;
    private Double targetRangeMax;
    private Double score;
    private String feedbackTextEn;
    private String feedbackTextTa;
    private String severity;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
