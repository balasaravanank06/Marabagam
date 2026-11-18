import React from 'react';
import { UserCheck, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface CameraCalibrationProps {
  isCalibrated: boolean;
  message: string;
  onProceed: () => void;
}

export const CameraCalibration: React.FC<CameraCalibrationProps> = ({ isCalibrated, message, onProceed }) => {
  const { t } = useTranslation();

  return (
    <div className="bg-amber-950/40 border border-amber-500/30 rounded-2xl p-6 text-center space-y-4">
      <div className="inline-flex p-4 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-400">
        <UserCheck className="w-10 h-10 animate-pulse" />
      </div>

      <div>
        <h4 className="text-lg font-bold font-serif text-amber-100">
          {t('movement.calibrationTitle', 'Learner Framing & Camera Alignment Check')}
        </h4>
        <p className="text-xs text-amber-300/80 max-w-md mx-auto mt-1">
          {t(
            'movement.calibrationSubtitle',
            'Ensure full body is visible facing the camera in bright lighting with a single learner.'
          )}
        </p>
      </div>

      <div className="flex items-center justify-center gap-2 py-2">
        {isCalibrated ? (
          <div className="flex items-center gap-2 text-emerald-400 bg-emerald-950/60 px-4 py-2 rounded-full border border-emerald-500/40 text-sm font-medium">
            <CheckCircle2 className="w-4 h-4" />
            <span>{message}</span>
          </div>
        ) : (
          <div className="flex items-center gap-2 text-amber-300 bg-amber-950/60 px-4 py-2 rounded-full border border-amber-500/40 text-sm font-medium">
            <AlertTriangle className="w-4 h-4 text-amber-400 animate-spin" />
            <span>{message}</span>
          </div>
        )}
      </div>

      <div className="pt-2">
        <button
          onClick={onProceed}
          disabled={!isCalibrated}
          className={`px-8 py-3 rounded-xl font-bold transition-all shadow-lg text-sm ${
            isCalibrated
              ? 'bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-600 hover:to-teal-700 text-emerald-950 cursor-pointer'
              : 'bg-amber-900/40 text-amber-500/50 cursor-not-allowed border border-amber-800/40'
          }`}
        >
          {t('movement.startPracticeNow', 'Begin Movement Practice')}
        </button>
      </div>
    </div>
  );
};
