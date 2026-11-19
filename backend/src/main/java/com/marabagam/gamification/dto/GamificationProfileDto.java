package com.marabagam.gamification.dto;

import java.util.List;

public class GamificationProfileDto {
    private Integer totalXp;
    private Integer level;
    private String levelTitleEn;
    private String levelTitleTa;
    private Integer currentStreak;
    private Integer longestStreak;
    private Integer totalBadges;
    private List<BadgeDto> badges;
    private Integer completedCoursesCount;
    private Integer quizzesPassedCount;
    private Integer leaderboardRank;
    private List<RecentActivityDto> recentActivities;

    public GamificationProfileDto() {
    }

    public Integer getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(Integer totalXp) {
        this.totalXp = totalXp;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getLevelTitleEn() {
        return levelTitleEn;
    }

    public void setLevelTitleEn(String levelTitleEn) {
        this.levelTitleEn = levelTitleEn;
    }

    public String getLevelTitleTa() {
        return levelTitleTa;
    }

    public void setLevelTitleTa(String levelTitleTa) {
        this.levelTitleTa = levelTitleTa;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public Integer getTotalBadges() {
        return totalBadges;
    }

    public void setTotalBadges(Integer totalBadges) {
        this.totalBadges = totalBadges;
    }

    public List<BadgeDto> getBadges() {
        return badges;
    }

    public void setBadges(List<BadgeDto> badges) {
        this.badges = badges;
    }

    public Integer getCompletedCoursesCount() {
        return completedCoursesCount;
    }

    public void setCompletedCoursesCount(Integer completedCoursesCount) {
        this.completedCoursesCount = completedCoursesCount;
    }

    public Integer getQuizzesPassedCount() {
        return quizzesPassedCount;
    }

    public void setQuizzesPassedCount(Integer quizzesPassedCount) {
        this.quizzesPassedCount = quizzesPassedCount;
    }

    public Integer getLeaderboardRank() {
        return leaderboardRank;
    }

    public void setLeaderboardRank(Integer leaderboardRank) {
        this.leaderboardRank = leaderboardRank;
    }

    public List<RecentActivityDto> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<RecentActivityDto> recentActivities) {
        this.recentActivities = recentActivities;
    }
}
