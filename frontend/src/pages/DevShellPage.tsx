import React from 'react';
import { useTranslation } from 'react-i18next';
import { StatusBadge } from '../components/common/StatusBadge';
import { useHealth } from '../hooks/useHealth';
import { Terminal, ShieldCheck, Layers, Cpu, Database, Server } from 'lucide-react';

export const DevShellPage: React.FC = () => {
  const { t } = useTranslation();
  const { backend, aiService, database, loading } = useHealth();

  return (
    <div className="space-y-8 max-w-4xl mx-auto">
      {/* Banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-amber-950/40 via-slate-900 to-rose-950/40 border border-amber-500/20 p-8 shadow-2xl">
        <div className="relative z-10 space-y-4">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs font-semibold">
            <ShieldCheck className="w-4 h-4 text-amber-400" />
            <span>PLATFORM MICROSERVICES OPERATIONAL</span>
          </div>
          
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white">
            {t('title')}
          </h2>

          <p className="text-slate-300 text-base max-w-2xl leading-relaxed">
            {t('subtitle')}
          </p>
        </div>
      </div>

      {/* Live Service Health Section */}
      <div className="space-y-4">
        <h3 className="text-lg font-bold text-slate-200 flex items-center space-x-2">
          <Server className="w-5 h-5 text-amber-400" />
          <span>Container & Service Integration Status</span>
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <StatusBadge label={t('status.backend')} status={backend} loading={loading} />
          <StatusBadge label={t('status.aiService')} status={aiService} loading={loading} />
          <StatusBadge label={t('status.database')} status={database} loading={loading} />
        </div>
      </div>

      {/* Tech Baseline Grid */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-6">
        <h3 className="text-lg font-bold text-slate-200 flex items-center space-x-2">
          <Layers className="w-5 h-5 text-amber-400" />
          <span>{t('techStack.title')}</span>
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="p-4 bg-slate-950/80 rounded-xl border border-slate-850 space-y-2">
            <div className="flex items-center text-amber-400 text-sm font-semibold space-x-2">
              <Cpu className="w-4 h-4" />
              <span>Frontend Shell</span>
            </div>
            <p className="text-xs text-slate-400">{t('techStack.frontend')}</p>
          </div>

          <div className="p-4 bg-slate-950/80 rounded-xl border border-slate-850 space-y-2">
            <div className="flex items-center text-emerald-400 text-sm font-semibold space-x-2">
              <Server className="w-4 h-4" />
              <span>Backend Core</span>
            </div>
            <p className="text-xs text-slate-400">{t('techStack.backend')}</p>
          </div>

          <div className="p-4 bg-slate-950/80 rounded-xl border border-slate-850 space-y-2">
            <div className="flex items-center text-cyan-400 text-sm font-semibold space-x-2">
              <Terminal className="w-4 h-4" />
              <span>AI Service</span>
            </div>
            <p className="text-xs text-slate-400">{t('techStack.ai')}</p>
          </div>

          <div className="p-4 bg-slate-950/80 rounded-xl border border-slate-850 space-y-2">
            <div className="flex items-center text-rose-400 text-sm font-semibold space-x-2">
              <Database className="w-4 h-4" />
              <span>Database & Orchestration</span>
            </div>
            <p className="text-xs text-slate-400">{t('techStack.container')}</p>
          </div>
        </div>
      </div>
    </div>
  );
};
