package com.marabagam.certificate.repository;

import com.marabagam.certificate.domain.Certificate;
import com.marabagam.certificate.domain.CertificateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    Optional<Certificate> findByCertificateCode(String certificateCode);
    Optional<Certificate> findByUserIdAndCourseId(UUID userId, UUID courseId);
    List<Certificate> findByUserId(UUID userId);
    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);
    int countByUserId(UUID userId);
}
