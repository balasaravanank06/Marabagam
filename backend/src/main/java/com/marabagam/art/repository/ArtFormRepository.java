package com.marabagam.art.repository;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.ArtFormStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArtFormRepository extends JpaRepository<ArtForm, UUID> {

    Optional<ArtForm> findBySlugAndStatus(String slug, ArtFormStatus status);

    Optional<ArtForm> findBySlug(String slug);

    boolean existsBySlug(String slug);

    java.util.List<ArtForm> findByDistrictsIdAndStatus(Long districtId, ArtFormStatus status);

    long countByDistrictsIdAndStatus(Long districtId, ArtFormStatus status);

    long countByStatus(ArtFormStatus status);

    @Query("SELECT DISTINCT a FROM ArtForm a " +
           "LEFT JOIN a.categories c " +
           "LEFT JOIN a.districts d " +
           "WHERE (:status IS NULL OR a.status = :status) " +
           "AND (:category IS NULL OR LOWER(c.name) = LOWER(CAST(:category AS string))) " +
           "AND (:district IS NULL OR LOWER(d.name) = LOWER(CAST(:district AS string))) " +
           "AND (:query IS NULL OR (" +
           "    LOWER(a.name) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR " +
           "    LOWER(a.nameTa) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR " +
           "    LOWER(a.slug) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR " +
           "    LOWER(a.shortDescription) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR " +
           "    LOWER(a.shortDescriptionTa) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))" +
           "))")
    Page<ArtForm> searchArtForms(
            @Param("status") ArtFormStatus status,
            @Param("category") String category,
            @Param("district") String district,
            @Param("query") String query,
            Pageable pageable
    );
}
