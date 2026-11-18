export type CertificateStatus = 'ISSUED' | 'REVOKED';

export interface Certificate {
  id: string;
  certificateCode: string;
  courseId: string;
  recipientNameSnapshot: string;
  courseTitleSnapshot: string;
  courseTitleTaSnapshot: string;
  finalScorePercentage: number;
  issuedAt: string;
  signatureHmac: string;
  keyVersion: number;
  status: CertificateStatus;
  verificationUrl: string;
}

export interface VerificationResponse {
  certificateCode: string;
  recipientNameSnapshot: string;
  courseTitleSnapshot: string;
  courseTitleTaSnapshot: string;
  issuedAt: string;
  status: CertificateStatus;
  authentic: boolean;
  verificationUrl: string;
}
