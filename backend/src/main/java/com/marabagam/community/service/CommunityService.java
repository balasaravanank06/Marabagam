package com.marabagam.community.service;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.common.audit.Auditable;
import com.marabagam.community.domain.CommunityComment;
import com.marabagam.community.domain.CommunityPost;
import com.marabagam.community.dto.*;
import com.marabagam.community.repository.CommunityCommentRepository;
import com.marabagam.community.repository.CommunityPostRepository;
import com.marabagam.notification.service.NotificationService;
import com.marabagam.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final ArtFormRepository artFormRepository;
    private final NotificationService notificationService;

    public CommunityService(CommunityPostRepository postRepository,
                            CommunityCommentRepository commentRepository,
                            ArtFormRepository artFormRepository,
                            NotificationService notificationService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.artFormRepository = artFormRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Page<CommunityPostDto> getPublicPosts(String category, UUID artFormId, Pageable pageable) {
        Page<CommunityPost> posts;
        if (category != null && !category.isBlank()) {
            posts = postRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, "VISIBLE", pageable);
        } else if (artFormId != null) {
            posts = postRepository.findByArtFormIdAndStatusOrderByCreatedAtDesc(artFormId, "VISIBLE", pageable);
        } else {
            posts = postRepository.findByStatusOrderByCreatedAtDesc("VISIBLE", pageable);
        }
        return posts.map(this::toPostSummaryDto);
    }

    @Transactional(readOnly = true)
    public CommunityPostDto getPostDetail(UUID postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Community post not found: " + postId));

        if (!"VISIBLE".equals(post.getStatus())) {
            throw new NoSuchElementException("Community post not found: " + postId);
        }

        CommunityPostDto dto = toPostSummaryDto(post);
        List<CommunityComment> topComments = commentRepository.findByPostIdAndParentCommentIdIsNullAndStatusOrderByCreatedAtAsc(postId, "VISIBLE");

        List<CommunityCommentDto> commentDtos = topComments.stream().map(parent -> {
            CommunityCommentDto pDto = toCommentDto(parent);
            List<CommunityComment> replies = commentRepository.findByParentCommentIdAndStatusOrderByCreatedAtAsc(parent.getId(), "VISIBLE");
            pDto.setReplies(replies.stream().map(this::toCommentDto).collect(Collectors.toList()));
            return pDto;
        }).collect(Collectors.toList());

        dto.setComments(commentDtos);
        return dto;
    }

    @Transactional
    @Auditable(action = "CREATE_POST", resourceType = "COMMUNITY_POST")
    public CommunityPostDto createPost(CommunityPostCreateDto createDto, User author) {
        // Plain text sanitization (escapes HTML tags)
        String safeTitle = sanitizePlainText(createDto.getTitle(), 200);
        String safeContent = sanitizePlainText(createDto.getContent(), 2000);

        ArtForm artForm = null;
        if (createDto.getArtFormId() != null) {
            artForm = artFormRepository.findById(createDto.getArtFormId()).orElse(null);
        }

        CommunityPost post = new CommunityPost();
        post.setAuthor(author);
        post.setArtForm(artForm);
        post.setCategory(createDto.getCategory() != null ? createDto.getCategory() : "DISCUSSION");
        post.setTitle(safeTitle);
        post.setContent(safeContent);
        post.setStatus("VISIBLE"); // Immediately VISIBLE upon creation (UGC)

        CommunityPost saved = postRepository.save(post);
        return toPostSummaryDto(saved);
    }

    @Transactional
    @Auditable(action = "UPDATE_POST", resourceType = "COMMUNITY_POST")
    public CommunityPostDto updatePost(UUID postId, CommunityPostUpdateDto updateDto, User author) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found: " + postId));

        if (post.getAuthor() == null || !post.getAuthor().getId().equals(author.getId())) {
            throw new AccessDeniedException("You are not authorized to edit this post");
        }

        // 15-minute edit window
        if (Instant.now().isAfter(post.getCreatedAt().plusSeconds(900))) {
            throw new IllegalStateException("Edit window has expired (15 minutes maximum)");
        }

        if (updateDto.getTitle() != null) {
            post.setTitle(sanitizePlainText(updateDto.getTitle(), 200));
        }
        if (updateDto.getContent() != null) {
            post.setContent(sanitizePlainText(updateDto.getContent(), 2000));
        }

        return toPostSummaryDto(postRepository.save(post));
    }

    @Transactional
    @Auditable(action = "DELETE_POST", resourceType = "COMMUNITY_POST")
    public void deletePost(UUID postId, User author) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found: " + postId));

        boolean isAuthor = post.getAuthor() != null && post.getAuthor().getId().equals(author.getId());
        boolean isAdmin = author.getRole() != null && author.getRole().name().equals("ADMIN");
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this post");
        }

        post.setStatus("DELETED");
        post.setDeletedAt(Instant.now());
        post.setDeletedBy(author);
        postRepository.save(post);
    }

    @Transactional
    @Auditable(action = "ADD_COMMENT", resourceType = "COMMUNITY_COMMENT")
    public CommunityCommentDto addComment(UUID postId, CommunityCommentCreateDto createDto, User author) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found: " + postId));

        if (!"VISIBLE".equals(post.getStatus())) {
            throw new IllegalArgumentException("Cannot comment on hidden or deleted post");
        }

        CommunityComment parentComment = null;
        if (createDto.getParentCommentId() != null) {
            parentComment = commentRepository.findById(createDto.getParentCommentId())
                    .orElseThrow(() -> new NoSuchElementException("Parent comment not found"));
            // Single-depth rule: reply must reference a top-level comment
            if (parentComment.getParentComment() != null) {
                throw new IllegalArgumentException("Comments are restricted to a maximum of one reply level (2-tier)");
            }
        }

        String safeContent = sanitizePlainText(createDto.getContent(), 500);

        CommunityComment comment = new CommunityComment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setParentComment(parentComment);
        comment.setContent(safeContent);
        comment.setStatus("VISIBLE");

        CommunityComment saved = commentRepository.save(comment);

        // Send COMMUNITY_REPLY notification to post author or parent comment author
        User notifyRecipient = (parentComment != null && parentComment.getAuthor() != null) ?
                parentComment.getAuthor() : post.getAuthor();

        if (notifyRecipient != null && !notifyRecipient.getId().equals(author.getId())) {
            notificationService.sendNotification(
                    notifyRecipient,
                    "COMMUNITY_REPLY",
                    "New Reply on Community Discussion",
                    "சமூகக் கலந்துரையாடலில் புதிய பதில்",
                    author.getFullName() + " replied to your discussion '" + post.getTitle() + "'",
                    author.getFullName() + " உங்கள் கலந்துரையாடலுக்குப் பதிலளித்தார்.",
                    "COMMUNITY_REPLY|" + post.getId() + "|" + notifyRecipient.getId() + "|" + (System.currentTimeMillis() / 600000), // 10-min cooldown window
                    "POST",
                    post.getId()
            );
        }

        return toCommentDto(saved);
    }

    @Transactional
    @Auditable(action = "DELETE_COMMENT", resourceType = "COMMUNITY_COMMENT")
    public void deleteComment(UUID commentId, User author) {
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found: " + commentId));

        boolean isAuthor = comment.getAuthor() != null && comment.getAuthor().getId().equals(author.getId());
        boolean isAdmin = author.getRole() != null && author.getRole().name().equals("ADMIN");
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this comment");
        }

        comment.setStatus("DELETED");
        comment.setDeletedAt(Instant.now());
        comment.setDeletedBy(author);
        commentRepository.save(comment);
    }

    private String sanitizePlainText(String input, int maxLen) {
        if (input == null) return "";
        String escaped = HtmlUtils.htmlEscape(input.trim());
        if (escaped.length() > maxLen) {
            return escaped.substring(0, maxLen);
        }
        return escaped;
    }

    private CommunityPostDto toPostSummaryDto(CommunityPost post) {
        CommunityPostDto dto = new CommunityPostDto();
        dto.setId(post.getId());
        if (post.getAuthor() != null) {
            dto.setAuthorDisplayName(post.getAuthor().getFullName());
            dto.setAuthorRole(post.getAuthor().getRole() != null ? post.getAuthor().getRole().name() : "LEARNER");
            dto.setAuthorAvatarUrl(post.getAuthor().getAvatarUrl());
        } else {
            dto.setAuthorDisplayName("Anonymous Community Member");
            dto.setAuthorRole("LEARNER");
        }
        if (post.getArtForm() != null) {
            dto.setArtFormId(post.getArtForm().getId());
            dto.setArtFormName(post.getArtForm().getName());
        }
        dto.setCategory(post.getCategory());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setStatus(post.getStatus());
        dto.setCommentCount(commentRepository.countByPostIdAndStatus(post.getId(), "VISIBLE"));
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        return dto;
    }

    private CommunityCommentDto toCommentDto(CommunityComment comment) {
        CommunityCommentDto dto = new CommunityCommentDto();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPost().getId());
        if (comment.getAuthor() != null) {
            dto.setAuthorDisplayName(comment.getAuthor().getFullName());
            dto.setAuthorRole(comment.getAuthor().getRole() != null ? comment.getAuthor().getRole().name() : "LEARNER");
            dto.setAuthorAvatarUrl(comment.getAuthor().getAvatarUrl());
        } else {
            dto.setAuthorDisplayName("Anonymous Member");
            dto.setAuthorRole("LEARNER");
        }
        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }
        dto.setContent(comment.getContent());
        dto.setStatus(comment.getStatus());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
