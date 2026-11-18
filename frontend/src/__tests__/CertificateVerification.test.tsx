import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { CertificateTemplate } from '../components/certificate/CertificateTemplate';
import { Certificate } from '../types/certificate';

describe('Certificate Components', () => {
  const mockCertificate: Certificate = {
    id: 'cert-123',
    certificateCode: 'MBG-CERT-2026-F89A12B3C4D5',
    courseId: 'course-silambam',
    recipientNameSnapshot: 'Kovalan Learner',
    courseTitleSnapshot: 'Silambam Fencing Fundamentals',
    courseTitleTaSnapshot: 'சிலம்பம் தற்காப்புக் கலை அடிப்படைகள்',
    finalScorePercentage: 92.5,
    issuedAt: '2026-09-08T10:00:00Z',
    signatureHmac: 'a1b2c3d4e5f6',
    keyVersion: 1,
    status: 'ISSUED',
    verificationUrl: 'http://localhost:5173/certificate/MBG-CERT-2026-F89A12B3C4D5/verify'
  };

  it('renders CertificateTemplate with recipient, course title, and verification link', () => {
    render(<CertificateTemplate certificate={mockCertificate} />);

    expect(screen.getByText(/Kovalan Learner/i)).toBeInTheDocument();
    expect(screen.getByText(/Silambam Fencing Fundamentals/i)).toBeInTheDocument();
    expect(screen.getByText(/MBG-CERT-2026-F89A12B3C4D5/i)).toBeInTheDocument();
    expect(screen.getByText(/92.5%/i)).toBeInTheDocument();
    expect(screen.getByText(/Verify Certificate/i)).toBeInTheDocument();
  });
});
