import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { BookOpen, Clock, CheckCircle2, PlayCircle, Lock, ArrowLeft, AlertCircle } from 'lucide-react';
import { learningService } from '../services/learningService';
import { CourseDetail, Enrollment } from '../types/learning';
import { useAuth } from '../context/AuthContext';

export const CourseDetailPage: React.FC = () => {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null);
  const [loading, setLoading] = useState(true);
  const [enrolling, setEnrolling] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (slug) {
      fetchCourseDetails();
    }
  }, [slug]);

  const fetchCourseDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await learningService.getCourseBySlug(slug!);
      setCourse(data);

      if (isAuthenticated) {
        try {
          const enr = await learningService.getUserEnrollmentForCourse(data.id);
          setEnrollment(enr);
        } catch {
          setEnrollment(null);
        }
      }
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
        err?.response?.data?.error?.message ||
        (isTa ? 'பாட விவரங்களை ஏற்றுவதில் தோல்வி' : 'Failed to load course details.')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleEnroll = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    if (!course) return;

    try {
      setEnrolling(true);
      const newEnr = await learningService.enrollInCourse(course.id);
      setEnrollment(newEnr);
      navigate(`/course/${course.slug}/learn`);
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
        err?.response?.data?.error?.message ||
        (isTa ? 'சேர்க்கை தோல்வியடைந்தது' : 'Failed to enroll in course.')
      );
    } finally {
      setEnrolling(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[50vh] flex flex-col items-center justify-center space-y-4">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-500 border-t-transparent" />
        <p className="text-sm text-slate-400">
          {isTa ? 'பாட விவரங்கள் ஏற்றப்படுகின்றன...' : 'Loading course details...'}
        </p>
      </div>
    );
  }

  if (error || !course) {
    return (
      <div className="max-w-xl mx-auto my-12 bg-slate-900/60 p-8 rounded-2xl border border-slate-800 text-center space-y-4">
        <AlertCircle className="w-12 h-12 text-rose-400 mx-auto" />
        <h2 className="text-xl font-bold text-white">
          {isTa ? 'பாடம் கிடைக்கவில்லை' : 'Course Not Found'}
        </h2>
        <p className="text-xs text-slate-400">{error || 'The requested course does not exist or is unavailable.'}</p>
        <Link
          to="/courses"
          className="inline-flex items-center space-x-2 px-5 py-2.5 bg-amber-600 hover:bg-amber-500 text-slate-950 font-bold rounded-xl text-xs transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>{isTa ? 'பாட அட்டவணைக்குத் திரும்பு' : 'Back to Course Catalog'}</span>
        </Link>
      </div>
    );
  }

  const courseTitle = isTa && course.titleTa ? course.titleTa : course.title;
  const courseDesc = isTa && course.descriptionTa ? course.descriptionTa : course.description;
  const totalLessons = course.modules?.reduce((acc, m) => acc + (m.lessons?.length || 0), 0) || 0;

  return (
    <div className="space-y-8 max-w-5xl mx-auto">
      {/* Back Link */}
      <Link
        to="/courses"
        className="inline-flex items-center space-x-2 text-xs font-semibold text-slate-400 hover:text-amber-300 transition"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>{isTa ? 'பாட அட்டவணைக்குத் திரும்பு' : 'Back to Course Catalog'}</span>
      </Link>

      {/* Course Header Banner */}
      <div className="bg-gradient-to-r from-amber-950/70 via-slate-900 to-rose-950/60 rounded-2xl p-6 sm:p-8 border border-amber-500/30 shadow-2xl backdrop-blur-md space-y-4">
        <div className="flex flex-wrap items-center gap-2">
          <span className="px-3 py-1 bg-amber-500/20 text-amber-300 border border-amber-500/40 rounded-full text-xs font-semibold">
            {course.artForm?.name || 'Heritage Art'}
          </span>
          <span className="px-3 py-1 bg-slate-800 text-slate-300 border border-slate-700 rounded-full text-xs font-medium">
            {course.difficulty}
          </span>
          <span className="px-3 py-1 bg-emerald-500/10 text-emerald-300 border border-emerald-500/30 rounded-full text-xs font-medium">
            {isTa ? 'இலவசப் பாடம்' : 'Free Heritage Curriculum'}
          </span>
        </div>

        <h1 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-white leading-tight">
          {courseTitle}
        </h1>

        <p className="text-slate-300 text-xs sm:text-sm leading-relaxed max-w-3xl">
          {courseDesc}
        </p>

        {/* Course Meta Info */}
        <div className="pt-4 border-t border-slate-800 flex flex-wrap items-center gap-6 text-xs text-slate-400">
          <div className="flex items-center space-x-2">
            <Clock className="w-4 h-4 text-amber-400" />
            <span>{course.estimatedDurationMinutes} {isTa ? 'நிமிடம்' : 'mins'}</span>
          </div>
          <div className="flex items-center space-x-2">
            <BookOpen className="w-4 h-4 text-amber-400" />
            <span>{course.modules?.length || 0} {isTa ? 'தொகுதிகள்' : 'Modules'} ({totalLessons} {isTa ? 'பாடங்கள்' : 'Lessons'})</span>
          </div>
        </div>

        {/* Enrollment Action */}
        <div className="pt-4">
          {enrollment ? (
            <Link
              to={`/course/${course.slug}/learn`}
              className="inline-flex items-center space-x-2 px-6 py-3 bg-gradient-to-r from-amber-600 to-amber-500 text-slate-950 font-bold rounded-xl text-sm hover:brightness-110 shadow-lg shadow-amber-500/20 transition"
            >
              <PlayCircle className="w-5 h-5" />
              <span>{isTa ? 'கற்றலைத் தொடர்க (Continue Learning)' : 'Continue Learning'}</span>
            </Link>
          ) : (
            <button
              onClick={handleEnroll}
              disabled={enrolling}
              className="inline-flex items-center space-x-2 px-6 py-3 bg-gradient-to-r from-amber-600 to-amber-500 text-slate-950 font-bold rounded-xl text-sm hover:brightness-110 shadow-lg shadow-amber-500/20 transition disabled:opacity-50"
            >
              <CheckCircle2 className="w-5 h-5" />
              <span>
                {enrolling
                  ? isTa ? 'சேர்க்கப்படுகிறது...' : 'Enrolling...'
                  : isTa ? 'பாடநெறியில் சேரவும் (Enroll Now)' : 'Enroll in Course'}
              </span>
            </button>
          )}
        </div>
      </div>

      {/* Syllabus & Modules */}
      <div className="space-y-4">
        <h2 className="text-xl font-bold text-white flex items-center space-x-2">
          <BookOpen className="w-5 h-5 text-amber-400" />
          <span>{isTa ? 'பாடத்திட்டம் (Curriculum & Syllabus)' : 'Course Syllabus'}</span>
        </h2>

        <div className="space-y-4">
          {course.modules?.map((mod, idx) => (
            <div
              key={mod.id}
              className="bg-slate-900/60 rounded-2xl border border-slate-800 p-5 backdrop-blur-md space-y-3"
            >
              <div className="flex items-start justify-between">
                <div>
                  <span className="text-xs font-bold text-amber-400 uppercase tracking-wider">
                    {isTa ? `தொகுதி ${idx + 1}` : `Module ${idx + 1}`}
                  </span>
                  <h3 className="text-base font-bold text-white mt-0.5">
                    {isTa && mod.titleTa ? mod.titleTa : mod.title}
                  </h3>
                  {mod.description && (
                    <p className="text-xs text-slate-400 mt-1">
                      {isTa && mod.descriptionTa ? mod.descriptionTa : mod.description}
                    </p>
                  )}
                </div>
                <span className="text-xs text-slate-500 font-medium">
                  {mod.lessons?.length || 0} {isTa ? 'பாடங்கள்' : 'lessons'}
                </span>
              </div>

              <div className="divide-y divide-slate-800/80 pt-2">
                {mod.lessons?.map((lesson, lIdx) => (
                  <div
                    key={lesson.id}
                    className="py-2.5 flex items-center justify-between text-xs text-slate-300"
                  >
                    <div className="flex items-center space-x-3">
                      <span className="text-slate-500 font-mono w-5">
                        {idx + 1}.{lIdx + 1}
                      </span>
                      <span className="font-medium text-white">
                        {isTa && lesson.titleTa ? lesson.titleTa : lesson.title}
                      </span>
                    </div>

                    <div className="flex items-center space-x-3 text-slate-400">
                      <span>{lesson.estimatedDurationMinutes} min</span>
                      {enrollment ? (
                        <PlayCircle className="w-4 h-4 text-amber-400" />
                      ) : (
                        <Lock className="w-3.5 h-3.5 text-slate-600" />
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
