package com.marabagam.movement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class EvaluatePracticeSessionRequestDto {

    @NotNull(message = "Frames payload cannot be null")
    @Size(min = 1, max = 300, message = "Frame count must be between 1 and 300 frames per evaluation request")
    @Valid
    private List<FrameLandmarkDto> frames;

    public List<FrameLandmarkDto> getFrames() { return frames; }
    public void setFrames(List<FrameLandmarkDto> frames) { this.frames = frames; }
}
