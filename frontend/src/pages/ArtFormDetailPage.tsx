import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { artService } from '../services/artService';
import { ArtFormDetail } from '../types/art';
import {
  ArrowLeft,
  BookOpen,
  History,
  Shield,
  Music,
  Shirt,
  MapPin,
  Clock,
  Sparkles,
  AlertCircle,
  Video,
} from 'lucide-react';

export const ArtFormDetailPage: React.FC = () => {
  const { slug } = useParams<{ slug: string }>();
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const [art, setArt] = useState<ArtFormDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDetail = async () => {
      if (!slug) return;
      setLoading(true);
      setError(null);
      try {
        const data = await artService.getArtFormBySlug(slug);
        setArt(data);
      } catch (err: any) {
        setError(err.response?.data?.message || 'கலை தகவல்களை ஏற்றுவதில் தோல்வி / Art form not found');
      } finally {
        setLoading(false);
      }
    };

    fetchDetail();
  }, [slug]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] space-y-4">
        <div className="w-12 h-12 border-4 border-amber-500 border-t-transparent rounded-full animate-spin" />
        <p className="text-amber-200 text-sm font-medium animate-pulse">
          கலைத்தகவல்களை ஏற்றுகிறது... / Loading Cultural Encyclopedia...
        </p>
      </div>
    );
  }

  if (error || !art) {
    return (
      <div className="max-w-2xl mx-auto my-12 p-8 rounded-2xl bg-red-950/40 border border-red-500/30 text-center space-y-4">
        <AlertCircle className="w-12 h-12 text-red-400 mx-auto" />
        <h2 className="text-2xl font-bold text-red-200">கலை வடிவம் காணப்படவில்லை / Art Form Not Found</h2>
        <p className="text-sm text-slate-300">
          The requested cultural art form does not exist or has not been published yet.
        </p>
        <Link
          to="/art-forms"
          className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl bg-amber-500 text-slate-950 font-bold text-xs hover:bg-amber-400 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>கலை மரபுப் பட்டியலுக்குத் திரும்புக / Back to Directory</span>
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto space-y-8">
      {/* Breadcrumb & Navigation */}
      <div>
        <Link
          to="/art-forms"
          className="inline-flex items-center space-x-2 text-xs font-semibold text-amber-400 hover:text-amber-300 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>கலை மரபுப் பட்டியல் / Back to Art Directory</span>
        </Link>
      </div>

      {/* Detail Header Banner */}
      <div className="p-8 sm:p-10 rounded-3xl bg-gradient-to-br from-amber-950/80 via-slate-900 to-slate-950 border border-amber-500/30 shadow-2xl space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Sparkles className="w-5 h-5 text-amber-400" />
              <span className="text-xs font-bold text-amber-300 uppercase tracking-widest">
                TAMIL TRADITIONAL ART FORM
              </span>
            </div>
            <h1 className="text-3xl sm:text-5xl font-black text-white">{art.name}</h1>
            <p className="text-xl sm:text-2xl font-bold text-amber-400">{art.nameTa}</p>
          </div>

          <div className="flex flex-wrap gap-2">
            {art.categories.map((c) => (
              <span
                key={c.id}
                className="px-3 py-1 rounded-full bg-amber-500/20 border border-amber-500/40 text-amber-200 text-xs font-bold"
              >
                {isTa ? c.nameTa : c.name}
              </span>
            ))}
          </div>
        </div>

        {/* Origin & Historical Period Badges */}
        <div className="pt-4 border-t border-slate-800 flex flex-wrap gap-6 text-xs text-slate-300">
          {art.origin && (
            <div className="flex items-center space-x-2 bg-slate-950/60 px-3 py-2 rounded-xl border border-slate-800">
              <MapPin className="w-4 h-4 text-amber-400" />
              <span>Origin: <strong className="text-white">{isTa ? art.originTa || art.origin : art.origin}</strong></span>
            </div>
          )}

          {art.historicalPeriod && (
            <div className="flex items-center space-x-2 bg-slate-950/60 px-3 py-2 rounded-xl border border-slate-800">
              <Clock className="w-4 h-4 text-amber-400" />
              <span>Period: <strong className="text-white">{isTa ? art.historicalPeriodTa || art.historicalPeriod : art.historicalPeriod}</strong></span>
            </div>
          )}
        </div>
      </div>

      {/* Cultural Sections Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* Main Cultural Content (2 Cols) */}
        <div className="md:col-span-2 space-y-8">
          {/* Overview & Description */}
          <div className="p-6 sm:p-8 rounded-2xl bg-slate-900/60 border border-slate-800 shadow-xl space-y-4">
            <div className="flex items-center space-x-3 border-b border-slate-800 pb-3">
              <BookOpen className="w-5 h-5 text-amber-400" />
              <h2 className="text-lg font-bold text-white">கலை விளக்கம் / Overview & Description</h2>
            </div>
            <p className="text-slate-300 text-sm leading-relaxed whitespace-pre-line">
              {isTa ? art.descriptionTa : art.description}
            </p>
          </div>

          {/* Cultural Significance */}
          {(art.culturalSignificance || art.culturalSignificanceTa) && (
            <div className="p-6 sm:p-8 rounded-2xl bg-slate-900/60 border border-slate-800 shadow-xl space-y-4">
              <div className="flex items-center space-x-3 border-b border-slate-800 pb-3">
                <History className="w-5 h-5 text-amber-400" />
                <h2 className="text-lg font-bold text-white">பண்பாட்டுச் சிறப்பு / Cultural Significance</h2>
              </div>
              <p className="text-slate-300 text-sm leading-relaxed whitespace-pre-line">
                {isTa ? art.culturalSignificanceTa || art.culturalSignificance : art.culturalSignificance}
              </p>
            </div>
          )}

          {/* Performance Style */}
          {(art.performanceStyle || art.performanceStyleTa) && (
            <div className="p-6 sm:p-8 rounded-2xl bg-slate-900/60 border border-slate-800 shadow-xl space-y-4">
              <div className="flex items-center space-x-3 border-b border-slate-800 pb-3">
                <Shield className="w-5 h-5 text-amber-400" />
                <h2 className="text-lg font-bold text-white">செயல்முறை & பாணி / Performance Style</h2>
              </div>
              <p className="text-slate-300 text-sm leading-relaxed whitespace-pre-line">
                {isTa ? art.performanceStyleTa || art.performanceStyle : art.performanceStyle}
              </p>
            </div>
          )}
        </div>

        {/* Sidebar Attributes & Media Placeholder (1 Col) */}
        <div className="space-y-6">
          {/* Costume & Attire */}
          {(art.costume || art.costumeTa) && (
            <div className="p-6 rounded-2xl bg-slate-900/60 border border-slate-800 shadow-xl space-y-3">
              <div className="flex items-center space-x-2 text-amber-400 font-bold text-sm border-b border-slate-800 pb-2">
                <Shirt className="w-4 h-4" />
                <span>அலங்காரம் & உடை / Costume</span>
              </div>
              <p className="text-slate-300 text-xs leading-relaxed">
                {isTa ? art.costumeTa || art.costume : art.costume}
              </p>
            </div>
          )}

          {/* Instruments */}
          {(art.instruments || art.instrumentsTa) && (
            <div className="p-6 rounded-2xl bg-slate-900/60 border border-slate-800 shadow-xl space-y-3">
              <div className="flex items-center space-x-2 text-amber-400 font-bold text-sm border-b border-slate-800 pb-2">
                <Music className="w-4 h-4" />
                <span>இசைக்கருவிகள் / Instruments</span>
              </div>
              <p className="text-slate-300 text-xs leading-relaxed">
                {isTa ? art.instrumentsTa || art.instruments : art.instruments}
              </p>
            </div>
          )}

          {/* Districts */}
          {art.districts && art.districts.length > 0 && (
            <div className="p-6 rounded-2xl bg-slate-900/60 border border-slate-800 shadow-xl space-y-3">
              <div className="flex items-center space-x-2 text-amber-400 font-bold text-sm border-b border-slate-800 pb-2">
                <MapPin className="w-4 h-4" />
                <span>வட்டாரங்கள் / Associated Regions</span>
              </div>
              <div className="flex flex-wrap gap-2 pt-1">
                {art.districts.map((d) => (
                  <span
                    key={d.id}
                    className="px-2.5 py-1 rounded-lg bg-slate-950 border border-slate-800 text-slate-300 text-xs font-semibold"
                  >
                    {isTa ? d.nameTa : d.name} ({d.state})
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Media Placeholder Area */}
          <div className="p-6 rounded-2xl bg-gradient-to-b from-slate-900/80 to-slate-950 border border-amber-500/20 text-center space-y-3">
            <Video className="w-8 h-8 text-amber-400/60 mx-auto" />
            <h3 className="text-xs font-bold text-white uppercase tracking-wider">
              ஊடக காப்பகம் / Cultural Media Archive
            </h3>
            <p className="text-[11px] text-slate-400">
              High-definition performance videos, audio recordings, and archival photography curated by traditional art masters.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
