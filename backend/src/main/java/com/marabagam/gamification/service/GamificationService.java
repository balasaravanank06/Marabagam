package com.marabagam.gamification.service;

import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.gamification.domain.*;
import com.marabagam.gamification.dto.BadgeDto;
import com.marabagam.gamification.dto.GamificationProfileDto;
import com.marabagam.gamification.dto.LeaderboardEntryDto;
import com.marabagam.gamification.repository.*;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GamificationService {

    private static final Logger log = LoggerFactory.getLogger(GamificationService.class);

    @Autowired
    private XpTransactionRepository xpTransactionRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private UserStreakRepository userStreakRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public boolean awardXp(User user, int amount, SourceActivity activity, String idempotencyKey) {
        if (amount <= 0 || idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }

        if (xpTransactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.info("XP Transaction idempotency key already exists: {}", idempotencyKey);
            return false;
        }

        XpTransaction tx = new XpTransaction();
        tx.setUser(user);
        tx.setAmount(amount);
        tx.setSourceActivity(activity);
        tx.setIdempotencyKey(idempotencyKey);
        xpTransactionRepository.save(tx);

        log.info("Awarded {} XP to user {} for activity {} (Key: {})", amount, user.getId(), activity, idempotencyKey);
        
        // Evaluate streaks & badges
        updateStreak(user);
        evaluateBadges(user);
        return true;
    }

    @Transactional
    public void updateStreak(User user) {
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        Optional<UserStreak> streakOpt = userStreakRepository.findByUserId(user.getId());

        UserStreak streak;
        if (streakOpt.isEmpty()) {
            streak = new UserStreak();
            streak.setUser(user);
            streak.setCurrentStreak(1);
            streak.setLongestStreak(1);
            streak.setLastActivityDate(todayUtc);
            userStreakRepository.save(streak);
            return;
        }

        streak = streakOpt.get();
        LocalDate lastDate = streak.getLastActivityDate();

        if (lastDate.equals(todayUtc)) {
            // Already logged activity today, do not alter streak
            return;
        } else if (lastDate.equals(todayUtc.minusDays(1))) {
            // Consecutive day
            int newStreak = streak.getCurrentStreak() + 1;
            streak.setCurrentStreak(newStreak);
            if (newStreak > streak.getLongestStreak()) {
                streak.setLongestStreak(newStreak);
            }
            streak.setLastActivityDate(todayUtc);
        } else {
            // Missed a day
            streak.setCurrentStreak(1);
            streak.setLastActivityDate(todayUtc);
        }

        userStreakRepository.save(streak);

        // Check for 7-day streak bonus
        if (streak.getCurrentStreak() == 7) {
            String streakKey = "STREAK_BONUS_7:" + user.getId() + ":" + todayUtc;
            awardXp(user, 100, SourceActivity.STREAK_BONUS, streakKey);
        }
    }

    @Transactional
    public void evaluateBadges(User user) {
        List<Badge> allBadges = badgeRepository.findByActiveTrue();
        for (Badge badge : allBadges) {
            if (userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
                continue;
            }

            boolean eligible = checkBadgeEligibility(user, badge.getCode());
            if (eligible) {
                UserBadge ub = new UserBadge();
                ub.setUser(user);
                ub.setBadge(badge);
                userBadgeRepository.save(ub);
                log.info("Badge [{}] awarded to user {}", badge.getCode(), user.getId());
            }
        }
    }

    private boolean checkBadgeEligibility(User user, String badgeCode) {
        switch (badgeCode) {
            case "FIRST_STEP":
                return xpTransactionRepository.sumAmountByUserId(user.getId()) > 0;
            case "FIRST_QUIZ_PASS":
                return xpTransactionRepository.existsByIdempotencyKey("QUIZ_PASS_FIRST:" + user.getId() + ":*") ||
                       userBadgeRepository.countByUserId(user.getId()) > 0;
            case "WEEKLY_STREAK_7":
                Optional<UserStreak> st = userStreakRepository.findByUserId(user.getId());
                return st.isPresent() && st.get().getLongestStreak() >= 7;
            default:
                return false;
        }
    }

    public GamificationProfileDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        int totalXp = xpTransactionRepository.sumAmountByUserId(userId);
        int level = (int) Math.floor(Math.sqrt(totalXp / 100.0)) + 1;

        String titleEn = getLevelTitleEn(level);
        String titleTa = getLevelTitleTa(level);

        UserStreak streak = userStreakRepository.findByUserId(userId).orElseGet(() -> {
            UserStreak s = new UserStreak();
            s.setCurrentStreak(0);
            s.setLongestStreak(0);
            return s;
        });

        List<Badge> allBadges = badgeRepository.findByActiveTrue();
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);

        List<BadgeDto> badgeDtos = new ArrayList<>();
        for (Badge b : allBadges) {
            BadgeDto dto = new BadgeDto();
            dto.setId(b.getId());
            dto.setCode(b.getCode());
            dto.setNameEn(b.getNameEn());
            dto.setNameTa(b.getNameTa());
            dto.setDescriptionEn(b.getDescriptionEn());
            dto.setDescriptionTa(b.getDescriptionTa());
            dto.setIconUrl(b.getIconUrl());
            dto.setCategory(b.getCategory());

            Optional<UserBadge> ubOpt = userBadges.stream()
                    .filter(ub -> ub.getBadge().getId().equals(b.getId()))
                    .findFirst();

            if (ubOpt.isPresent()) {
                dto.setEarned(true);
                dto.setAwardedAt(ubOpt.get().getAwardedAt());
            } else {
                dto.setEarned(false);
            }
            badgeDtos.add(dto);
        }

        GamificationProfileDto profile = new GamificationProfileDto();
        profile.setTotalXp(totalXp);
        profile.setLevel(level);
        profile.setLevelTitleEn(titleEn);
        profile.setLevelTitleTa(titleTa);
        profile.setCurrentStreak(streak.getCurrentStreak());
        profile.setLongestStreak(streak.getLongestStreak());
        profile.setTotalBadges(userBadges.size());
        profile.setBadges(badgeDtos);

        return profile;
    }

    public List<LeaderboardEntryDto> getGlobalLeaderboard(int page, int size) {
        List<Object[]> rows = xpTransactionRepository.findGlobalLeaderboard(PageRequest.of(page, size));
        List<LeaderboardEntryDto> entries = new ArrayList<>();
        int rank = page * size + 1;

        for (Object[] row : rows) {
            String name = (String) row[1];
            Long totalXpLong = (Long) row[3];
            int totalXp = totalXpLong != null ? totalXpLong.intValue() : 0;
            int level = (int) Math.floor(Math.sqrt(totalXp / 100.0)) + 1;
            UUID userId = (UUID) row[0];
            int badgesCount = userBadgeRepository.countByUserId(userId);

            entries.add(new LeaderboardEntryDto(rank++, name, totalXp, level, badgesCount));
        }

        return entries;
    }

    public List<LeaderboardEntryDto> getWeeklyLeaderboard(int page, int size) {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime startOfWeek = nowUtc.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime endOfWeek = startOfWeek.plusDays(7);

        List<Object[]> rows = xpTransactionRepository.findWeeklyLeaderboard(
                startOfWeek.toInstant(),
                endOfWeek.toInstant(),
                PageRequest.of(page, size)
        );

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        int rank = page * size + 1;

        for (Object[] row : rows) {
            String name = (String) row[1];
            Long xpLong = (Long) row[3];
            int weeklyXp = xpLong != null ? xpLong.intValue() : 0;
            UUID userId = (UUID) row[0];
            int totalXp = xpTransactionRepository.sumAmountByUserId(userId);
            int level = (int) Math.floor(Math.sqrt(totalXp / 100.0)) + 1;
            int badgesCount = userBadgeRepository.countByUserId(userId);

            entries.add(new LeaderboardEntryDto(rank++, name, weeklyXp, level, badgesCount));
        }

        return entries;
    }

    private String getLevelTitleEn(int level) {
        switch (level) {
            case 1: return "Novice Student";
            case 2: return "Dedicated Practitioner";
            case 3: return "Heritage Explorer";
            case 4: return "Skilled Artiste";
            case 5: return "Heritage Guardian";
            default: return "Senior Master";
        }
    }

    private String getLevelTitleTa(int level) {
        switch (level) {
            case 1: return "ஆரம்பநிலை மாணவர்";
            case 2: return "தீவிர பயிற்சிமையாளர்";
            case 3: return "பாரம்பரிய ஆய்வாளர்";
            case 4: return "கலை வல்லுநர்";
            case 5: return "பாரம்பரியக் காவலர்";
            default: return "கலைப் பேரசான்";
        }
    }
}
