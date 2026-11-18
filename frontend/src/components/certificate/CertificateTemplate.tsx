import React from 'react';
import { Certificate } from '../../types/certificate';

interface CertificateTemplateProps {
  certificate: Certificate;
  language?: 'en' | 'ta';
}

export const CertificateTemplate: React.FC<CertificateTemplateProps> = ({
  certificate,
  language = 'en'
}) => {
  const isTa = language === 'ta';
  const formattedDate = new Date(certificate.issuedAt).toLocaleDateString(
    isTa ? 'ta-IN' : 'en-US',
    { year: 'numeric', month: 'long', day: 'numeric' }
  );

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="max-w-4xl mx-auto my-6">
      {/* Certificate Frame */}
      <div className="bg-stone-900 border-4 border-amber-600/80 p-8 rounded-xl shadow-2xl relative overflow-hidden text-stone-200 printable-certificate">
        {/* Background Decorative Pattern */}
        <div className="absolute inset-0 opacity-5 pointer-events-none bg-[radial-gradient(#f59e0b_1px,transparent_1px)] [background-size:16px_16px]" />

        <div className="border-2 border-dashed border-amber-500/40 p-8 rounded-lg relative text-center">
          {/* Header Badge */}
          <div className="w-16 h-16 bg-amber-600/20 border-2 border-amber-500 rounded-full flex items-center justify-center mx-auto mb-4 text-3xl shadow-lg">
            📜
          </div>

          <h4 className="text-amber-500 uppercase tracking-widest text-xs font-bold mb-1">
            {isTa ? 'மரபகம் தமிழ் பாரம்பரிய கலைகள் பீடம்' : 'MARABAGAM TAMIL HERITAGE ACADEMY'}
          </h4>

          <h1 className="text-3xl sm:text-4xl font-extrabold text-amber-200 tracking-wide mb-6">
            {isTa ? 'சான்றிதழ் (Certificate of Completion)' : 'CERTIFICATE OF COMPLETION'}
          </h1>

          <p className="text-stone-400 text-sm italic mb-2">
            {isTa ? 'இச்சான்றிதழ் இவருக்கு பெருமையுடன் வழங்கப்படுகிறது:' : 'This is to certify that'}
          </p>

          <h2 className="text-2xl sm:text-3xl font-bold text-amber-300 border-b border-amber-500/30 pb-2 inline-block px-8 my-2">
            {certificate.recipientNameSnapshot}
          </h2>

          <p className="text-stone-300 text-sm mt-4 max-w-xl mx-auto leading-relaxed">
            {isTa
              ? `அவர்கள் வெற்றிகரமாக பாடத்திட்டத்தை நிறைவு செய்து, ${certificate.finalScorePercentage.toFixed(1)}% தேர்ச்சி மதிப்பெண் பெற்றுள்ளார்:`
              : `has successfully completed the curriculum requirements with a score of ${certificate.finalScorePercentage.toFixed(1)}% in:`}
          </p>

          <h3 className="text-xl font-bold text-amber-400 my-4 text-stone-100">
            {isTa && certificate.courseTitleTaSnapshot
              ? certificate.courseTitleTaSnapshot
              : certificate.courseTitleSnapshot}
          </h3>

          <div className="grid grid-cols-2 gap-8 mt-10 pt-6 border-t border-stone-800 text-left text-xs text-stone-400">
            <div>
              <p><span className="font-semibold text-stone-300">{isTa ? 'வழங்கப்பட்ட நாள்:' : 'Date Issued:'}</span> {formattedDate}</p>
              <p className="mt-1 font-mono text-[11px] text-amber-500/80">
                {isTa ? 'குறியீடு:' : 'Code:'} {certificate.certificateCode}
              </p>
            </div>

            <div className="text-right flex flex-col items-end">
              <div className="bg-stone-950 p-2 border border-stone-800 rounded inline-block text-center">
                <a
                  href={certificate.verificationUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-[10px] text-amber-400 underline block"
                >
                  {isTa ? 'சரிபார்க்க இங்கே சொடுக்கவும்' : 'Verify Certificate'}
                </a>
              </div>
              <p className="text-[10px] text-stone-500 mt-1">
                {isTa ? 'HMAC டிஜிட்டல் கையொப்பமிட்டது' : 'Authentic HMAC Signed Record'}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="flex justify-end mt-4 gap-4">
        <button
          onClick={handlePrint}
          className="px-4 py-2 bg-amber-600 text-stone-950 rounded-lg font-bold hover:bg-amber-500 transition-colors shadow"
        >
          🖨️ {isTa ? 'அச்சிடுக / பதிவிறக்குக (PDF)' : 'Print / Download PDF'}
        </button>
      </div>
    </div>
  );
};
