package com.marabagam.gamification.dto;

public class LeaderboardEntryDto {
    private Integer rank;
    private String displayName;
    private Integer xp;
    private Integer level;
    private Integer badgesCount;

    public LeaderboardEntryDto() {
    }

    public LeaderboardEntryDto(Integer rank, String displayName, Integer xp, Integer level, Integer badgesCount) {
        this.rank = rank;
        this.displayName = displayName;
        this.xp = xp;
        this.level = level;
        this.badgesCount = badgesCount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getXp() {
        return xp;
    }

    public void setXp(Integer xp) {
        this.xp = xp;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getBadgesCount() {
        return badgesCount;
    }

    public void setBadgesCount(Integer badgesCount) {
        this.badgesCount = badgesCount;
    }
}
