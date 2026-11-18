package com.marabagam.community.repository;

import com.marabagam.community.domain.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, UUID> {

    @Query("SELECT c FROM CommunityComment c WHERE c.post.id = :postId AND c.status = :status ORDER BY c.createdAt ASC")
    List<CommunityComment> findByPostIdAndStatusOrderByCreatedAtAsc(@Param("postId") UUID postId, @Param("status") String status);

    @Query("SELECT c FROM CommunityComment c WHERE c.post.id = :postId AND c.parentComment IS NULL AND c.status = :status ORDER BY c.createdAt ASC")
    List<CommunityComment> findByPostIdAndParentCommentIdIsNullAndStatusOrderByCreatedAtAsc(@Param("postId") UUID postId, @Param("status") String status);

    @Query("SELECT c FROM CommunityComment c WHERE c.parentComment.id = :parentCommentId AND c.status = :status ORDER BY c.createdAt ASC")
    List<CommunityComment> findByParentCommentIdAndStatusOrderByCreatedAtAsc(@Param("parentCommentId") UUID parentCommentId, @Param("status") String status);

    @Query("SELECT COUNT(c) FROM CommunityComment c WHERE c.post.id = :postId AND c.status = :status")
    long countByPostIdAndStatus(@Param("postId") UUID postId, @Param("status") String status);
}
