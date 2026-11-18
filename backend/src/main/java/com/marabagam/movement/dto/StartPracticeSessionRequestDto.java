package com.marabagam.movement.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class StartPracticeSessionRequestDto {

    @NotNull(message = "movementId cannot be null")
    private UUID movementId;

    public UUID getMovementId() { return movementId; }
    public void setMovementId(UUID movementId) { this.movementId = movementId; }
}
