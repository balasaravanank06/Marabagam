package com.marabagam.community.repository;

import com.marabagam.community.domain.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, UUID> {
    Page<CommunityPost> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<CommunityPost> findByCategoryAndStatusOrderByCreatedAtDesc(String category, String status, Pageable pageable);

    @Query("SELECT p FROM CommunityPost p WHERE p.artForm.id = :artFormId AND p.status = :status ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByArtFormIdAndStatusOrderByCreatedAtDesc(@Param("artFormId") UUID artFormId, @Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM CommunityPost p WHERE p.author.id = :authorId")
    List<CommunityPost> findByAuthorId(@Param("authorId") UUID authorId);
}
