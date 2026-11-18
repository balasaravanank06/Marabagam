package com.marabagam.learning.service;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.CulturalEvent;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.art.repository.CulturalEventRepository;
import com.marabagam.art.repository.EventRegistrationRepository;
import com.marabagam.learning.domain.Course;
import com.marabagam.learning.domain.Enrollment;
import com.marabagam.learning.dto.RecommendationDto;
import com.marabagam.learning.dto.RecommendationFeedDto;
import com.marabagam.learning.repository.CourseRepository;
import com.marabagam.learning.repository.EnrollmentRepository;
import com.marabagam.user.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final CourseRepository courseRepository;
    private final CulturalEventRepository eventRepository;
    private final ArtFormRepository artFormRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EventRegistrationRepository registrationRepository;

    public RecommendationService(CourseRepository courseRepository,
                                 CulturalEventRepository eventRepository,
                                 ArtFormRepository artFormRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 EventRegistrationRepository registrationRepository) {
        this.courseRepository = courseRepository;
        this.eventRepository = eventRepository;
        this.artFormRepository = artFormRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional(readOnly = true)
    public RecommendationFeedDto getPersonalizedRecommendations(User user) {
        List<RecommendationDto> candidates = new ArrayList<>();

        if (user == null) {
            return getColdStartFeed();
        }

        // Fetch user learning signals
        List<Enrollment> userEnrollments = enrollmentRepository.findByUserId(user.getId());
        Set<UUID> activeCourseIds = userEnrollments.stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());

        Set<UUID> completedCourseIds = userEnrollments.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()))
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());

        Set<UUID> preferredArtFormIds = new HashSet<>();
        Set<Long> preferredDistrictIds = new HashSet<>();

        for (Enrollment e : userEnrollments) {
            if (e.getCourse() != null && e.getCourse().getArtForm() != null) {
                preferredArtFormIds.add(e.getCourse().getArtForm().getId());
            }
        }

        // District signals from event registrations (cultural-learning interaction only)
        registrationRepository.findByUserIdAndStatus(user.getId(), "REGISTERED").forEach(reg -> {
            if (reg.getEvent() != null && reg.getEvent().getDistrict() != null) {
                preferredDistrictIds.add(reg.getEvent().getDistrict().getId());
                if (reg.getEvent().getArtForm() != null) {
                    preferredArtFormIds.add(reg.getEvent().getArtForm().getId());
                }
            }
        });

        // Cold-start fallback if learner has zero interaction history
        if (activeCourseIds.isEmpty() && completedCourseIds.isEmpty() && preferredArtFormIds.isEmpty()) {
            return getColdStartFeed();
        }

        // Candidate 1: Published Courses (Exclude active enrollments)
        List<Course> availableCourses = courseRepository.findByPublishedTrue();
        for (Course course : availableCourses) {
            if (activeCourseIds.contains(course.getId())) {
                continue; // Exclude currently active course enrollment
            }

            double score = 0.0;
            String reason = "Recommended based on popular demand.";
            String reasonTa = "பிரபலமான தேவையின் அடிப்படையில் பரிந்துரைக்கப்படுகிறது.";

            if (course.getArtForm() != null && preferredArtFormIds.contains(course.getArtForm().getId())) {
                score += 40.0;
                reason = "Based on your completed practice in " + course.getArtForm().getName() + ".";
                reasonTa = "நீங்கள் கற்ற " + course.getArtForm().getNameTa() + " கலைப் பிரிவின் அடிப்படையில்.";
            }

            if (completedCourseIds.contains(course.getId())) {
                score -= 30.0; // Deprioritize completed courses
            } else {
                score += 15.0; // Unenrolled new course bonus
            }

            // Freshness bonus
            score += 10.0;

            RecommendationDto dto = new RecommendationDto();
            dto.setId(UUID.randomUUID());
            dto.setItemType("COURSE");
            dto.setItemId(course.getId());
            dto.setTitle(course.getTitle());
            dto.setTitleTa(course.getTitleTa());
            dto.setDescription(course.getDescription());
            dto.setDescriptionTa(course.getDescriptionTa());
            dto.setImageUrl(null);

            dto.setTargetUrl("/course/" + course.getSlug());
            if (course.getArtForm() != null) {
                dto.setArtFormName(course.getArtForm().getName());
                dto.setArtFormNameTa(course.getArtForm().getNameTa());
            }
            dto.setReason(reason);
            dto.setReasonTa(reasonTa);
            dto.setRank((int) score);
            candidates.add(dto);
        }

        // Candidate 2: Published Upcoming Events
        List<CulturalEvent> publishedEvents = eventRepository.findByStatus("PUBLISHED");
        for (CulturalEvent event : publishedEvents) {
            // Exclude past events if startAt is past
            if (event.getStartAt() != null && event.getStartAt().isBefore(Instant.now())) {
                continue;
            }

            double score = 10.0;
            String reason = "Upcoming cultural workshop in your area of interest.";
            String reasonTa = "உங்கள் ஆர்வமுள்ள துறையில் வரவிருக்கும் பண்பாட்டுப் பயிலரங்கம்.";

            if (event.getArtForm() != null && preferredArtFormIds.contains(event.getArtForm().getId())) {
                score += 40.0;
                reason = "Workshops for " + event.getArtForm().getName() + " in your active learning area.";
                reasonTa = "உங்கள் கற்றல் பிரிவான " + event.getArtForm().getNameTa() + " குறித்த பயிலரங்கம்.";
            }

            if (event.getDistrict() != null && preferredDistrictIds.contains(event.getDistrict().getId())) {
                score += 15.0; // District cultural-learning interaction bonus
            }

            RecommendationDto dto = new RecommendationDto();
            dto.setId(UUID.randomUUID());
            dto.setItemType("EVENT");
            dto.setItemId(event.getId());
            dto.setTitle(event.getTitle());
            dto.setTitleTa(event.getTitleTa());
            dto.setDescription(event.getDescription());
            dto.setDescriptionTa(event.getDescriptionTa());
            dto.setTargetUrl("/event/" + event.getSlug());
            if (event.getArtForm() != null) {
                dto.setArtFormName(event.getArtForm().getName());
                dto.setArtFormNameTa(event.getArtForm().getNameTa());
            }
            dto.setReason(reason);
            dto.setReasonTa(reasonTa);
            dto.setRank((int) score);
            candidates.add(dto);
        }

        // Candidate 3: Art Forms for Exploration
        List<ArtForm> allArtForms = artFormRepository.findAll();
        for (ArtForm artForm : allArtForms) {
            if (!preferredArtFormIds.contains(artForm.getId())) {
                RecommendationDto dto = new RecommendationDto();
                dto.setId(UUID.randomUUID());
                dto.setItemType("ART_FORM");
                dto.setItemId(artForm.getId());
                dto.setTitle("Discover " + artForm.getName());
                dto.setTitleTa(artForm.getNameTa() + " கலையைக் கண்டறியவும்");
                dto.setDescription(artForm.getDescription());
                dto.setDescriptionTa(artForm.getDescriptionTa());
                dto.setTargetUrl("/art-form/" + artForm.getSlug());
                dto.setArtFormName(artForm.getName());
                dto.setArtFormNameTa(artForm.getNameTa());
                dto.setReason("Explore new traditional art forms.");
                dto.setReasonTa("புதிய பாரம்பரியக் கலைப் பிரிவுகளைக் கண்டறியுங்கள்.");
                dto.setRank(20);
                candidates.add(dto);
            }
        }

        // Sort candidates by internal score descending
        candidates.sort(Comparator.comparingInt(RecommendationDto::getRank).reversed());

        // Apply diversity constraint: Maximum 2 consecutive items from the same Art Form
        List<RecommendationDto> diverseFeed = applyDiversityFilter(candidates);

        // Assign clean 1-indexed sequential rank
        for (int i = 0; i < diverseFeed.size(); i++) {
            diverseFeed.get(i).setRank(i + 1);
        }

        RecommendationFeedDto feed = new RecommendationFeedDto();
        feed.setRecommendations(diverseFeed);
        feed.setGeneratedAt(Instant.now());
        feed.setIsColdStart(false);
        return feed;
    }

    private RecommendationFeedDto getColdStartFeed() {
        List<RecommendationDto> items = new ArrayList<>();
        List<Course> topCourses = courseRepository.findByPublishedTrue();

        int rank = 1;
        for (Course course : topCourses) {
            RecommendationDto dto = new RecommendationDto();
            dto.setId(UUID.randomUUID());
            dto.setItemType("COURSE");
            dto.setItemId(course.getId());
            dto.setTitle(course.getTitle());
            dto.setTitleTa(course.getTitleTa());
            dto.setDescription(course.getDescription());
            dto.setDescriptionTa(course.getDescriptionTa());
            dto.setImageUrl(null);

            dto.setTargetUrl("/course/" + course.getSlug());
            if (course.getArtForm() != null) {
                dto.setArtFormName(course.getArtForm().getName());
                dto.setArtFormNameTa(course.getArtForm().getNameTa());
            }
            dto.setReason("Featured Cultural Content — Recommended for beginner learners.");
            dto.setReasonTa("சிறப்புப் பாரம்பரிய உள்ளடக்கம் — தொடக்கநிலை கற்பவர்களுக்குப் பரிந்துரைக்கப்படுகிறது.");
            dto.setRank(rank++);
            items.add(dto);
        }

        List<CulturalEvent> publishedEvents = eventRepository.findByStatus("PUBLISHED");
        for (CulturalEvent event : publishedEvents) {
            RecommendationDto dto = new RecommendationDto();
            dto.setId(UUID.randomUUID());
            dto.setItemType("EVENT");
            dto.setItemId(event.getId());
            dto.setTitle(event.getTitle());
            dto.setTitleTa(event.getTitleTa());
            dto.setDescription(event.getDescription());
            dto.setDescriptionTa(event.getDescriptionTa());
            dto.setTargetUrl("/event/" + event.getSlug());
            if (event.getArtForm() != null) {
                dto.setArtFormName(event.getArtForm().getName());
                dto.setArtFormNameTa(event.getArtForm().getNameTa());
            }
            dto.setReason("Featured Cultural Workshop.");
            dto.setReasonTa("சிறப்புப் பண்பாட்டுப் பயிலரங்கம்.");
            dto.setRank(rank++);
            items.add(dto);
        }

        RecommendationFeedDto feed = new RecommendationFeedDto();
        feed.setRecommendations(items);
        feed.setGeneratedAt(Instant.now());
        feed.setIsColdStart(true);
        return feed;
    }

    private List<RecommendationDto> applyDiversityFilter(List<RecommendationDto> candidates) {
        List<RecommendationDto> result = new ArrayList<>();
        String lastArtForm = null;
        int consecutiveCount = 0;

        for (RecommendationDto candidate : candidates) {
            String currentArtForm = candidate.getArtFormName();
            if (currentArtForm != null && currentArtForm.equalsIgnoreCase(lastArtForm)) {
                consecutiveCount++;
            } else {
                lastArtForm = currentArtForm;
                consecutiveCount = 1;
            }

            if (consecutiveCount <= 2) {
                result.add(candidate);
            }
        }
        return result;
    }
}
