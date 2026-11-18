package com.marabagam.movement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class LandmarkDto {

    @NotNull(message = "x coordinate cannot be null")
    @Min(value = 0, message = "x coordinate must be >= 0.0")
    @Max(value = 1, message = "x coordinate must be <= 1.0")
    private Double x;

    @NotNull(message = "y coordinate cannot be null")
    @Min(value = 0, message = "y coordinate must be >= 0.0")
    @Max(value = 1, message = "y coordinate must be <= 1.0")
    private Double y;

    private Double z = 0.0;

    @Min(value = 0, message = "visibility must be >= 0.0")
    @Max(value = 1, message = "visibility must be <= 1.0")
    private Double visibility = 1.0;

    @Min(value = 0, message = "presence must be >= 0.0")
    @Max(value = 1, message = "presence must be <= 1.0")
    private Double presence = 1.0;

    public LandmarkDto() {}

    public LandmarkDto(Double x, Double y, Double z, Double visibility, Double presence) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.visibility = visibility;
        this.presence = presence;
    }

    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }

    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }

    public Double getZ() { return z; }
    public void setZ(Double z) { this.z = z; }

    public Double getVisibility() { return visibility; }
    public void setVisibility(Double visibility) { this.visibility = visibility; }

    public Double getPresence() { return presence; }
    public void setPresence(Double presence) { this.presence = presence; }
}
