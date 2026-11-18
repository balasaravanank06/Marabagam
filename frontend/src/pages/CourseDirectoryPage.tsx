import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, BookOpen, Clock, Sparkles } from 'lucide-react';
import { learningService } from '../services/learningService';
import { artService } from '../services/artService';
import { CourseSummary, CourseDifficulty } from '../types/learning';
import { ArtFormSummary } from '../types/art';

export const CourseDirectoryPage: React.FC = () => {
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

  // Language toggle state
  const [lang, setLang] = useState<'en' | 'ta'>('en');

  useEffect(() => {
    fetchArtForms();
  }, []);

  useEffect(() => {
    fetchCourses();
  }, [searchTerm, selectedArtForm, selectedDifficulty, page]);

  const fetchArtForms = async () => {
    try {
      const res = await artService.getPublishedArtForms({ size: 100 });
      setArtForms(res.content);
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
        search: searchTerm || undefined,
        artForm: selectedArtForm || undefined,
        difficulty: (selectedDifficulty as CourseDifficulty) || undefined
      });
      setCourses(res.content);
      setTotalPages(res.totalPages || 1);
    } catch (err: any) {
      setError(err?.response?.data?.error?.message || 'Failed to load courses.');
    } finally {
      setLoading(false);
    }
  };

  const getDifficultyBadgeColor = (diff: CourseDifficulty) => {
    switch (diff) {
      case 'BEGINNER': return 'bg-emerald-100 text-emerald-800 border-emerald-300';
      case 'INTERMEDIATE': return 'bg-amber-100 text-amber-800 border-amber-300';
      case 'ADVANCED': return 'bg-rose-100 text-rose-800 border-rose-300';
      default: return 'bg-gray-100 text-gray-800 border-gray-300';
    }
  };

  return (
    <div className="min-h-screen bg-[#FFFFF0]/30 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-8">

        {/* Hero Section */}
        <div className="bg-gradient-to-r from-[#800020] via-[#5c0017] to-[#400010] text-[#FFFFF0] rounded-2xl p-8 shadow-xl relative overflow-hidden">
          <div className="absolute top-0 right-0 opacity-10 pointer-events-none translate-x-1/4 -translate-y-1/4">
            <Sparkles className="w-96 h-96 text-[#D4AF37]" />
          </div>
          <div className="relative z-10 space-y-4 max-w-3xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 bg-[#D4AF37]/20 border border-[#D4AF37]/40 rounded-full text-[#D4AF37] text-sm font-medium">
              <BookOpen className="w-4 h-4" />
              <span>Structured Heritage E-Learning</span>
            </div>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
              {lang === 'ta' ? 'தமிழ் பாரம்பரிய கலைக் கற்கைகள்' : 'Tamil Heritage Art Courses'}
            </h1>
            <p className="text-[#FFFFF0]/80 text-lg">
              Explore step-by-step masterclasses, structured modules, and multimedia lessons taught by traditional artists and cultural experts.
            </p>
            <div className="flex items-center gap-3 pt-2">
              <button
                onClick={() => setLang(lang === 'en' ? 'ta' : 'en')}
                className="px-4 py-2 bg-[#D4AF37] text-[#333333] font-semibold rounded-lg hover:bg-[#c49f27] transition shadow"
              >
                {lang === 'en' ? 'தமிழில் படிக்க (Read in Tamil)' : 'Switch to English'}
              </button>
            </div>
          </div>
        </div>

        {/* Search and Filters */}
        <div className="bg-white rounded-xl shadow-md p-6 border border-[#D4AF37]/20 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">

            {/* Search Input */}
            <div className="md:col-span-2 relative">
              <Search className="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder={lang === 'ta' ? 'பாடங்களை தேடுக...' : 'Search courses by title or description...'}
                value={searchTerm}
                onChange={(e) => { setSearchTerm(e.target.value); setPage(0); }}
                className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#800020]"
              />
            </div>

            {/* Art Form Filter */}
            <div>
              <select
                value={selectedArtForm}
                onChange={(e) => { setSelectedArtForm(e.target.value); setPage(0); }}
                className="w-full py-2.5 px-3 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#800020] text-gray-700"
              >
                <option value="">{lang === 'ta' ? 'அனைத்து கலைகள்' : 'All Art Forms'}</option>
                {artForms.map((af) => (
                  <option key={af.id} value={af.slug}>
                    {lang === 'ta' ? af.nameTa : af.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Difficulty Filter */}
            <div>
              <select
                value={selectedDifficulty}
                onChange={(e) => { setSelectedDifficulty(e.target.value as CourseDifficulty); setPage(0); }}
                className="w-full py-2.5 px-3 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#800020] text-gray-700"
              >
                <option value="">{lang === 'ta' ? 'அனைத்து நிலைகள்' : 'All Difficulties'}</option>
                <option value="BEGINNER">BEGINNER (தொடக்க நிலை)</option>
                <option value="INTERMEDIATE">INTERMEDIATE (இடைநிலை)</option>
                <option value="ADVANCED">ADVANCED (உயர் நிலை)</option>
              </select>
            </div>

          </div>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="p-4 bg-rose-50 border border-rose-200 text-rose-700 rounded-xl">
            {error}
          </div>
        )}

        {/* Loading State Skeleton */}
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="bg-white rounded-xl p-6 shadow border border-gray-100 animate-pulse space-y-4">
                <div className="h-6 bg-gray-200 rounded w-3/4"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                <div className="h-16 bg-gray-100 rounded"></div>
              </div>
            ))}
          </div>
        ) : courses.length === 0 ? (
          <div className="text-center py-16 bg-white rounded-xl shadow border border-gray-200 space-y-3">
            <BookOpen className="w-12 h-12 text-[#800020] mx-auto opacity-50" />
            <h3 className="text-xl font-bold text-gray-800">
              {lang === 'ta' ? 'பாடங்கள் எதுவும் கிடைக்கவில்லை' : 'No courses found'}
            </h3>
            <p className="text-gray-500 max-w-md mx-auto">
              Try adjusting your search criteria or selected art form filter.
            </p>
          </div>
        ) : (
          /* Course Cards Grid */
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {courses.map((course) => (
              <Link
                key={course.id}
                to={`/course/${course.slug}`}
                className="bg-white rounded-xl shadow-md hover:shadow-xl transition-all duration-300 border border-[#D4AF37]/20 flex flex-col justify-between overflow-hidden group"
              >
                <div className="p-6 space-y-4">

                  {/* Header badges */}
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-xs font-semibold text-[#800020] bg-[#800020]/10 px-2.5 py-1 rounded-md border border-[#800020]/20">
                      {lang === 'ta' ? course.artForm?.nameTa : course.artForm?.name}
                    </span>
                    <span className={`text-xs font-semibold px-2.5 py-1 rounded-md border ${getDifficultyBadgeColor(course.difficulty)}`}>
                      {course.difficulty}
                    </span>
                  </div>

                  {/* Course Titles */}
                  <div>
                    <h3 className="text-xl font-bold text-[#333333] group-hover:text-[#800020] transition-colors">
                      {lang === 'ta' ? course.titleTa : course.title}
                    </h3>
                    {lang === 'en' && course.titleTa && (
                      <p className="text-sm font-medium text-[#D4AF37]">{course.titleTa}</p>
                    )}
                  </div>

                  {/* Short Description */}
                  <p className="text-gray-600 text-sm line-clamp-3">
                    {lang === 'ta' ? course.shortDescriptionTa : course.shortDescription}
                  </p>
                </div>

                {/* Card Footer */}
                <div className="px-6 py-4 bg-gray-50 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500">
                  <div className="flex items-center gap-1.5">
                    <Clock className="w-4 h-4 text-[#800020]" />
                    <span>{course.estimatedDurationMinutes} mins</span>
                  </div>
                  <span className="font-semibold text-[#800020] group-hover:underline">
                    View Course &rarr;
                  </span>
                </div>
              </Link>
            ))}
          </div>
        )}

        {/* Pagination Controls */}
        {totalPages > 1 && (
          <div className="flex justify-center items-center gap-2 pt-4">
            <button
              disabled={page === 0}
              onClick={() => setPage(page - 1)}
              className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
            >
              Previous
            </button>
            <span className="text-sm font-medium text-gray-700">
              Page {page + 1} of {totalPages}
            </span>
            <button
              disabled={page >= totalPages - 1}
              onClick={() => setPage(page + 1)}
              className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
            >
              Next
            </button>
          </div>
        )}

      </div>
    </div>
  );
};
