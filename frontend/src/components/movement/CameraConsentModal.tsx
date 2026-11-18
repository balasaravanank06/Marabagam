import React from 'react';
import { Camera, ShieldCheck, Lock, Cpu, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface CameraConsentModalProps {
  isOpen: boolean;
  onAccept: () => void;
  onCancel: () => void;
}

export const CameraConsentModal: React.FC<CameraConsentModalProps> = ({ isOpen, onAccept, onCancel }) => {
  const { t } = useTranslation();

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4 animate-fade-in">
      <div className="bg-amber-950/90 border border-amber-500/30 rounded-2xl max-w-lg w-full p-6 shadow-2xl text-amber-50 relative">
        <button
          onClick={onCancel}
          className="absolute top-4 right-4 text-amber-400 hover:text-amber-200 transition-colors"
          aria-label="Close privacy modal"
        >
          <X className="w-6 h-6" />
        </button>

        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 bg-amber-500/20 rounded-xl border border-amber-500/40 text-amber-400">
            <Camera className="w-8 h-8" />
          </div>
          <div>
            <h3 className="text-xl font-bold font-serif text-amber-100">
              {t('movement.privacyTitle', 'AI Movement Coach — Camera Privacy Notice')}
            </h3>
            <p className="text-xs text-amber-300/80">
              {t('movement.privacySubtitle', 'Privacy-first on-device pose estimation')}
            </p>
          </div>
        </div>

        <div className="space-y-3 my-6 text-sm text-amber-200/90 bg-black/40 p-4 rounded-xl border border-amber-900/50">
          <div className="flex items-start gap-3">
            <Cpu className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
            <p>
              <strong>{t('movement.onDeviceHeader', 'On-Device Frame Processing:')}</strong>{' '}
              {t(
                'movement.onDeviceDesc',
                'Raw camera video is processed entirely inside your browser memory buffer. Your camera video is NEVER uploaded, recorded, or saved to Marabagam servers.'
              )}
            </p>
          </div>

          <div className="flex items-start gap-3">
            <ShieldCheck className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
            <p>
              <strong>{t('movement.derivedDataHeader', 'Derived Metric Evaluation:')}</strong>{' '}
              {t(
                'movement.derivedDataDesc',
                'Only derived numerical keypoint joint coordinates (e.g. knee flex angle) required for evaluation are transmitted over HTTPS to generate practice feedback.'
              )}
            </p>
          </div>

          <div className="flex items-start gap-3">
            <Lock className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
            <p>
              <strong>{t('movement.controlHeader', 'Complete Control:')}</strong>{' '}
              {t(
                'movement.controlDesc',
                'Camera access stops immediately when practice ends or when you navigate away. You can permanently delete your practice history at any time.'
              )}
            </p>
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 pt-2">
          <button
            onClick={onCancel}
            className="px-4 py-2.5 rounded-xl border border-amber-700/50 text-amber-300 hover:bg-amber-900/40 transition-colors text-sm font-medium"
          >
            {t('common.cancel', 'Cancel')}
          </button>
          <button
            onClick={onAccept}
            className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-600 hover:to-orange-700 text-amber-950 font-bold transition-all shadow-lg text-sm flex items-center gap-2"
          >
            <Camera className="w-4 h-4" />
            {t('movement.enableCamera', 'Enable Practice Camera')}
          </button>
        </div>
      </div>
    </div>
  );
};
