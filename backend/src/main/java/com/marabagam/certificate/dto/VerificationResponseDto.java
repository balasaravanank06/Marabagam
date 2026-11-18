package com.marabagam.certificate.dto;

import com.marabagam.certificate.domain.CertificateStatus;
import java.time.Instant;

public class VerificationResponseDto {
    private String certificateCode;
    private String recipientNameSnapshot;
    private String courseTitleSnapshot;
    private String courseTitleTaSnapshot;
    private Instant issuedAt;
    private CertificateStatus status;
    private Boolean authentic;
    private String verificationUrl;

    public VerificationResponseDto() {
    }

    public String getCertificateCode() {
        return certificateCode;
    }

    public void setCertificateCode(String certificateCode) {
        this.certificateCode = certificateCode;
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

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public CertificateStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateStatus status) {
        this.status = status;
    }

    public Boolean getAuthentic() {
        return authentic;
    }

    public void setAuthentic(Boolean authentic) {
        this.authentic = authentic;
    }

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public void setVerificationUrl(String verificationUrl) {
        this.verificationUrl = verificationUrl;
    }
}
