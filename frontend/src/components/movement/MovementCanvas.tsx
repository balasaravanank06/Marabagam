import React from 'react';
import { Play, Square, RefreshCw, Eye } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface MovementCanvasProps {
  videoRef: React.RefObject<HTMLVideoElement>;
  canvasRef: React.RefObject<HTMLCanvasElement>;
  isCameraActive: boolean;
  isRecording: boolean;
  liveMetrics: { kneeAngle: number; torsoAngle: number; stanceWidthRatio: number };
  onStartRecording: () => void;
  onStopRecording: () => void;
  onStopCamera: () => void;
}

export const MovementCanvas: React.FC<MovementCanvasProps> = ({
  videoRef,
  canvasRef,
  isCameraActive,
  isRecording,
  liveMetrics,
  onStartRecording,
  onStopRecording,
  onStopCamera
}) => {
  const { t } = useTranslation();

  return (
    <div className="relative rounded-2xl overflow-hidden bg-black border border-amber-500/40 shadow-2xl">
      {/* Video element hidden behind canvas rendering */}
      <video
        ref={videoRef}
        playsInline
        muted
        className="hidden"
      />

      {/* Canvas displaying video frame + neon skeletal overlay */}
      <canvas
        ref={canvasRef}
        className="w-full h-[480px] object-cover bg-slate-950"
      />

      {/* Camera State Badge */}
      {isCameraActive && (
        <div className="absolute top-4 left-4 flex items-center gap-2 bg-black/60 backdrop-blur-md px-3 py-1.5 rounded-full border border-emerald-500/40 text-xs text-emerald-400 font-medium">
          <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-ping" />
          <span>{t('movement.onDeviceIndicator', 'Camera Active — On-Device Processing')}</span>
        </div>
      )}

      {/* Live Biomechanical Stance Metrics Overlay */}
      <div className="absolute top-4 right-4 bg-black/75 backdrop-blur-md p-3 rounded-xl border border-amber-500/30 text-xs text-amber-200 space-y-1.5 min-w-[170px]">
        <div className="flex items-center gap-1.5 text-amber-400 font-semibold mb-1 border-b border-amber-900/50 pb-1">
          <Eye className="w-3.5 h-3.5" />
          <span>{t('movement.liveGuidance', 'Local Real-Time Math')}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-amber-300/70">{t('movement.kneeFlex', 'Knee Angle:')}</span>
          <span className="font-mono font-bold text-amber-100">{liveMetrics.kneeAngle}°</span>
        </div>
        <div className="flex justify-between">
          <span className="text-amber-300/70">{t('movement.torsoAngle', 'Torso Inclination:')}</span>
          <span className="font-mono font-bold text-amber-100">{liveMetrics.torsoAngle}°</span>
        </div>
        <div className="flex justify-between">
          <span className="text-amber-300/70">{t('movement.stanceRatio', 'Stance Ratio:')}</span>
          <span className="font-mono font-bold text-amber-100">{liveMetrics.stanceWidthRatio}x</span>
        </div>
      </div>

      {/* Recording Status Overlay */}
      {isRecording && (
        <div className="absolute bottom-20 left-1/2 transform -translate-x-1/2 bg-red-600/90 text-white px-5 py-2 rounded-full font-bold text-sm shadow-xl animate-pulse flex items-center gap-2 border border-red-400">
          <span className="w-3 h-3 rounded-full bg-white animate-ping" />
          <span>{t('movement.recordingPractice', 'Recording Stance Sequence...')}</span>
        </div>
      )}

      {/* Control Buttons Bar */}
      <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex items-center gap-4 bg-black/80 backdrop-blur-md px-6 py-3 rounded-2xl border border-amber-500/30">
        {!isRecording ? (
          <button
            onClick={onStartRecording}
            className="flex items-center gap-2 px-6 py-2.5 bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-600 hover:to-orange-700 text-amber-950 font-bold rounded-xl shadow-lg transition-all text-sm cursor-pointer"
            aria-label="Start recording stance"
          >
            <Play className="w-4 h-4 fill-amber-950" />
            <span>{t('movement.recordStance', 'Record Movement Attempt')}</span>
          </button>
        ) : (
          <button
            onClick={onStopRecording}
            className="flex items-center gap-2 px-6 py-2.5 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl shadow-lg transition-all text-sm cursor-pointer"
            aria-label="Stop recording stance"
          >
            <Square className="w-4 h-4 fill-white" />
            <span>{t('movement.stopAndAnalyze', 'Stop & Evaluate')}</span>
          </button>
        )}

        <button
          onClick={onStopCamera}
          className="p-2.5 rounded-xl bg-amber-900/40 hover:bg-amber-900/70 text-amber-300 border border-amber-700/50 transition-colors"
          title={t('movement.stopCamera', 'Stop Camera')}
          aria-label="Stop camera"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
