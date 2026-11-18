package com.marabagam.quiz.dto;

import java.util.List;

public class QuizSubmissionDto {
    private List<SubmitAnswerDto> answers;

    public QuizSubmissionDto() {
    }

    public List<SubmitAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SubmitAnswerDto> answers) {
        this.answers = answers;
    }
}
