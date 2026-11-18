import React, { useState } from 'react';
import { X, AlertTriangle } from 'lucide-react';
import { communityService } from '../services/communityService';

interface ReportModalProps {
  isOpen: boolean;
  onClose: () => void;
  targetType: 'POST' | 'COMMENT';
  targetId: string;
  isTamil?: boolean;
}

export const ReportModal: React.FC<ReportModalProps> = ({
  isOpen,
  onClose,
  targetType,
  targetId,
  isTamil = false
}) => {
  const [reason, setReason] = useState('SPAM');
  const [details, setDetails] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await communityService.submitReport({
        targetType,
        targetId,
        reason,
        details
      });
      setSuccess(true);
      setTimeout(() => {
        setSuccess(false);
        onClose();
      }, 1500);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit report');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-sm p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-xl w-full max-w-md p-6 relative shadow-xl">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-200 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-2 mb-4 text-rose-400">
          <AlertTriangle className="w-5 h-5" />
          <h2 className="text-lg font-bold text-slate-100">
            {isTamil ? 'உள்ளடக்கத்தைப் புகாரளிக்கவும்' : 'Report Content'}
          </h2>
        </div>

        {success ? (
          <div className="p-4 rounded-lg bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-sm font-medium text-center">
            {isTamil ? 'புகார் வெற்றிகரமாகச் சமர்ப்பிக்கப்பட்டது.' : 'Report submitted successfully. Thank you for keeping Marabagam safe.'}
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs">
                {error}
              </div>
            )}

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                {isTamil ? 'காரணம்' : 'Reason for Reporting'}
              </label>
              <select
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
              >
                <option value="SPAM">Spam or Unrelated</option>
                <option value="HARASSMENT">Harassment or Offensive Language</option>
                <option value="MISINFORMATION">Misinformation or Misleading Content</option>
                <option value="OTHER">Other Policy Violation</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                {isTamil ? 'கூடுதல் தகவல்கள் (விருப்பத்தேர்வு)' : 'Additional Details (Optional)'}
              </label>
              <textarea
                value={details}
                onChange={(e) => setDetails(e.target.value)}
                rows={3}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg p-3 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                placeholder={isTamil ? 'விவரங்களை உள்ளிடவும்...' : 'Provide details...'}
              />
            </div>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-xs font-semibold text-slate-400 hover:text-slate-200 transition-colors"
              >
                {isTamil ? 'ரத்து செய்' : 'Cancel'}
              </button>

              <button
                type="submit"
                disabled={loading}
                className="px-4 py-2 text-xs font-semibold rounded-lg bg-rose-600 hover:bg-rose-500 text-white transition-colors disabled:opacity-50"
              >
                {loading ? (isTamil ? 'சமர்ப்பிக்கிறது...' : 'Submitting...') : (isTamil ? 'புகாரளி' : 'Submit Report')}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
