package com.marabagam.certificate.domain;

import com.marabagam.learning.domain.Course;
import com.marabagam.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "certificate_code", nullable = false, unique = true)
    private String certificateCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "recipient_name_snapshot", nullable = false)
    private String recipientNameSnapshot;

    @Column(name = "course_title_snapshot", nullable = false)
    private String courseTitleSnapshot;

    @Column(name = "course_title_ta_snapshot", nullable = false)
    private String courseTitleTaSnapshot;

    @Column(name = "final_score_percentage", nullable = false)
    private Double finalScorePercentage;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "signature_hmac", nullable = false)
    private String signatureHmac;

    @Column(name = "key_version", nullable = false)
    private Integer keyVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateStatus status = CertificateStatus.ISSUED;

    @Column(name = "revocation_reason", columnDefinition = "TEXT")
    private String revocationReason;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public Certificate() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.issuedAt == null) {
            this.issuedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCertificateCode() {
        return certificateCode;
    }

    public void setCertificateCode(String certificateCode) {
        this.certificateCode = certificateCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getRecipientNameSnapshot() {
        return recipientNameSnapshot;
    }

    public void setRecipientNameSnapshot(String recipientNameSnapshot) {
        this.recipientNameSnapshot = recipientNameSnapshot;
    }

    public String getCourseTitleSnapshot() {
        return courseTitleSnapshot;
    }

    public void setCourseTitleSnapshot(String courseTitleSnapshot) {
        this.courseTitleSnapshot = courseTitleSnapshot;
    }

    public String getCourseTitleTaSnapshot() {
        return courseTitleTaSnapshot;
    }

    public void setCourseTitleTaSnapshot(String courseTitleTaSnapshot) {
        this.courseTitleTaSnapshot = courseTitleTaSnapshot;
    }

    public Double getFinalScorePercentage() {
        return finalScorePercentage;
    }

    public void setFinalScorePercentage(Double finalScorePercentage) {
        this.finalScorePercentage = finalScorePercentage;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getSignatureHmac() {
        return signatureHmac;
    }

    public void setSignatureHmac(String signatureHmac) {
        this.signatureHmac = signatureHmac;
    }

    public Integer getKeyVersion() {
        return keyVersion;
    }

    public void setKeyVersion(Integer keyVersion) {
        this.keyVersion = keyVersion;
    }

    public CertificateStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateStatus status) {
        this.status = status;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
