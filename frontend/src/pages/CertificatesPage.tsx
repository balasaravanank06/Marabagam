import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Award, BookOpen, AlertCircle, RefreshCw, CheckCircle, ExternalLink } from 'lucide-react';
import { certificateService } from '../services/certificateService';
import { Certificate } from '../types/certificate';
import { CertificateTemplate } from '../components/certificate/CertificateTemplate';

export const CertificatesPage: React.FC = () => {
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const [certificates, setCertificates] = useState<Certificate[]>([]);
  const [selectedCert, setSelectedCert] = useState<Certificate | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchCertificates();
  }, []);

  const fetchCertificates = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await certificateService.getUserCertificates();
      setCertificates(list || []);
      if (list && list.length > 0) {
        setSelectedCert(list[0]);
      } else {
        setSelectedCert(null);
      }
    } catch (err: any) {
      const errorMsg =
        err?.response?.data?.message ||
        err?.message ||
        (isTa ? 'சான்றிதழ்களை ஏற்றுவதில் தோல்வி' : 'Failed to fetch certificates');
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[50vh] flex flex-col items-center justify-center space-y-4">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-500 border-t-transparent" />
        <p className="text-sm text-slate-400">
          {isTa ? 'சான்றிதழ்கள் ஏற்றப்படுகின்றன...' : 'Loading your certificates...'}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <header className="border-b border-slate-800 pb-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-amber-400 text-xs font-semibold uppercase tracking-wider mb-1">
            <Award className="w-4 h-4" />
            <span>{isTa ? 'டிஜிட்டல் அங்கீகாரம்' : 'Official Credentials'}</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white">
            {isTa ? 'என் சான்றிதழ்கள் (My Certificates)' : 'My Certificates'}
          </h1>
          <p className="text-slate-400 text-xs sm:text-sm mt-1">
            {isTa
              ? 'பாடநெறிகளை நிறைவு செய்து பெறப்பட்ட சரிபார்க்கக்கூடிய டிஜிட்டல் சான்றிதழ்கள்.'
              : 'View and download your tamper-proof certificates of course completion.'}
          </p>
        </div>

        <Link
          to="/courses"
          className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-amber-300 text-xs font-semibold border border-slate-700 transition self-start sm:self-auto"
        >
          <BookOpen className="w-4 h-4 text-amber-400" />
          <span>{isTa ? 'பாட அட்டவணை' : 'Course Directory'}</span>
        </Link>
      </header>

      {/* Actual API failure */}
      {error ? (
        <div className="bg-rose-950/60 border border-rose-500/50 p-6 rounded-2xl text-center space-y-4 shadow-xl">
          <div className="w-12 h-12 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center mx-auto">
            <AlertCircle className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-base font-bold text-rose-200">
              {isTa ? 'சான்றிதழ்களைப் பெற முடியவில்லை' : 'Failed to fetch certificates'}
            </h3>
            <p className="text-xs text-rose-300/80 mt-1 max-w-md mx-auto">{error}</p>
          </div>
          <button
            onClick={fetchCertificates}
            className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold transition shadow-lg shadow-rose-900/30"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>{isTa ? 'மீண்டும் முயற்சிக்கவும் (Retry)' : 'Retry'}</span>
          </button>
        </div>
      ) : certificates.length === 0 ? (
        /* User has no certificates */
        <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-10 md:p-14 text-center backdrop-blur-md shadow-2xl max-w-2xl mx-auto space-y-5">
          <div className="w-20 h-20 rounded-2xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center mx-auto text-4xl shadow-inner">
            📜
          </div>
          <div className="space-y-2">
            <h2 className="text-2xl font-bold text-amber-200">
              Your learning journey starts here.
            </h2>
            <p className="text-slate-300 text-sm max-w-lg mx-auto leading-relaxed">
              You haven't earned any certificates yet. Complete a course and its required assessments to earn your first certificate.
            </p>
            <p className="text-xs text-slate-500 mt-2">
              {isTa
                ? 'உங்கள் கற்றல் பயணம் இங்கே தொடங்குகிறது. ஒரு பாரம்பரிய கலைப் படிப்பை நிறைவு செய்து உங்கள் முதல் சான்றிதழைப் பெறுங்கள்.'
                : 'Tamil heritage certificates are cryptographically verifiable and signed with tamper-proof HMAC verification.'}
            </p>
          </div>
          <div className="pt-2">
            <Link
              to="/courses"
              className="inline-flex items-center space-x-2 px-6 py-3 rounded-xl bg-gradient-to-r from-amber-600 to-amber-500 text-slate-950 font-bold hover:brightness-110 shadow-lg shadow-amber-500/20 transition-all text-sm"
            >
              <BookOpen className="w-4 h-4" />
              <span>{isTa ? 'பாடங்களைப் பார்வையிடுக / Explore Courses' : 'Explore Course Directory'}</span>
            </Link>
          </div>
        </div>
      ) : (
        /* User has certificates */
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Certificate records list */}
          <div className="lg:col-span-5 space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
              {isTa ? `சான்றிதழ்கள் (${certificates.length})` : `Issued Certificates (${certificates.length})`}
            </h3>

            <div className="space-y-3">
              {certificates.map((cert) => {
                const isSelected = selectedCert?.id === cert.id;
                const formattedDate = new Date(cert.issuedAt).toLocaleDateString(
                  isTa ? 'ta-IN' : 'en-US',
                  { year: 'numeric', month: 'short', day: 'numeric' }
                );

                return (
                  <div
                    key={cert.id}
                    onClick={() => setSelectedCert(cert)}
                    className={`p-4 rounded-xl border text-left transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-amber-950/40 border-amber-500/60 shadow-lg shadow-amber-950/30'
                        : 'bg-slate-900/50 border-slate-800 hover:border-slate-700 hover:bg-slate-900/80 text-slate-300'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="space-y-1 flex-1 min-w-0">
                        <h4 className="font-bold text-sm text-white truncate">
                          {isTa && cert.courseTitleTaSnapshot
                            ? cert.courseTitleTaSnapshot
                            : cert.courseTitleSnapshot}
                        </h4>
                        <p className="text-xs text-slate-400">
                          {isTa ? 'பெறுநர்:' : 'Learner:'}{' '}
                          <span className="text-slate-200 font-medium">{cert.recipientNameSnapshot}</span>
                        </p>
                      </div>

                      <span
                        className={`inline-flex items-center space-x-1 px-2 py-0.5 rounded text-[10px] font-bold shrink-0 ${
                          cert.status === 'ISSUED'
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                            : 'bg-rose-500/10 text-rose-400 border border-rose-500/30'
                        }`}
                      >
                        <CheckCircle className="w-3 h-3" />
                        <span>{cert.status === 'ISSUED' ? 'Verified' : 'Revoked'}</span>
                      </span>
                    </div>

                    <div className="mt-3 pt-3 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-2 text-[11px] text-slate-400 font-mono">
                      <span>ID: {cert.certificateCode}</span>
                      <span className="font-sans text-slate-500">{formattedDate}</span>
                    </div>

                    <div className="mt-3 flex items-center justify-between gap-2 pt-2">
                      <span className="text-xs text-amber-400 font-semibold">
                        Score: {cert.finalScorePercentage?.toFixed(1)}%
                      </span>

                      <div className="flex items-center space-x-2">
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedCert(cert);
                          }}
                          className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium transition"
                        >
                          {isTa ? 'பார்வையிடு' : 'View'}
                        </button>
                        <a
                          href={cert.verificationUrl}
                          target="_blank"
                          rel="noreferrer"
                          onClick={(e) => e.stopPropagation()}
                          className="p-1 rounded text-slate-400 hover:text-amber-300 transition"
                          title="Verify Certificate"
                        >
                          <ExternalLink className="w-3.5 h-3.5" />
                        </a>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Certificate Preview and Actions */}
          <div className="lg:col-span-7">
            {selectedCert ? (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                    {isTa ? 'சான்றிதழ் முன்னோட்டம்' : 'Certificate View'}
                  </h3>
                  <span className="text-xs font-mono text-amber-400">
                    {selectedCert.certificateCode}
                  </span>
                </div>
                <CertificateTemplate certificate={selectedCert} language={isTa ? 'ta' : 'en'} />
              </div>
            ) : (
              <div className="h-full min-h-[300px] flex items-center justify-center rounded-2xl bg-slate-900/40 border border-slate-800 text-slate-500 text-xs">
                {isTa ? 'சான்றிதழைத் தேர்ந்தெடுக்கவும்' : 'Select a certificate from the list to view.'}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
