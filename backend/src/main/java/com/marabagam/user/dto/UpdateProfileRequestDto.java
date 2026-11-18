package com.marabagam.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequestDto {

    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 150, message = "Display name must be between 2 and 150 characters")
    private String displayName;

    @NotBlank(message = "Preferred language is required")
    @Size(min = 2, max = 10, message = "Preferred language code must be valid (e.g. en, ta)")
    private String preferredLanguage;

    public UpdateProfileRequestDto() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
}
