package com.marabagam.movement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubmitReferenceRequestDto {

    @NotBlank(message = "title cannot be blank")
    private String title;

    @NotBlank(message = "performerAttribution cannot be blank")
    private String performerAttribution;

    @NotBlank(message = "keypointsTemplateJson cannot be blank")
    private String keypointsTemplateJson;

    private String demonstrationVideoUrl;
    private String thumbnailUrl;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPerformerAttribution() { return performerAttribution; }
    public void setPerformerAttribution(String performerAttribution) { this.performerAttribution = performerAttribution; }

    public String getKeypointsTemplateJson() { return keypointsTemplateJson; }
    public void setKeypointsTemplateJson(String keypointsTemplateJson) { this.keypointsTemplateJson = keypointsTemplateJson; }

    public String getDemonstrationVideoUrl() { return demonstrationVideoUrl; }
    public void setDemonstrationVideoUrl(String demonstrationVideoUrl) { this.demonstrationVideoUrl = demonstrationVideoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
}
