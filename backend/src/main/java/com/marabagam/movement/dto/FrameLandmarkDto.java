package com.marabagam.movement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class FrameLandmarkDto {

    private Integer frameIndex;
    private Long timestampMs;

    @NotNull(message = "Landmarks list cannot be null")
    @Valid
    private List<LandmarkDto> landmarks;

    public Integer getFrameIndex() { return frameIndex; }
    public void setFrameIndex(Integer frameIndex) { this.frameIndex = frameIndex; }

    public Long getTimestampMs() { return timestampMs; }
    public void setTimestampMs(Long timestampMs) { this.timestampMs = timestampMs; }

    public List<LandmarkDto> getLandmarks() { return landmarks; }
    public void setLandmarks(List<LandmarkDto> landmarks) { this.landmarks = landmarks; }
}
