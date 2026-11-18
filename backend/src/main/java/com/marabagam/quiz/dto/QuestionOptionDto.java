package com.marabagam.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionOptionDto {
    private UUID id;
    private String optionText;
    private String optionTextTa;
    private Integer displayOrder;
    private Boolean isCorrect;
    private String explanationEn;
    private String explanationTa;

    public QuestionOptionDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public String getOptionTextTa() {
        return optionTextTa;
    }

    public void setOptionTextTa(String optionTextTa) {
        this.optionTextTa = optionTextTa;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getExplanationEn() {
        return explanationEn;
    }

    public void setExplanationEn(String explanationEn) {
        this.explanationEn = explanationEn;
    }

    public String getExplanationTa() {
        return explanationTa;
    }

    public void setExplanationTa(String explanationTa) {
        this.explanationTa = explanationTa;
    }
}
