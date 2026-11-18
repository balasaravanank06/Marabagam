import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { certificateService } from '../services/certificateService';
import { VerificationResponse } from '../types/certificate';

export const CertificateVerifyPage: React.FC = () => {
  const { code } = useParams<{ code: string }>();
  const [verification, setVerification] = useState<VerificationResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (code) {
      verify();
    }
  }, [code]);

  const verify = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await certificateService.verifyCertificate(code!);
      setVerification(res);
    } catch (err: any) {
      setError(err.message || 'Verification failed. Certificate code may be invalid.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-stone-950 text-stone-200 p-6 flex flex-col items-center justify-center">
      <div className="bg-stone-900 border border-amber-900/50 rounded-2xl p-8 max-w-xl w-full shadow-2xl">
        <div className="text-center mb-6">
          <div className="w-16 h-16 bg-amber-950/80 border-2 border-amber-500 rounded-full flex items-center justify-center mx-auto text-3xl mb-3">
            📜
          </div>
          <h1 className="text-2xl font-extrabold text-amber-200">
            சான்றிதழ் சரிபார்ப்பு (Certificate Verification)
          </h1>
          <p className="text-stone-400 text-xs mt-1">
            Marabagam Tamil Digital Heritage Official Verification Portal
          </p>
        </div>

        {loading ? (
          <div className="flex justify-center py-8">
            <div className="animate-spin rounded-full h-10 w-10 border-4 border-amber-500 border-t-transparent" />
          </div>
        ) : error ? (
          <div className="bg-red-950/80 border border-red-500/50 p-6 rounded-xl text-center">
            <div className="text-3xl text-red-400 mb-2">✕</div>
            <h3 className="text-lg font-bold text-red-300">சான்றிதழ் செல்லுபடியாகவில்லை</h3>
            <p className="text-xs text-red-400 mt-1">{error}</p>
          </div>
        ) : verification ? (
          <div className="space-y-6">
            <div
              className={`p-4 rounded-xl border text-center ${
                verification.authentic && verification.status === 'ISSUED'
                  ? 'bg-emerald-950/60 border-emerald-500/60 text-emerald-300'
                  : 'bg-red-950/60 border-red-500/60 text-red-300'
              }`}
            >
              <div className="text-2xl font-bold mb-1">
                {verification.authentic && verification.status === 'ISSUED'
                  ? '✓ உண்மையான சான்றிதழ் (Authentic Certificate)'
                  : '✕ ரத்து செய்யப்பட்டது (Revoked Certificate)'}
              </div>
              <p className="text-xs">
                {verification.authentic && verification.status === 'ISSUED'
                  ? 'Cryptographically verified using HMAC-SHA256 signature'
                  : 'This certificate has been revoked by platform administrators'}
              </p>
            </div>

            <div className="bg-stone-950 p-4 rounded-xl border border-stone-800 space-y-3 text-sm">
              <div>
                <span className="text-stone-400 text-xs block">சான்றிதழ் குறியீடு (Code):</span>
                <span className="font-mono text-amber-300 font-bold">{verification.certificateCode}</span>
              </div>
              <div>
                <span className="text-stone-400 text-xs block">பெறுநர் (Recipient Name):</span>
                <span className="font-bold text-stone-200">{verification.recipientNameSnapshot}</span>
              </div>
              <div>
                <span className="text-stone-400 text-xs block">பாடநெறி (Course Title):</span>
                <span className="font-semibold text-amber-200">
                  {verification.courseTitleTaSnapshot || verification.courseTitleSnapshot}
                </span>
              </div>
              <div>
                <span className="text-stone-400 text-xs block">வழங்கப்பட்ட நாள் (Issued At):</span>
                <span className="text-stone-300">
                  {new Date(verification.issuedAt).toLocaleDateString()}
                </span>
              </div>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
};
