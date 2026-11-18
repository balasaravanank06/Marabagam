import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Search, BookOpen, Clock, Sparkles, ChevronLeft, ChevronRight } from 'lucide-react';
import { learningService } from '../services/learningService';
import { artService } from '../services/artService';
import { CourseSummary, CourseDifficulty } from '../types/learning';
import { ArtFormSummary } from '../types/art';

export const CourseDirectoryPage: React.FC = () => {
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [artForms, setArtForms] = useState<ArtFormSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter States
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedArtForm, setSelectedArtForm] = useState('');
  const [selectedDifficulty, setSelectedDifficulty] = useState<CourseDifficulty | ''>('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    fetchArtForms();
  }, []);

  useEffect(() => {
    fetchCourses();
  }, [searchTerm, selectedArtForm, selectedDifficulty, page]);

  const fetchArtForms = async () => {
    try {
      const res = await artService.getPublishedArtForms({ size: 100 });
      setArtForms(res.content || []);
    } catch (err) {
      console.error('Failed to load art forms filter', err);
    }
  };

  const fetchCourses = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await learningService.getPublicCourses({
        page,
        size: 9,
        search: searchTerm.trim() || undefined,
        artForm: selectedArtForm || undefined,
        difficulty: (selectedDifficulty as CourseDifficulty) || undefined
      });
      setCourses(res.content || []);
      setTotalPages(res.totalPages || 1);
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
        err?.response?.data?.error?.message ||
        (isTa ? 'பாடங்களை ஏற்றுவதில் தோல்வி' : 'Failed to load courses.')
      );
    } finally {
      setLoading(false);
    }
  };

  const getDifficultyBadge = (diff: CourseDifficulty) => {
    switch (diff) {
      case 'BEGINNER':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-500/10 text-emerald-300 border border-emerald-500/30">
            {diff}
          </span>
        );
      case 'INTERMEDIATE':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-amber-500/10 text-amber-300 border border-amber-500/30">
            {diff}
          </span>
        );
      case 'ADVANCED':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-rose-500/10 text-rose-300 border border-rose-500/30">
            {diff}
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-slate-800 text-slate-300 border border-slate-700">
            {diff}
          </span>
        );
    }
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Hero Banner */}
      <div className="relative rounded-2xl overflow-hidden bg-gradient-to-r from-amber-950 via-slate-900 to-rose-950 p-6 sm:p-8 border border-amber-500/30 shadow-2xl">
        <div className="relative z-10 max-w-3xl space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-amber-500/20 border border-amber-500/40 rounded-full text-amber-300 text-xs font-semibold">
            <Sparkles className="w-3.5 h-3.5 text-amber-400" />
            <span>{isTa ? 'கட்டமைக்கப்பட்ட மின்-கற்றல்' : 'Structured Heritage E-Learning'}</span>
          </div>
          <h1 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-white tracking-tight">
            {isTa ? 'தமிழ் பாரம்பரிய கலைக் கற்கைகள்' : 'Tamil Heritage Art Courses'}
          </h1>
          <p className="text-slate-300 text-xs sm:text-sm leading-relaxed">
            {isTa
              ? 'பாரம்பரியக் கலைஞர்கள் மற்றும் கலாச்சார வல்லுநர்களால் கற்பிக்கப்படும் பாடங்கள், தொகுதிகள் மற்றும் பல்லூடகப் பயிற்சிகள்.'
              : 'Explore step-by-step masterclasses, structured modules, and multimedia lessons taught by traditional artists and cultural experts.'}
          </p>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="bg-slate-900/60 rounded-2xl p-5 border border-slate-800 backdrop-blur-md shadow-lg space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Search Input */}
          <div className="md:col-span-2 relative">
            <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder={isTa ? 'பாடங்களைத் தேடுக...' : 'Search courses by title or description...'}
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setPage(0);
              }}
              className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs sm:text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-amber-500 transition"
            />
          </div>

          {/* Art Form Filter */}
          <div>
            <select
              value={selectedArtForm}
              onChange={(e) => {
                setSelectedArtForm(e.target.value);
                setPage(0);
              }}
              className="w-full px-3 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs sm:text-sm text-slate-200 focus:outline-none focus:border-amber-500 transition"
            >
              <option value="">{isTa ? 'அனைத்து கலைகள்' : 'All Art Forms'}</option>
              {artForms.map((af) => (
                <option key={af.id} value={af.id}>
                  {isTa && af.nameTa ? af.nameTa : af.name}
                </option>
              ))}
            </select>
          </div>

          {/* Difficulty Filter */}
          <div>
            <select
              value={selectedDifficulty}
              onChange={(e) => {
                setSelectedDifficulty(e.target.value as CourseDifficulty);
                setPage(0);
              }}
              className="w-full px-3 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs sm:text-sm text-slate-200 focus:outline-none focus:border-amber-500 transition"
            >
              <option value="">{isTa ? 'அனைத்து நிலைகள்' : 'All Difficulty Levels'}</option>
              <option value="BEGINNER">{isTa ? 'தொடக்க நிலை (Beginner)' : 'Beginner'}</option>
              <option value="INTERMEDIATE">{isTa ? 'இடைநிலை (Intermediate)' : 'Intermediate'}</option>
              <option value="ADVANCED">{isTa ? 'உயர்நிலை (Advanced)' : 'Advanced'}</option>
            </select>
          </div>
        </div>
      </div>

      {/* Courses Grid / Loading / Error */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 space-y-4">
          <div className="animate-spin rounded-full h-10 w-10 border-4 border-amber-500 border-t-transparent" />
          <p className="text-sm text-slate-400">
            {isTa ? 'பாடங்கள் ஏற்றப்படுகின்றன...' : 'Loading courses...'}
          </p>
        </div>
      ) : error ? (
        <div className="bg-rose-950/60 border border-rose-500/50 p-6 rounded-2xl text-center space-y-4 shadow-xl">
          <p className="text-sm text-rose-300 font-semibold">{error}</p>
          <button
            onClick={fetchCourses}
            className="px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold rounded-xl transition"
          >
            {isTa ? 'மீண்டும் முயற்சிக்கவும்' : 'Retry'}
          </button>
        </div>
      ) : courses.length === 0 ? (
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-12 text-center text-slate-400 space-y-4">
          <BookOpen className="w-12 h-12 text-slate-600 mx-auto" />
          <h3 className="text-lg font-bold text-white">
            {isTa ? 'பாடங்கள் எதுவும் கிடைக்கவில்லை' : 'No Courses Found'}
          </h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto">
            {isTa
              ? 'உங்கள் தேடல் அல்லது வடிகட்டிகளுக்கு ஏற்ற பாடங்கள் எதுவும் இல்லை. வேறு வார்த்தைகளைத் தேர்ந்தெடுக்கவும்.'
              : 'Try clearing your search query or choosing different category filters.'}
          </p>
          {(searchTerm || selectedArtForm || selectedDifficulty) && (
            <button
              onClick={() => {
                setSearchTerm('');
                setSelectedArtForm('');
                setSelectedDifficulty('');
                setPage(0);
              }}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-amber-300 rounded-xl text-xs font-semibold border border-slate-700 transition"
            >
              {isTa ? 'வடிகட்டிகளை மீட்டமை' : 'Reset Filters'}
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {courses.map((course) => {
            const courseTitle = isTa && course.titleTa ? course.titleTa : course.title;
            const courseDesc = isTa && course.shortDescriptionTa ? course.shortDescriptionTa : course.shortDescription;

            return (
              <div
                key={course.id}
                className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-5 shadow-lg flex flex-col justify-between hover:border-amber-500/50 transition-all group backdrop-blur-md"
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-xs font-semibold text-amber-400">
                      {course.artForm?.name || 'Tamil Heritage'}
                    </span>
                    {getDifficultyBadge(course.difficulty)}
                  </div>

                  <h3 className="text-lg font-bold text-white group-hover:text-amber-200 transition line-clamp-2">
                    {courseTitle}
                  </h3>

                  <p className="text-xs text-slate-400 line-clamp-3 leading-relaxed">
                    {courseDesc}
                  </p>

                  <div className="pt-2 flex flex-wrap items-center gap-4 text-xs text-slate-400 border-t border-slate-800/80">
                    <div className="flex items-center space-x-1">
                      <Clock className="w-3.5 h-3.5 text-amber-400" />
                      <span>{course.estimatedDurationMinutes} mins</span>
                    </div>
                  </div>
                </div>

                <div className="mt-5 pt-4 border-t border-slate-800/80 flex items-center justify-between">
                  <span className="text-xs font-bold text-amber-400/90 uppercase tracking-wider">
                    {isTa ? 'இலவசப் பாடம்' : 'Free Heritage Access'}
                  </span>

                  <Link
                    to={`/course/${course.slug}`}
                    className="px-4 py-2 bg-gradient-to-r from-amber-600 to-amber-500 text-slate-950 font-bold rounded-xl text-xs hover:brightness-110 shadow-md shadow-amber-500/20 transition flex items-center space-x-1"
                  >
                    <span>{isTa ? 'விவரங்கள்' : 'View Course'}</span>
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center space-x-3 pt-4">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-400 hover:text-white disabled:opacity-40 transition"
            aria-label="Previous Page"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
          <span className="text-xs font-semibold text-slate-400">
            {isTa ? `பக்கம் ${page + 1} / ${totalPages}` : `Page ${page + 1} of ${totalPages}`}
          </span>
          <button
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-400 hover:text-white disabled:opacity-40 transition"
            aria-label="Next Page"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>
      )}
    </div>
  );
};
