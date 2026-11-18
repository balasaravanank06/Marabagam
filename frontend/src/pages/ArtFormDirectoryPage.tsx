import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { artService } from '../services/artService';
import { ArtFormSummary, Category, District, PaginatedArtForms } from '../types/art';
import { Search, Filter, BookOpen, MapPin, Sparkles, AlertCircle, ChevronLeft, ChevronRight } from 'lucide-react';

export const ArtFormDirectoryPage: React.FC = () => {
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const [artData, setArtData] = useState<PaginatedArtForms | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [districts, setDistricts] = useState<District[]>([]);

  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedDistrict, setSelectedDistrict] = useState('');
  const [page, setPage] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch Categories & Districts for Filter Dropdowns
  useEffect(() => {
    const fetchMetadata = async () => {
      try {
        const [cats, dists] = await Promise.all([
          artService.getCategories(),
          artService.getDistricts(),
        ]);
        setCategories(cats);
        setDistricts(dists);
      } catch (err) {
        console.error('Failed to load art metadata filters:', err);
      }
    };
    fetchMetadata();
  }, []);

  // Fetch Published Art Forms
  useEffect(() => {
    const fetchArtForms = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await artService.getPublishedArtForms({
          page,
          size: 9,
          search: search.trim() || undefined,
          category: selectedCategory || undefined,
          district: selectedDistrict || undefined,
        });
        setArtData(data);
      } catch (err: any) {
        setError(err.response?.data?.message || 'கலை தகவல்களை ஏற்றுவதில் தோல்வி / Failed to load art forms');
      } finally {
        setLoading(false);
      }
    };

    const timer = setTimeout(fetchArtForms, 300);
    return () => clearTimeout(timer);
  }, [search, selectedCategory, selectedDistrict, page]);

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Hero Header */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-amber-950 via-rose-950 to-slate-950 border border-amber-500/30 p-8 sm:p-10 shadow-2xl">
        <div className="relative z-10 space-y-4 max-w-3xl">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs font-semibold tracking-wider">
            <Sparkles className="w-4 h-4 text-amber-400" />
            <span>TAMIL TRADITIONAL ARTS DIRECTORY • தமிழ் மரபுக் கலைப் பெட்டகம்</span>
          </div>

          <h1 className="text-3xl sm:text-5xl font-extrabold text-white tracking-tight leading-tight">
            {isTa ? 'தமிழ் பாரம்பரியக் கலைகள் களஞ்சியம்' : 'Traditional Tamil Arts Directory'}
          </h1>

          <p className="text-slate-300 text-base sm:text-lg leading-relaxed">
            {isTa
              ? 'சிலம்பம், கரகாட்டம், தெருக்கூத்து போன்ற பழந்தமிழ்ப் பாரம்பரியக் கலை வடிவங்களை ஆராய்ந்து அவற்றின் வரலாறு, ஆடை, இசைக்கருவிகள் மற்றும் பண்பாட்டுச் சிறப்புகளை அறிந்து கொள்ளுங்கள்.'
              : 'Explore the digital encyclopedia of ancient Tamil traditional arts, martial forms, folk dances, and open-air theatre heritage.'}
          </p>
        </div>
      </div>

      {/* Search & Filter Toolbar */}
      <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 backdrop-blur-md shadow-xl flex flex-col md:flex-row items-center justify-between gap-4">
        {/* Search Bar */}
        <div className="relative flex-1 w-full">
          <Search className="absolute left-3.5 top-3.5 w-5 h-5 text-amber-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            placeholder={isTa ? 'கலை பெயர் அல்லது சொல்லைத் தேடுக... (e.g. Silambam)' : 'Search art forms, styles, instruments...'}
            className="w-full pl-11 pr-4 py-3 bg-slate-950/80 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500 text-sm"
          />
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto">
          {/* Category Filter */}
          <div className="relative flex-1 md:flex-initial min-w-[160px]">
            <Filter className="absolute left-3.5 top-3.5 w-4 h-4 text-slate-400" />
            <select
              value={selectedCategory}
              onChange={(e) => {
                setSelectedCategory(e.target.value);
                setPage(0);
              }}
              className="w-full pl-10 pr-8 py-3 bg-slate-950/80 border border-slate-800 rounded-xl text-white text-xs font-semibold focus:outline-none focus:border-amber-500"
            >
              <option value="">{isTa ? 'அனைத்து வகைகளும் (All Categories)' : 'All Categories'}</option>
              {categories.map((c) => (
                <option key={c.id} value={c.name}>
                  {isTa ? c.nameTa : c.name}
                </option>
              ))}
            </select>
          </div>

          {/* District Filter */}
          <div className="relative flex-1 md:flex-initial min-w-[160px]">
            <MapPin className="absolute left-3.5 top-3.5 w-4 h-4 text-slate-400" />
            <select
              value={selectedDistrict}
              onChange={(e) => {
                setSelectedDistrict(e.target.value);
                setPage(0);
              }}
              className="w-full pl-10 pr-8 py-3 bg-slate-950/80 border border-slate-800 rounded-xl text-white text-xs font-semibold focus:outline-none focus:border-amber-500"
            >
              <option value="">{isTa ? 'அனைத்து மாவட்டங்களும் (All Districts)' : 'All Districts'}</option>
              {districts.map((d) => (
                <option key={d.id} value={d.name}>
                  {isTa ? d.nameTa : d.name}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Loading Skeleton State */}
      {loading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((n) => (
            <div key={n} className="p-6 rounded-2xl bg-slate-900/40 border border-slate-800/60 animate-pulse space-y-4">
              <div className="h-6 bg-slate-800 rounded w-2/3" />
              <div className="h-4 bg-slate-800/60 rounded w-full" />
              <div className="h-4 bg-slate-800/60 rounded w-4/5" />
              <div className="pt-4 flex justify-between">
                <div className="h-6 bg-slate-800/80 rounded w-20" />
                <div className="h-6 bg-slate-800/80 rounded w-24" />
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Error State */}
      {error && !loading && (
        <div className="p-8 rounded-2xl bg-red-950/40 border border-red-500/30 text-center space-y-3">
          <AlertCircle className="w-10 h-10 text-red-400 mx-auto" />
          <h3 className="text-xl font-bold text-red-200">{error}</h3>
          <p className="text-xs text-red-300/70">தயவுசெய்து இணைய இணைப்பை சரிபார்க்கவும்.</p>
        </div>
      )}

      {/* Empty State */}
      {!loading && !error && artData && artData.content.length === 0 && (
        <div className="p-12 rounded-2xl bg-slate-900/40 border border-slate-800 text-center space-y-4">
          <BookOpen className="w-12 h-12 text-amber-500/40 mx-auto" />
          <h3 className="text-xl font-bold text-slate-300">
            {isTa ? 'கலை வடிவங்கள் எதுவும் காணப்படவில்லை' : 'No Art Forms Found'}
          </h3>
          <p className="text-sm text-slate-400 max-w-md mx-auto">
            {isTa
              ? 'உங்கள் தேடலுக்கு ஏற்ற கலை வடிவங்கள் பட்டியலில் இல்லை. வேறு சொல்லைப் பயன்படுத்தி தேடவும்.'
              : 'No matching cultural art forms found for the selected filter criteria. Try clearing search filters.'}
          </p>
          <button
            onClick={() => {
              setSearch('');
              setSelectedCategory('');
              setSelectedDistrict('');
              setPage(0);
            }}
            className="px-4 py-2 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs font-semibold hover:bg-amber-500/20 transition"
          >
            {isTa ? 'தேடலை அழிக்கவும் / Reset Filters' : 'Reset Filters'}
          </button>
        </div>
      )}

      {/* Art Form Cards Grid */}
      {!loading && !error && artData && artData.content.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {artData.content.map((art: ArtFormSummary) => (
            <Link
              key={art.id}
              to={`/art-forms/${art.slug}`}
              className="group p-6 rounded-2xl bg-gradient-to-b from-slate-900/90 to-slate-950 border border-slate-800 hover:border-amber-500/40 transition-all duration-300 shadow-lg hover:shadow-2xl flex flex-col justify-between"
            >
              <div className="space-y-4">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <h3 className="text-xl font-bold text-white group-hover:text-amber-300 transition-colors">
                      {art.name}
                    </h3>
                    <p className="text-sm font-medium text-amber-400">{art.nameTa}</p>
                  </div>
                  {art.origin && (
                    <span className="px-2.5 py-1 rounded-full bg-slate-800 border border-slate-700 text-[11px] font-medium text-slate-300 flex items-center space-x-1">
                      <MapPin className="w-3 h-3 text-amber-400" />
                      <span>{isTa ? art.originTa || art.origin : art.origin}</span>
                    </span>
                  )}
                </div>

                <p className="text-slate-300 text-xs leading-relaxed line-clamp-3">
                  {isTa ? art.shortDescriptionTa : art.shortDescription}
                </p>
              </div>

              <div className="pt-6 mt-4 border-t border-slate-800/80 flex items-center justify-between">
                <div className="flex flex-wrap gap-1.5">
                  {art.categories.map((c) => (
                    <span
                      key={c.id}
                      className="px-2.5 py-0.5 rounded-md bg-amber-500/10 border border-amber-500/20 text-amber-300 text-[10px] font-semibold"
                    >
                      {isTa ? c.nameTa : c.name}
                    </span>
                  ))}
                </div>

                <span className="text-xs font-bold text-amber-400 group-hover:translate-x-1 transition-transform flex items-center space-x-1">
                  <span>{isTa ? 'மேலும் அறிய' : 'View Details'}</span>
                  <ChevronRight className="w-4 h-4" />
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}

      {/* Pagination Controls */}
      {!loading && !error && artData && artData.totalPages > 1 && (
        <div className="flex items-center justify-center space-x-4 pt-6">
          <button
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white disabled:opacity-30 disabled:pointer-events-none transition"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>

          <span className="text-xs font-semibold text-slate-400">
            Page <span className="text-amber-400">{page + 1}</span> of {artData.totalPages}
          </span>

          <button
            disabled={page >= artData.totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
            className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white disabled:opacity-30 disabled:pointer-events-none transition"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>
      )}
    </div>
  );
};
