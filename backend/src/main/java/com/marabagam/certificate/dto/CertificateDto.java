package com.marabagam.certificate.dto;

import com.marabagam.certificate.domain.CertificateStatus;
import java.time.Instant;
import java.util.UUID;

public class CertificateDto {
    private UUID id;
    private String certificateCode;
    private UUID courseId;
    private String recipientNameSnapshot;
    private String courseTitleSnapshot;
    private String courseTitleTaSnapshot;
    private Double finalScorePercentage;
    private Instant issuedAt;
    private String signatureHmac;
    private Integer keyVersion;
    private CertificateStatus status;
    private String verificationUrl;

    public CertificateDto() {
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

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
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

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public void setVerificationUrl(String verificationUrl) {
        this.verificationUrl = verificationUrl;
    }
}
