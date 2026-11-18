package com.marabagam.art.repository;

import com.marabagam.art.domain.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    Optional<District> findByNameIgnoreCase(String name);
    Optional<District> findByCodeIgnoreCase(String code);
    org.springframework.data.domain.Page<District> findByNameContainingIgnoreCaseOrNameTaContainingIgnoreCase(String query, String queryTa, org.springframework.data.domain.Pageable pageable);
}
