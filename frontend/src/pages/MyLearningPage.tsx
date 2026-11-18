import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { BookOpen, CheckCircle, PlayCircle, XCircle, AlertCircle, Award } from 'lucide-react';
import { learningService } from '../services/learningService';
import { Enrollment } from '../types/learning';

export const MyLearningPage: React.FC = () => {
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'ACTIVE' | 'COMPLETED' | 'CANCELLED'>('ACTIVE');

  useEffect(() => {
    fetchMyEnrollments();
  }, []);

  const fetchMyEnrollments = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await learningService.getUserEnrollments();
      setEnrollments(data || []);
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
        err?.response?.data?.error?.message ||
        (isTa ? 'உங்கள் சேர்க்கைகளை ஏற்றுவதில் தோல்வி' : 'Failed to load your course enrollments.')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCancelEnrollment = async (courseId: string) => {
    const confirmMsg = isTa
      ? 'இந்த பாடநெறி சேர்க்கையை ரத்து செய்ய விரும்புகிறீர்களா? நீங்கள் எப்போது வேண்டுமானாலும் மீண்டும் சேரலாம்.'
      : 'Are you sure you want to cancel this course enrollment? You can re-enroll anytime.';
    if (!window.confirm(confirmMsg)) {
      return;
    }

    try {
      await learningService.cancelEnrollment(courseId);
      await fetchMyEnrollments();
    } catch (err: any) {
      alert(err?.response?.data?.error?.message || err?.message || 'Failed to cancel enrollment.');
    }
  };

  const filteredEnrollments = enrollments.filter((e) => e.status === activeTab);

  if (loading) {
    return (
      <div className="min-h-[50vh] flex flex-col items-center justify-center space-y-4">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-500 border-t-transparent" />
        <p className="text-sm text-slate-400">
          {isTa ? 'உங்கள் பாடங்கள் ஏற்றப்படுகின்றன...' : 'Loading your learning progress...'}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-amber-950/60 via-slate-900 to-amber-900/40 rounded-2xl p-6 sm:p-8 border border-amber-500/20 shadow-2xl backdrop-blur-md flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div>
          <div className="flex items-center space-x-2 text-amber-400 text-xs font-semibold uppercase tracking-wider mb-1">
            <BookOpen className="w-4 h-4" />
            <span>{isTa ? 'தனிப்பயன் கற்றல் பலகை' : 'Personal Learning Dashboard'}</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white">
            {isTa ? 'எனது பாடங்கள் (My Learning Dashboard)' : 'My Learning Dashboard'}
          </h1>
          <p className="text-slate-400 text-xs sm:text-sm mt-1">
            {isTa
              ? 'தமிழ் பாரம்பரிய கலைக் கற்கைகளின் முன்னேற்றத்தைக் கண்காணித்து தொடர்ந்து பயிலுங்கள்.'
              : 'Track your progress across Tamil traditional heritage art courses.'}
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <Link
            to="/certificates"
            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-amber-300 font-semibold rounded-xl text-xs border border-slate-700 transition flex items-center space-x-1.5"
          >
            <Award className="w-4 h-4 text-amber-400" />
            <span>{isTa ? 'சான்றிதழ்கள்' : 'Certificates'}</span>
          </Link>
          <Link
            to="/courses"
            className="px-4 py-2 bg-gradient-to-r from-amber-600 to-amber-500 text-slate-950 font-bold rounded-xl text-xs hover:brightness-110 shadow-lg shadow-amber-500/10 transition"
          >
            {isTa ? 'பாட அட்டவணை' : 'Explore Catalog'}
          </Link>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-800 gap-6 text-xs sm:text-sm font-semibold">
        <button
          onClick={() => setActiveTab('ACTIVE')}
          className={`pb-3 border-b-2 transition ${
            activeTab === 'ACTIVE'
              ? 'border-amber-500 text-amber-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          {isTa ? 'நடப்புப் பாடங்கள்' : 'Active Courses'} ({enrollments.filter((e) => e.status === 'ACTIVE').length})
        </button>
        <button
          onClick={() => setActiveTab('COMPLETED')}
          className={`pb-3 border-b-2 transition ${
            activeTab === 'COMPLETED'
              ? 'border-amber-500 text-amber-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          {isTa ? 'நிறைவு செய்தவை' : 'Completed'} ({enrollments.filter((e) => e.status === 'COMPLETED').length})
        </button>
        <button
          onClick={() => setActiveTab('CANCELLED')}
          className={`pb-3 border-b-2 transition ${
            activeTab === 'CANCELLED'
              ? 'border-amber-500 text-amber-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          {isTa ? 'ரத்து செய்யப்பட்டவை' : 'Cancelled'} ({enrollments.filter((e) => e.status === 'CANCELLED').length})
        </button>
      </div>

      {/* Error notification with retry */}
      {error && (
        <div className="p-4 bg-rose-950/60 text-rose-200 border border-rose-500/40 rounded-xl flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <AlertCircle className="w-5 h-5 text-rose-400" />
            <span className="text-xs">{error}</span>
          </div>
          <button
            onClick={fetchMyEnrollments}
            className="px-3 py-1 bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold rounded-lg transition"
          >
            {isTa ? 'மீண்டும் முயற்சிக்கவும்' : 'Retry'}
          </button>
        </div>
      )}

      {/* Empty State */}
      {filteredEnrollments.length === 0 && !error && (
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-12 text-center text-slate-400 space-y-4">
          <BookOpen className="w-12 h-12 text-slate-600 mx-auto" />
          <h3 className="text-lg font-bold text-white">
            {activeTab === 'ACTIVE'
              ? isTa ? 'நடப்பில் எந்த பாடங்களும் இல்லை' : 'No Active Course Enrollments'
              : activeTab === 'COMPLETED'
              ? isTa ? 'நீங்கள் இதுவரை எந்த பாடத்தையும் நிறைவு செய்யவில்லை' : 'No Completed Courses Yet'
              : isTa ? 'ரத்து செய்யப்பட்ட பாடங்கள் எதுவும் இல்லை' : 'No Cancelled Enrollments'}
          </h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto">
            {activeTab === 'ACTIVE'
              ? isTa
                ? 'தமிழ் பாரம்பரிய கலைகளைக் கற்க பாட அட்டவணையிலிருந்து புதிய பாடத்தைத் தேர்ந்தெடுங்கள்.'
                : 'Browse our heritage course catalog to enroll in authentic traditional art lessons.'
              : isTa
                ? 'பாடங்களையும் வினாடி வினாக்களையும் நிறைவு செய்து சான்றிதழ் பெறுங்கள்.'
                : 'Complete enrolled courses and assessments to earn your official credentials.'}
          </p>
          {activeTab === 'ACTIVE' && (
            <Link
              to="/courses"
              className="inline-block mt-2 px-5 py-2.5 bg-amber-600 hover:bg-amber-500 text-slate-950 font-bold rounded-xl text-xs transition shadow-md shadow-amber-600/20"
            >
              {isTa ? 'பாடங்களைத் தேடுக' : 'Find Courses to Learn'}
            </Link>
          )}
        </div>
      )}

      {/* Course List Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredEnrollments.map((enr) => {
          const course = enr.course;
          const courseTitle = isTa && course?.titleTa ? course.titleTa : course?.title;

          return (
            <div
              key={enr.id}
              className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-5 shadow-lg flex flex-col justify-between hover:border-slate-700 transition"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between text-xs text-slate-400">
                  <span className="font-semibold text-amber-400">
                    {course?.artForm?.name || 'Heritage Art'}
                  </span>
                  <span className="text-[11px] text-slate-500">
                    {new Date(enr.enrolledAt).toLocaleDateString()}
                  </span>
                </div>

                <h3 className="font-bold text-base text-white line-clamp-1">{courseTitle}</h3>

                <p className="text-xs text-slate-400 line-clamp-2">
                  {isTa && course?.shortDescriptionTa ? course.shortDescriptionTa : course?.shortDescription}
                </p>

                {/* Progress Bar */}
                <div className="space-y-1.5 pt-2">
                  <div className="flex justify-between text-xs font-semibold">
                    <span className="text-slate-400">{isTa ? 'முன்னேற்றம்' : 'Progress'}</span>
                    <span className="text-amber-400 font-mono">{enr.completionPercentage?.toFixed(0) || 0}%</span>
                  </div>
                  <div className="w-full h-2 bg-slate-950 rounded-full overflow-hidden border border-slate-800">
                    <div
                      className="h-full bg-gradient-to-r from-amber-600 to-amber-400 rounded-full transition-all duration-300"
                      style={{ width: `${enr.completionPercentage || 0}%` }}
                    />
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between gap-3">
                {enr.status === 'ACTIVE' && (
                  <>
                    <Link
                      to={`/course/${course?.slug}/learn`}
                      className="flex-1 py-2 px-3 bg-amber-600 hover:bg-amber-500 text-slate-950 font-bold rounded-xl text-xs flex items-center justify-center space-x-1.5 transition shadow"
                    >
                      <PlayCircle className="w-4 h-4" />
                      <span>{isTa ? 'தொடர்க' : 'Continue'}</span>
                    </Link>

                    <button
                      onClick={() => handleCancelEnrollment(course?.id)}
                      className="p-2 text-slate-500 hover:text-rose-400 hover:bg-rose-950/40 rounded-xl transition"
                      title={isTa ? 'சேர்க்கையை ரத்து செய்க' : 'Cancel Enrollment'}
                    >
                      <XCircle className="w-4 h-4" />
                    </button>
                  </>
                )}

                {enr.status === 'COMPLETED' && (
                  <div className="w-full flex items-center justify-between gap-2">
                    <span className="text-emerald-400 text-xs font-semibold flex items-center space-x-1">
                      <CheckCircle className="w-4 h-4" />
                      <span>{isTa ? 'முடிக்கப்பட்டது' : 'Completed'}</span>
                    </span>
                    <Link
                      to="/certificates"
                      className="px-3 py-1.5 bg-amber-500/20 text-amber-300 border border-amber-500/30 rounded-lg text-xs font-bold hover:bg-amber-500/30 transition flex items-center space-x-1"
                    >
                      <Award className="w-3.5 h-3.5" />
                      <span>{isTa ? 'சான்றிதழ்' : 'Certificate'}</span>
                    </Link>
                  </div>
                )}

                {enr.status === 'CANCELLED' && (
                  <span className="text-slate-500 text-xs italic">
                    {isTa ? 'சேர்க்கை ரத்து செய்யப்பட்டுள்ளது' : 'Enrollment cancelled'}
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
