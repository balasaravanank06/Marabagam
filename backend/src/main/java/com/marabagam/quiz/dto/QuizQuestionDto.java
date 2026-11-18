package com.marabagam.quiz.dto;

import com.marabagam.quiz.domain.QuestionType;
import java.util.List;
import java.util.UUID;

public class QuizQuestionDto {
    private UUID id;
    private String questionText;
    private String questionTextTa;
    private QuestionType questionType;
    private UUID mediaId;
    private String mediaUrl;
    private Integer points;
    private Integer displayOrder;
    private List<QuestionOptionDto> options;

    public QuizQuestionDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionTextTa() {
        return questionTextTa;
    }

    public void setQuestionTextTa(String questionTextTa) {
        this.questionTextTa = questionTextTa;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public UUID getMediaId() {
        return mediaId;
    }

    public void setMediaId(UUID mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public List<QuestionOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionDto> options) {
        this.options = options;
    }
}
