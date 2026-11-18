package com.marabagam.art.repository;

import com.marabagam.art.domain.HeritageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HeritageLocationRepository extends JpaRepository<HeritageLocation, UUID> {
    Optional<HeritageLocation> findBySlug(String slug);
    List<HeritageLocation> findByDistrictIdAndStatus(Long districtId, String status);
    List<HeritageLocation> findByArtFormSlugAndStatus(String slug, String status);
    List<HeritageLocation> findByStatus(String status);
    long countByDistrictIdAndStatus(Long districtId, String status);
    long countByStatus(String status);
}
