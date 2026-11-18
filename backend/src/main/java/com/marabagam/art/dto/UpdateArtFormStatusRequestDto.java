package com.marabagam.art.dto;

import com.marabagam.art.domain.ArtFormStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateArtFormStatusRequestDto {

    @NotNull(message = "Status is required")
    private ArtFormStatus status;

    public UpdateArtFormStatusRequestDto() {
    }

    public UpdateArtFormStatusRequestDto(ArtFormStatus status) {
        this.status = status;
    }

    public ArtFormStatus getStatus() {
        return status;
    }

    public void setStatus(ArtFormStatus status) {
        this.status = status;
    }
}
