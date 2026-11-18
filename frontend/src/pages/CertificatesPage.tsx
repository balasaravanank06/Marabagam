import React, { useEffect, useState } from 'react';
import { certificateService } from '../services/certificateService';
import { Certificate } from '../types/certificate';
import { CertificateTemplate } from '../components/certificate/CertificateTemplate';

export const CertificatesPage: React.FC = () => {
  const [certificates, setCertificates] = useState<Certificate[]>([]);
  const [selectedCert, setSelectedCert] = useState<Certificate | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchCertificates();
  }, []);

  const fetchCertificates = async () => {
    setLoading(true);
    try {
      const list = await certificateService.getUserCertificates();
      setCertificates(list);
      if (list.length > 0) {
        setSelectedCert(list[0]);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load certificates');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-stone-950 text-stone-200 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-500 border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-stone-950 text-stone-200 p-6 md:p-10">
      <div className="max-w-5xl mx-auto space-y-6">
        <header className="border-b border-stone-800 pb-4">
          <h1 className="text-3xl font-extrabold text-amber-200">
            என் சான்றிதழ்கள் (My Certificates)
          </h1>
          <p className="text-stone-400 text-sm mt-1">
            View and download your tamper-proof certificates of course completion.
          </p>
        </header>

        {error ? (
          <div className="bg-red-950/80 border border-red-500/50 p-4 rounded-lg text-red-300 text-center">
            {error}
          </div>
        ) : certificates.length === 0 ? (
          <div className="bg-stone-900 border border-stone-800 rounded-xl p-12 text-center text-stone-400">
            <div className="text-5xl mb-4">📜</div>
            <h3 className="text-xl font-bold text-amber-300 mb-2">சான்றிதழ்கள் எதுவும் இல்லை</h3>
            <p className="text-sm max-w-md mx-auto">
              Complete courses and pass all mandatory quizzes with 80%+ average to earn official Marabagam certificates.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            {/* Sidebar list */}
            <div className="lg:col-span-1 bg-stone-900 border border-stone-800 rounded-xl p-4 space-y-2 h-fit">
              <h3 className="text-xs uppercase tracking-wider text-stone-400 font-bold mb-2">
                வழங்கப்பட்டவை ({certificates.length})
              </h3>
              {certificates.map(c => (
                <button
                  key={c.id}
                  onClick={() => setSelectedCert(c)}
                  className={`w-full text-left p-3 rounded-lg border text-xs transition-colors ${
                    selectedCert?.id === c.id
                      ? 'bg-amber-950/60 border-amber-500 text-amber-200 font-semibold'
                      : 'bg-stone-950/60 border-stone-800 text-stone-400 hover:text-stone-200'
                  }`}
                >
                  <div className="font-bold truncate">{c.courseTitleSnapshot}</div>
                  <div className="text-[10px] text-stone-500 mt-1">
                    {new Date(c.issuedAt).toLocaleDateString()}
                  </div>
                </button>
              ))}
            </div>

            {/* Display Selected Certificate */}
            <div className="lg:col-span-3">
              {selectedCert && <CertificateTemplate certificate={selectedCert} />}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
