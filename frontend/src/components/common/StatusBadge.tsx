import React from 'react';
import { CheckCircle2, XCircle, Loader2 } from 'lucide-react';

interface StatusBadgeProps {
  label: string;
  status: boolean | null;
  loading?: boolean;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ label, status, loading }) => {
  return (
    <div className="flex items-center justify-between p-4 bg-slate-900/80 border border-slate-800 rounded-xl backdrop-blur-sm shadow-md transition-all hover:border-slate-700">
      <span className="text-sm font-medium text-slate-300">{label}</span>
      <div className="flex items-center space-x-2">
        {loading || status === null ? (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/20">
            <Loader2 className="w-3.5 h-3.5 mr-1.5 animate-spin" />
            CHECKING
          </span>
        ) : status ? (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <CheckCircle2 className="w-3.5 h-3.5 mr-1.5" />
            ONLINE
          </span>
        ) : (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/20">
            <XCircle className="w-3.5 h-3.5 mr-1.5" />
            OFFLINE
          </span>
        )}
      </div>
    </div>
  );
};
