package com.marabagam.user.domain;

public enum Role {
    GUEST,
    LEARNER,
    ARTIST,
    INSTRUCTOR,
    CULTURAL_EXPERT,
    ADMIN;

    public String toAuthority() {
        return "ROLE_" + this.name();
    }
}
