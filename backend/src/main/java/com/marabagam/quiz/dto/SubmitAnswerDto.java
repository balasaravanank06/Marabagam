package com.marabagam.quiz.dto;

import java.util.UUID;

public class SubmitAnswerDto {
    private UUID questionId;
    private UUID selectedOptionId;

    public SubmitAnswerDto() {
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public UUID getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(UUID selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }
}
