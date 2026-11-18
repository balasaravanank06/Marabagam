package com.marabagam.community.service;

import com.marabagam.common.audit.Auditable;
import com.marabagam.community.domain.CommunityComment;
import com.marabagam.community.domain.CommunityPost;
import com.marabagam.community.domain.CommunityReport;
import com.marabagam.community.dto.CommunityReportCreateDto;
import com.marabagam.community.dto.CommunityReportDto;
import com.marabagam.community.repository.CommunityCommentRepository;
import com.marabagam.community.repository.CommunityPostRepository;
import com.marabagam.community.repository.CommunityReportRepository;
import com.marabagam.notification.service.NotificationService;
import com.marabagam.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class ModerationService {

    private final CommunityReportRepository reportRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final NotificationService notificationService;

    public ModerationService(CommunityReportRepository reportRepository,
                             CommunityPostRepository postRepository,
                             CommunityCommentRepository commentRepository,
                             NotificationService notificationService) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    @Auditable(action = "SUBMIT_REPORT", resourceType = "COMMUNITY_REPORT")
    public CommunityReportDto submitReport(CommunityReportCreateDto createDto, User reporter) {
        // Rate limiting check: max 5 reports per user per hour
        long recentCount = reportRepository.countByReporterIdAndCreatedAtAfter(reporter.getId(), Instant.now().minus(1, ChronoUnit.HOURS));
        if (recentCount >= 5) {
            throw new IllegalStateException("Rate limit exceeded: maximum 5 reports per hour");
        }

        // Polymorphic target validation
        UUID targetId = createDto.getTargetId();
        String targetType = createDto.getTargetType().toUpperCase();

        if ("POST".equals(targetType)) {
            CommunityPost post = postRepository.findById(targetId)
                    .orElseThrow(() -> new NoSuchElementException("Target post not found with ID: " + targetId));
            // Self-report prevention
            if (post.getAuthor() != null && post.getAuthor().getId().equals(reporter.getId())) {
                throw new IllegalArgumentException("Self-reporting prohibited: cannot report your own post");
            }
        } else if ("COMMENT".equals(targetType)) {
            CommunityComment comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new NoSuchElementException("Target comment not found with ID: " + targetId));
            // Self-report prevention
            if (comment.getAuthor() != null && comment.getAuthor().getId().equals(reporter.getId())) {
                throw new IllegalArgumentException("Self-reporting prohibited: cannot report your own comment");
            }
        } else {
            throw new IllegalArgumentException("Invalid report target type: " + targetType + ". Must be POST or COMMENT");
        }

        // Unique report constraint check: UNIQUE(reporter_id, target_type, target_id)
        Optional<CommunityReport> existing = reportRepository.findByReporterIdAndTargetTypeAndTargetId(reporter.getId(), targetType, targetId);
        if (existing.isPresent()) {
            throw new IllegalStateException("You have already reported this content");
        }

        CommunityReport report = new CommunityReport();
        report.setReporter(reporter);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(createDto.getReason());
        report.setDetails(createDto.getDetails());
        report.setStatus("PENDING"); // Post/comment remains VISIBLE. NO automatic hiding.

        CommunityReport saved = reportRepository.save(report);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<CommunityReportDto> getPendingReports(Pageable pageable) {
        return reportRepository.findByStatusOrderByCreatedAtAsc("PENDING", pageable)
                .map(this::toDto);
    }

    @Transactional
    @Auditable(action = "ACTION_REPORT", resourceType = "COMMUNITY_REPORT")
    public CommunityReportDto actionReport(UUID reportId, String action, String notes, User moderator) {
        CommunityReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NoSuchElementException("Report not found: " + reportId));

        report.setActionedBy(moderator);
        report.setActionedAt(Instant.now());
        report.setActionTaken(action);

        if ("DISMISS".equalsIgnoreCase(action)) {
            report.setStatus("DISMISSED");
            // Target content remains VISIBLE
        } else if ("HIDE".equalsIgnoreCase(action) || "REMOVE".equalsIgnoreCase(action)) {
            report.setStatus("ACTIONED");
            String newTargetStatus = "HIDE".equalsIgnoreCase(action) ? "HIDDEN" : "REMOVED";

            if ("POST".equals(report.getTargetType())) {
                postRepository.findById(report.getTargetId()).ifPresent(post -> {
                    post.setStatus(newTargetStatus);
                    postRepository.save(post);
                });
            } else if ("COMMENT".equals(report.getTargetType())) {
                commentRepository.findById(report.getTargetId()).ifPresent(comment -> {
                    comment.setStatus(newTargetStatus);
                    commentRepository.save(comment);
                });
            }
        } else {
            throw new IllegalArgumentException("Invalid moderation action: " + action + ". Must be DISMISS, HIDE, or REMOVE");
        }

        CommunityReport saved = reportRepository.save(report);

        // Notify reporter of report outcome
        notificationService.sendNotification(
                report.getReporter(),
                "MODERATION_UPDATE",
                "Moderation Report Processed",
                "இயக்கக் கோரிக்கை பரிசீலிக்கப்பட்டது",
                "Your content report has been reviewed and " + ("DISMISS".equalsIgnoreCase(action) ? "dismissed after review." : "actioned by a moderator."),
                "உங்கள் பதிவு பெறப்பட்ட அறிக்கை பரிசீலிக்கப்பட்டது.",
                "MODERATION_UPDATE|" + report.getId(),
                "REPORT",
                report.getId()
        );

        return toDto(saved);
    }

    private CommunityReportDto toDto(CommunityReport report) {
        CommunityReportDto dto = new CommunityReportDto();
        dto.setId(report.getId());
        if (report.getReporter() != null) {
            dto.setReporterName(report.getReporter().getFullName());
        }
        dto.setTargetType(report.getTargetType());
        dto.setTargetId(report.getTargetId());
        dto.setReason(report.getReason());
        dto.setDetails(report.getDetails());
        dto.setStatus(report.getStatus());
        dto.setActionTaken(report.getActionTaken());
        if (report.getActionedBy() != null) {
            dto.setActionedByName(report.getActionedBy().getFullName());
        }
        dto.setActionedAt(report.getActionedAt());
        dto.setCreatedAt(report.getCreatedAt());

        // Attach content snippet for moderator context
        if ("POST".equals(report.getTargetType())) {
            postRepository.findById(report.getTargetId())
                    .ifPresent(p -> dto.setTargetContentSnippet(p.getTitle() + " - " + (p.getContent().length() > 50 ? p.getContent().substring(0, 50) + "..." : p.getContent())));
        } else if ("COMMENT".equals(report.getTargetType())) {
            commentRepository.findById(report.getTargetId())
                    .ifPresent(c -> dto.setTargetContentSnippet(c.getContent().length() > 50 ? c.getContent().substring(0, 50) + "..." : c.getContent()));
        }

        return dto;
    }
}
