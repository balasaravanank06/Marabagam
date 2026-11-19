package com.marabagam.certificate.service;

import com.marabagam.certificate.domain.Certificate;
import com.marabagam.certificate.domain.CertificateStatus;
import com.marabagam.certificate.dto.CertificateDto;
import com.marabagam.certificate.dto.VerificationResponseDto;
import com.marabagam.certificate.repository.CertificateRepository;
import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.learning.domain.Course;
import com.marabagam.learning.repository.CourseRepository;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${marabagam.certificate.secret:marabagam_development_certificate_secret_key_2026}")
    private String certificateSecret;

    @Value("${marabagam.certificate.public-base-url:http://localhost:5173}")
    private String publicBaseUrl;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public Certificate generateCertificate(User user, Course course, double finalScore) {
        Optional<Certificate> existing = certificateRepository.findByUserIdAndCourseId(user.getId(), course.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        String certificateCode = generateSecureCertificateCode();
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        String canonicalPayload = buildCanonicalPayload(
                certificateCode,
                user.getId(),
                course.getId(),
                user.getDisplayName(),
                course.getTitle(),
                finalScore,
                issuedAt
        );

        String signatureHmac = computeHmacSha256(canonicalPayload, certificateSecret);

        Certificate cert = new Certificate();
        cert.setCertificateCode(certificateCode);
        cert.setUser(user);
        cert.setCourse(course);
        cert.setRecipientNameSnapshot(user.getDisplayName());
        cert.setCourseTitleSnapshot(course.getTitle());
        cert.setCourseTitleTaSnapshot(course.getTitleTa());
        cert.setFinalScorePercentage(finalScore);
        cert.setIssuedAt(issuedAt);
        cert.setSignatureHmac(signatureHmac);
        cert.setKeyVersion(1);
        cert.setStatus(CertificateStatus.ISSUED);

        Certificate saved = certificateRepository.save(cert);
        log.info("Certificate [{}] issued for user {} in course {}", saved.getCertificateCode(), user.getId(), course.getId());
        return saved;
    }

    public VerificationResponseDto verifyCertificate(String certificateCode) {
        Certificate cert = certificateRepository.findByCertificateCode(certificateCode)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with code: " + certificateCode));

        String canonicalPayload = buildCanonicalPayload(
                cert.getCertificateCode(),
                cert.getUser().getId(),
                cert.getCourse().getId(),
                cert.getRecipientNameSnapshot(),
                cert.getCourseTitleSnapshot(),
                cert.getFinalScorePercentage(),
                cert.getIssuedAt()
        );

        String expectedSignature = computeHmacSha256(canonicalPayload, certificateSecret);

        // Constant-time comparison to prevent timing attacks, checking normalized payload with second precision
        boolean isAuthentic = (MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                cert.getSignatureHmac().getBytes(StandardCharsets.UTF_8)
        ) || MessageDigest.isEqual(
                computeHmacSha256(
                        cert.getCertificateCode() + "|" + cert.getUser().getId() + "|" + cert.getCourse().getId() + "|" +
                        cert.getRecipientNameSnapshot() + "|" + cert.getCourseTitleSnapshot() + "|" +
                        cert.getFinalScorePercentage() + "|" + cert.getIssuedAt().toString(),
                        certificateSecret
                ).getBytes(StandardCharsets.UTF_8),
                cert.getSignatureHmac().getBytes(StandardCharsets.UTF_8)
        )) && cert.getStatus() == CertificateStatus.ISSUED;

        VerificationResponseDto response = new VerificationResponseDto();
        response.setCertificateCode(cert.getCertificateCode());
        response.setRecipientNameSnapshot(cert.getRecipientNameSnapshot());
        response.setCourseTitleSnapshot(cert.getCourseTitleSnapshot());
        response.setCourseTitleTaSnapshot(cert.getCourseTitleTaSnapshot());
        response.setIssuedAt(cert.getIssuedAt());
        response.setStatus(cert.getStatus());
        response.setAuthentic(isAuthentic);
        response.setVerificationUrl(publicBaseUrl + "/certificate/" + cert.getCertificateCode() + "/verify");

        return response;
    }

    public List<CertificateDto> getUserCertificates(UUID userId) {
        return certificateRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CertificateDto getCertificateById(UUID certificateId, UUID userId) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certificateId));

        if (!cert.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Certificate access denied or not found");
        }

        return convertToDto(cert);
    }

    @Transactional
    public void revokeCertificate(UUID certificateId, String reason) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certificateId));

        cert.setStatus(CertificateStatus.REVOKED);
        cert.setRevocationReason(reason);
        cert.setRevokedAt(Instant.now());
        certificateRepository.save(cert);

        log.warn("Certificate [{}] revoked. Reason: {}", cert.getCertificateCode(), reason);
    }

    private String generateSecureCertificateCode() {
        byte[] randomBytes = new byte[6];
        SECURE_RANDOM.nextBytes(randomBytes);
        String hex = HexFormat.of().formatHex(randomBytes).toUpperCase();
        return "MBG-CERT-" + Year.now().getValue() + "-" + hex;
    }

    private String buildCanonicalPayload(String code, UUID userId, UUID courseId, String name, String title, double score, Instant issuedAt) {
        Instant normalizedInstant = issuedAt != null ? issuedAt.truncatedTo(ChronoUnit.SECONDS) : Instant.EPOCH;
        return code + "|" + userId + "|" + courseId + "|" + name + "|" + title + "|" + score + "|" + normalizedInstant.toString();
    }

    private String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error computing HMAC-SHA256 signature", e);
        }
    }

    private CertificateDto convertToDto(Certificate cert) {
        CertificateDto dto = new CertificateDto();
        dto.setId(cert.getId());
        dto.setCertificateCode(cert.getCertificateCode());
        dto.setCourseId(cert.getCourse().getId());
        dto.setRecipientNameSnapshot(cert.getRecipientNameSnapshot());
        dto.setCourseTitleSnapshot(cert.getCourseTitleSnapshot());
        dto.setCourseTitleTaSnapshot(cert.getCourseTitleTaSnapshot());
        dto.setFinalScorePercentage(cert.getFinalScorePercentage());
        dto.setIssuedAt(cert.getIssuedAt());
        dto.setSignatureHmac(cert.getSignatureHmac());
        dto.setKeyVersion(cert.getKeyVersion());
        dto.setStatus(cert.getStatus());
        dto.setVerificationUrl(publicBaseUrl + "/certificate/" + cert.getCertificateCode() + "/verify");
        return dto;
    }
}
