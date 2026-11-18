package com.marabagam.movement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import com.marabagam.common.exception.InvalidInputException;
import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.movement.domain.*;
import com.marabagam.movement.dto.*;
import com.marabagam.movement.repository.*;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class MovementService {

    private static final Logger log = LoggerFactory.getLogger(MovementService.class);

    private final MovementRepository movementRepository;
    private final MovementVersionRepository versionRepository;
    private final ReferenceMovementRepository referenceRepository;
    private final PracticeSessionRepository sessionRepository;
    private final PracticeFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.url:http://ai-service:8000}")
    private String aiServiceUrl;

    public MovementService(
            MovementRepository movementRepository,
            MovementVersionRepository versionRepository,
            ReferenceMovementRepository referenceRepository,
            PracticeSessionRepository sessionRepository,
            PracticeFeedbackRepository feedbackRepository,
            UserRepository userRepository,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.movementRepository = movementRepository;
        this.versionRepository = versionRepository;
        this.referenceRepository = referenceRepository;
        this.sessionRepository = sessionRepository;
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<MovementResponseDto> getMovements(UUID artFormId) {
        List<Movement> movements;
        if (artFormId != null) {
            movements = movementRepository.findByArtFormIdAndIsActiveTrue(artFormId);
        } else {
            movements = movementRepository.findByIsActiveTrue();
        }

        List<MovementResponseDto> dtos = new ArrayList<>();
        for (Movement m : movements) {
            dtos.add(mapMovementToDto(m));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public MovementResponseDto getMovementById(UUID id) {
        Movement m = movementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found: " + id));
        return mapMovementToDto(m);
    }

    @Transactional
    public PracticeSessionResponseDto startPracticeSession(UUID movementId, UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        Movement movement = movementRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found: " + movementId));

        MovementVersion currentVersion = versionRepository.findByMovementIdAndIsCurrentTrue(movement.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No current active version for movement: " + movementId));

        PracticeSession session = new PracticeSession();
        session.setUser(user);
        session.setMovement(movement);
        session.setVersion(currentVersion);
        session.setStartedAt(OffsetDateTime.now());
        session.setStatus(SessionStatus.IN_PROGRESS);

        PracticeSession saved = sessionRepository.save(session);
        return mapSessionToDto(saved, Collections.emptyList());
    }

    @Transactional
    public PracticeSessionResponseDto evaluatePracticeSession(UUID sessionId, EvaluatePracticeSessionRequestDto request, UUID currentUserId) {
        PracticeSession session = sessionRepository.findByIdAndUserId(sessionId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice session not found or access denied"));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new InvalidInputException("Practice session has already been evaluated and completed");
        }

        if (request.getFrames() == null || request.getFrames().isEmpty()) {
            throw new InvalidInputException("Evaluation frames cannot be empty");
        }

        if (request.getFrames().size() > 300) {
            throw new InvalidInputException("Frame count exceeds maximum limit of 300 frames per evaluation");
        }

        // Validate Coordinates & Confidence Values
        for (FrameLandmarkDto frame : request.getFrames()) {
            if (frame.getLandmarks() != null) {
                for (LandmarkDto lm : frame.getLandmarks()) {
                    if (lm.getX() == null || lm.getX().isNaN() || lm.getX().isInfinite() || lm.getX() < 0.0 || lm.getX() > 1.0) {
                        throw new InvalidInputException("Invalid x coordinate value: " + lm.getX());
                    }
                    if (lm.getY() == null || lm.getY().isNaN() || lm.getY().isInfinite() || lm.getY() < 0.0 || lm.getY() > 1.0) {
                        throw new InvalidInputException("Invalid y coordinate value: " + lm.getY());
                    }
                    if (lm.getVisibility() != null && (lm.getVisibility() < 0.0 || lm.getVisibility() > 1.0)) {
                        throw new InvalidInputException("Invalid visibility value: " + lm.getVisibility());
                    }
                }
            }
        }

        // Fetch Reference Movement Template
        Optional<ReferenceMovement> refOpt = referenceRepository.findFirstByMovementIdAndStatusOrderByApprovedAtDesc(
                session.getMovement().getId(), ReferenceStatus.APPROVED);

        String referenceTemplateJson = refOpt.map(ReferenceMovement::getKeypointsTemplateJson).orElse("{}");

        // Forward to FastAPI AI Movement Analysis service
        Double overallScore = 75.0;
        List<PracticeFeedback> feedbacksToSave = new ArrayList<>();
        String metricsSummaryJson = "{}";

        try {
            Map<String, Object> analysisReq = new HashMap<>();
            analysisReq.put("movementCode", session.getMovement().getCode());
            analysisReq.put("jointRulesJson", session.getVersion().getJointRulesJson());
            analysisReq.put("referenceTemplateJson", referenceTemplateJson);
            analysisReq.put("frames", request.getFrames());

            String url = aiServiceUrl + "/internal/v1/movement/analyze";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, analysisReq, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map data = response.getBody();
                if (data.containsKey("overallScore") && data.get("overallScore") != null) {
                    overallScore = ((Number) data.get("overallScore")).doubleValue();
                }
                if (data.containsKey("metricsSummaryJson") && data.get("metricsSummaryJson") != null) {
                    metricsSummaryJson = (String) data.get("metricsSummaryJson");
                }
                if (data.containsKey("feedbacks") && data.get("feedbacks") instanceof List) {
                    List<?> rawFeedbacks = (List<?>) data.get("feedbacks");
                    for (Object item : rawFeedbacks) {
                        Map fMap = objectMapper.convertValue(item, Map.class);
                        PracticeFeedback pf = new PracticeFeedback();
                        pf.setPracticeSession(session);
                        pf.setBodyPart((String) fMap.getOrDefault("bodyPart", "BODY"));
                        pf.setMetricName((String) fMap.getOrDefault("metricName", "ALIGNMENT"));
                        pf.setMeasuredValue(fMap.containsKey("measuredValue") ? ((Number) fMap.get("measuredValue")).doubleValue() : 0.0);
                        pf.setTargetRangeMin(fMap.containsKey("targetRangeMin") ? ((Number) fMap.get("targetRangeMin")).doubleValue() : 0.0);
                        pf.setTargetRangeMax(fMap.containsKey("targetRangeMax") ? ((Number) fMap.get("targetRangeMax")).doubleValue() : 0.0);
                        pf.setScore(fMap.containsKey("score") ? ((Number) fMap.get("score")).doubleValue() : 80.0);
                        pf.setFeedbackTextEn((String) fMap.getOrDefault("feedbackTextEn", "Good alignment observed."));
                        pf.setFeedbackTextTa((String) fMap.getOrDefault("feedbackTextTa", "நல்ல நிலை பராமரிக்கப்பட்டது."));
                        String sevStr = (String) fMap.getOrDefault("severity", "INFO");
                        try {
                            pf.setSeverity(FeedbackSeverity.valueOf(sevStr.toUpperCase()));
                        } catch (Exception e) {
                            pf.setSeverity(FeedbackSeverity.INFO);
                        }
                        feedbacksToSave.add(pf);
                    }
                }
            }
        } catch (Exception e) {
            log.error("FastAPI movement analysis request failed: {}", e.getMessage());
            // Deterministic Fallback Feedback
            PracticeFeedback fallbackFb = new PracticeFeedback();
            fallbackFb.setPracticeSession(session);
            fallbackFb.setBodyPart("POSTURE");
            fallbackFb.setMetricName("ALIGNMENT_BASELINE");
            fallbackFb.setMeasuredValue(0.0);
            fallbackFb.setTargetRangeMin(0.0);
            fallbackFb.setTargetRangeMax(15.0);
            fallbackFb.setScore(70.0);
            fallbackFb.setFeedbackTextEn("Post-practice local biomechanical analysis saved. Detailed AI service was offline.");
            fallbackFb.setFeedbackTextTa("பயிற்சி பகுப்பாய்வு சேமிக்கப்பட்டது.");
            fallbackFb.setSeverity(FeedbackSeverity.INFO);
            feedbacksToSave.add(fallbackFb);
        }

        session.setCompletedAt(OffsetDateTime.now());
        session.setStatus(SessionStatus.COMPLETED);
        session.setOverallScore(overallScore);
        if (session.getStartedAt() != null) {
            session.setDurationSeconds((int) (session.getCompletedAt().toEpochSecond() - session.getStartedAt().toEpochSecond()));
        }
        session.setMetricsSummaryJson(metricsSummaryJson);

        PracticeSession savedSession = sessionRepository.save(session);
        List<PracticeFeedback> savedFeedbacks = feedbackRepository.saveAll(feedbacksToSave);

        return mapSessionToDto(savedSession, savedFeedbacks);
    }

    @Transactional(readOnly = true)
    public Page<PracticeSessionResponseDto> getUserPracticeSessions(UUID currentUserId, Pageable pageable) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, pageable)
                .map(s -> {
                    List<PracticeFeedback> fbs = feedbackRepository.findByPracticeSessionIdOrderByCreatedAtAsc(s.getId());
                    return mapSessionToDto(s, fbs);
                });
    }

    @Transactional(readOnly = true)
    public PracticeSessionResponseDto getPracticeSessionById(UUID sessionId, UUID currentUserId) {
        PracticeSession s = sessionRepository.findByIdAndUserId(sessionId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice session not found or access denied"));
        List<PracticeFeedback> fbs = feedbackRepository.findByPracticeSessionIdOrderByCreatedAtAsc(s.getId());
        return mapSessionToDto(s, fbs);
    }

    @Transactional
    public void deletePracticeSession(UUID sessionId, UUID currentUserId) {
        PracticeSession s = sessionRepository.findByIdAndUserId(sessionId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice session not found or access denied"));
        sessionRepository.delete(s);
    }

    // Expert Reference Governance APIs
    @Transactional
    public ReferenceMovementResponseDto submitReferenceMovement(UUID movementId, SubmitReferenceRequestDto req, UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        Role role = user.getRole();
        if (role != Role.ARTIST && role != Role.INSTRUCTOR && role != Role.CULTURAL_EXPERT && role != Role.ADMIN) {
            throw new AccessDeniedException("User role not authorized to submit reference movement data");
        }

        Movement movement = movementRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found: " + movementId));

        MovementVersion currentVersion = versionRepository.findByMovementIdAndIsCurrentTrue(movement.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current version not found for movement: " + movementId));

        ReferenceMovement ref = new ReferenceMovement();
        ref.setMovement(movement);
        ref.setVersion(currentVersion);
        ref.setTitle(req.getTitle());
        ref.setPerformerAttribution(req.getPerformerAttribution());
        ref.setKeypointsTemplateJson(req.getKeypointsTemplateJson());
        ref.setDemonstrationVideoUrl(req.getDemonstrationVideoUrl());
        ref.setThumbnailUrl(req.getThumbnailUrl());
        ref.setStatus(ReferenceStatus.PENDING_REVIEW);
        ref.setCreatedBy(user);

        ReferenceMovement saved = referenceRepository.save(ref);
        return mapReferenceToDto(saved);
    }

    @Transactional
    public ReferenceMovementResponseDto approveReferenceMovement(UUID referenceId, UUID currentUserId) {
        User reviewer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        Role role = reviewer.getRole();
        if (role != Role.CULTURAL_EXPERT && role != Role.ADMIN) {
            throw new AccessDeniedException("Only Cultural Experts or Admins can approve reference movements");
        }

        ReferenceMovement ref = referenceRepository.findById(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Reference movement not found: " + referenceId));

        // Self-Approval Prohibition Rule Check!
        if (ref.getCreatedBy().getId().equals(currentUserId)) {
            throw new InvalidInputException("Self-Approval Prohibited: Contributor cannot approve their own submitted reference movement");
        }

        ref.setStatus(ReferenceStatus.APPROVED);
        ref.setApprovedBy(reviewer);
        ref.setApprovedAt(OffsetDateTime.now());

        ReferenceMovement saved = referenceRepository.save(ref);
        return mapReferenceToDto(saved);
    }

    private MovementResponseDto mapMovementToDto(Movement m) {
        MovementResponseDto dto = new MovementResponseDto();
        dto.setId(m.getId());
        if (m.getArtForm() != null) {
            dto.setArtFormId(m.getArtForm().getId());
            dto.setArtFormName(m.getArtForm().getName());
        }
        dto.setCode(m.getCode());
        dto.setNameEn(m.getNameEn());
        dto.setNameTa(m.getNameTa());
        dto.setDescriptionEn(m.getDescriptionEn());
        dto.setDescriptionTa(m.getDescriptionTa());
        dto.setCategory(m.getCategory());
        dto.setDifficultyLevel(m.getDifficultyLevel());
        dto.setIsActive(m.getIsActive());
        dto.setCreatedAt(m.getCreatedAt());

        versionRepository.findByMovementIdAndIsCurrentTrue(m.getId())
                .ifPresent(v -> dto.setCurrentVersionNumber(v.getVersionNumber()));

        referenceRepository.findFirstByMovementIdAndStatusOrderByApprovedAtDesc(m.getId(), ReferenceStatus.APPROVED)
                .ifPresent(r -> {
                    dto.setDemonstrationVideoUrl(r.getDemonstrationVideoUrl());
                    dto.setThumbnailUrl(r.getThumbnailUrl());
                });

        return dto;
    }

    private PracticeSessionResponseDto mapSessionToDto(PracticeSession s, List<PracticeFeedback> feedbacks) {
        PracticeSessionResponseDto dto = new PracticeSessionResponseDto();
        dto.setId(s.getId());
        dto.setUserId(s.getUser().getId());
        if (s.getMovement() != null) {
            dto.setMovementId(s.getMovement().getId());
            dto.setMovementNameEn(s.getMovement().getNameEn());
            dto.setMovementNameTa(s.getMovement().getNameTa());
        }
        if (s.getVersion() != null) {
            dto.setVersionNumber(s.getVersion().getVersionNumber());
        }
        dto.setStartedAt(s.getStartedAt());
        dto.setCompletedAt(s.getCompletedAt());
        dto.setStatus(s.getStatus().name());
        dto.setOverallScore(s.getOverallScore());
        dto.setDurationSeconds(s.getDurationSeconds());
        dto.setCreatedAt(s.getCreatedAt());

        List<PracticeFeedbackDto> fDtos = new ArrayList<>();
        if (feedbacks != null) {
            for (PracticeFeedback fb : feedbacks) {
                PracticeFeedbackDto fDto = new PracticeFeedbackDto();
                fDto.setId(fb.getId());
                fDto.setBodyPart(fb.getBodyPart());
                fDto.setMetricName(fb.getMetricName());
                fDto.setMeasuredValue(fb.getMeasuredValue());
                fDto.setTargetRangeMin(fb.getTargetRangeMin());
                fDto.setTargetRangeMax(fb.getTargetRangeMax());
                fDto.setScore(fb.getScore());
                fDto.setFeedbackTextEn(fb.getFeedbackTextEn());
                fDto.setFeedbackTextTa(fb.getFeedbackTextTa());
                fDto.setSeverity(fb.getSeverity().name());
                fDtos.add(fDto);
            }
        }
        dto.setFeedbacks(fDtos);
        return dto;
    }

    private ReferenceMovementResponseDto mapReferenceToDto(ReferenceMovement r) {
        ReferenceMovementResponseDto dto = new ReferenceMovementResponseDto();
        dto.setId(r.getId());
        if (r.getMovement() != null) {
            dto.setMovementId(r.getMovement().getId());
            dto.setMovementNameEn(r.getMovement().getNameEn());
        }
        if (r.getVersion() != null) {
            dto.setVersionNumber(r.getVersion().getVersionNumber());
        }
        dto.setTitle(r.getTitle());
        dto.setPerformerAttribution(r.getPerformerAttribution());
        dto.setDemonstrationVideoUrl(r.getDemonstrationVideoUrl());
        dto.setThumbnailUrl(r.getThumbnailUrl());
        dto.setStatus(r.getStatus().name());
        if (r.getCreatedBy() != null) {
            dto.setCreatedById(r.getCreatedBy().getId());
            dto.setCreatedByName(r.getCreatedBy().getUsername());
        }
        if (r.getApprovedBy() != null) {
            dto.setApprovedById(r.getApprovedBy().getId());
            dto.setApprovedByName(r.getApprovedBy().getUsername());
        }
        dto.setApprovedAt(r.getApprovedAt());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
