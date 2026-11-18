import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  CheckCircle,
  ArrowLeft,
  ArrowRight,
  Play,
  FileText,
  Video,
  Music,
  Image as ImageIcon,
  Award,
  AlertCircle
} from 'lucide-react';
import { learningService } from '../services/learningService';
import { CourseDetail, LessonDetail, LessonSummary, CourseProgress } from '../types/learning';

export const LearningExperiencePage: React.FC = () => {
  const { slug } = useParams<{ slug: string }>();
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [currentLesson, setCurrentLesson] = useState<LessonDetail | null>(null);
  const [progress, setProgress] = useState<CourseProgress | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (slug) {
      initLearningSession();
    }
  }, [slug]);

  const initLearningSession = async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch course details
      const courseData = await learningService.getCourseBySlug(slug!);
      setCourse(courseData);

      // Fetch progress
      const progressData = await learningService.getCourseProgress(courseData.id);
      setProgress(progressData);

      // Determine initial lesson to load
      let initialLessonId = progressData.currentLessonId || progressData.lastAccessedLessonId;
      if (!initialLessonId) {
        // Fallback to first lesson in first module
        const firstModule = courseData.modules?.[0];
        if (firstModule && firstModule.lessons?.length > 0) {
          initialLessonId = firstModule.lessons[0].id;
        }
      }

      if (initialLessonId) {
        await loadLesson(courseData.slug, initialLessonId);
      }
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
        err?.response?.data?.error?.message ||
        (isTa ? 'கற்றல் அமர்வை ஏற்றுவதில் தோல்வி' : 'Failed to initialize learning experience.')
      );
    } finally {
      setLoading(false);
    }
  };

  const loadLesson = async (courseSlug: string, lessonId: string) => {
    try {
      const lessonData = await learningService.getLessonDetail(courseSlug, lessonId);
      setCurrentLesson(lessonData);

      // Mark progress started at 25% if new
      await learningService.updateLessonProgress(lessonId, { progressPercent: 25 });
      const updatedProgress = await learningService.getCourseProgress(course!.id);
      setProgress(updatedProgress);
    } catch (err: any) {
      console.error('Error loading lesson', err);
    }
  };

  const handleCompleteLesson = async () => {
    if (!currentLesson || !course) return;

    try {
      setUpdating(true);
      await learningService.updateLessonProgress(currentLesson.id, {
        progressPercent: 100,
        completed: true
      });

      const updatedProgress = await learningService.getCourseProgress(course.id);
      setProgress(updatedProgress);

      // Find next lesson
      const allLessons = getAllPublishedLessons();
      const currentIndex = allLessons.findIndex((l) => l.id === currentLesson.id);

      if (currentIndex !== -1 && currentIndex < allLessons.length - 1) {
        const nextLesson = allLessons[currentIndex + 1];
        await loadLesson(course.slug, nextLesson.id);
      }
    } catch (err: any) {
      setError(err?.response?.data?.error?.message || err?.message || 'Failed to update progress.');
    } finally {
      setUpdating(false);
    }
  };

  const getAllPublishedLessons = (): LessonSummary[] => {
    if (!course || !course.modules) return [];
    const list: LessonSummary[] = [];
    course.modules.forEach((m) => {
      m.lessons?.forEach((l) => list.push(l));
    });
    return list;
  };

  const allLessons = getAllPublishedLessons();
  const currentIndex = currentLesson ? allLessons.findIndex((l) => l.id === currentLesson.id) : -1;
  const prevLesson = currentIndex > 0 ? allLessons[currentIndex - 1] : null;
  const nextLesson = currentIndex !== -1 && currentIndex < allLessons.length - 1 ? allLessons[currentIndex + 1] : null;

  if (loading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center space-y-4">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-500 border-t-transparent" />
        <p className="text-sm text-slate-400">
          {isTa ? 'கற்றல் பகுதி ஏற்றப்படுகிறது...' : 'Loading learning workspace...'}
        </p>
      </div>
    );
  }

  if (error || !course) {
    return (
      <div className="max-w-xl mx-auto my-12 bg-slate-900/60 p-8 rounded-2xl border border-slate-800 text-center space-y-4">
        <AlertCircle className="w-12 h-12 text-rose-400 mx-auto" />
        <h2 className="text-xl font-bold text-white">
          {isTa ? 'கற்றல் அமர்வுப் பிழை' : 'Learning Experience Error'}
        </h2>
        <p className="text-xs text-slate-400">{error || 'Could not load course.'}</p>
        <Link
          to="/courses"
          className="inline-block px-5 py-2.5 bg-amber-600 hover:bg-amber-500 text-slate-950 font-bold rounded-xl text-xs transition"
        >
          {isTa ? 'பாட அட்டவணைக்குத் திரும்பு' : 'Return to Catalog'}
        </Link>
      </div>
    );
  }

  const courseTitle = isTa && course.titleTa ? course.titleTa : course.title;

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Top Breadcrumb Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4 p-4 rounded-2xl bg-slate-900/80 border border-slate-800 backdrop-blur-md">
        <div className="flex items-center space-x-3">
          <Link
            to={`/course/${course.slug}`}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition"
            title={isTa ? 'பாட விவரங்கள்' : 'Course Details'}
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <h1 className="font-bold text-sm sm:text-base text-white truncate max-w-md">
              {courseTitle}
            </h1>
            <p className="text-[11px] text-amber-400 font-medium">
              {course.artForm?.name || 'Heritage Art'}
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-2">
          {progress && (
            <div className="hidden sm:flex items-center space-x-2 text-xs text-slate-400 bg-slate-950 px-3 py-1.5 rounded-xl border border-slate-800">
              <span>{isTa ? 'முன்னேற்றம்:' : 'Progress:'}</span>
              <span className="font-mono text-amber-400 font-bold">{progress.completionPercentage}%</span>
            </div>
          )}
        </div>
      </div>

      {/* 3-Column Learning Body */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column: Module & Lesson Sidebar */}
        <aside className="lg:col-span-3 bg-slate-900/60 rounded-2xl border border-slate-800 p-4 space-y-4 max-h-[750px] overflow-y-auto backdrop-blur-md">
          <h2 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
            {isTa ? 'பாடத்திட்டம்' : 'Course Syllabus'}
          </h2>

          <div className="space-y-4">
            {course.modules?.map((mod, idx) => (
              <div key={mod.id} className="space-y-1.5">
                <div className="text-[11px] font-bold text-amber-400 uppercase tracking-wider px-2">
                  {isTa ? `தொகுதி ${idx + 1}` : `Module ${idx + 1}`}: {isTa && mod.titleTa ? mod.titleTa : mod.title}
                </div>
                <div className="space-y-1">
                  {mod.lessons?.map((les) => {
                    const isSelected = currentLesson?.id === les.id;
                    return (
                      <button
                        key={les.id}
                        onClick={() => loadLesson(course.slug, les.id)}
                        className={`w-full text-left px-3 py-2 rounded-xl text-xs font-medium transition flex items-center justify-between ${
                          isSelected
                            ? 'bg-amber-500/20 text-amber-200 border border-amber-500/40 font-bold shadow'
                            : 'hover:bg-slate-800/60 text-slate-300'
                        }`}
                      >
                        <div className="flex items-center space-x-2 truncate">
                          <Play className="w-3.5 h-3.5 flex-shrink-0 text-amber-400" />
                          <span className="truncate">{isTa && les.titleTa ? les.titleTa : les.title}</span>
                        </div>
                      </button>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        </aside>

        {/* Center Column: Active Lesson Workspace */}
        <main className="lg:col-span-6 bg-slate-900/60 rounded-2xl border border-slate-800 p-6 space-y-6 backdrop-blur-md">
          {currentLesson ? (
            <div className="space-y-6">
              {/* Lesson Title & Type Badge */}
              <div className="space-y-2 border-b border-slate-800 pb-4">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-bold uppercase tracking-wider px-2.5 py-0.5 bg-amber-500/10 text-amber-300 rounded border border-amber-500/30">
                    {currentLesson.lessonType} LESSON
                  </span>
                  <span className="text-xs text-slate-400">
                    {currentLesson.estimatedDurationMinutes} {isTa ? 'நிமிடம்' : 'mins'}
                  </span>
                </div>

                <h2 className="text-xl sm:text-2xl font-extrabold text-white">
                  {isTa && currentLesson.titleTa ? currentLesson.titleTa : currentLesson.title}
                </h2>
                {currentLesson.titleTa && !isTa && (
                  <p className="text-amber-400 text-xs font-semibold">{currentLesson.titleTa}</p>
                )}
              </div>

              {/* Multimedia Media Items Container */}
              {currentLesson.mediaItems && currentLesson.mediaItems.length > 0 && (
                <div className="space-y-4">
                  {currentLesson.mediaItems.map((media) => (
                    <div
                      key={media.id}
                      className="bg-slate-950 rounded-xl overflow-hidden border border-slate-800 p-4 space-y-3"
                    >
                      <div className="flex items-center space-x-2 text-amber-300 text-xs font-semibold">
                        {media.mediaType === 'VIDEO' && <Video className="w-4 h-4" />}
                        {media.mediaType === 'AUDIO' && <Music className="w-4 h-4" />}
                        {media.mediaType === 'IMAGE' && <ImageIcon className="w-4 h-4" />}
                        {media.mediaType === 'DOCUMENT' && <FileText className="w-4 h-4" />}
                        <span>{isTa ? media.titleTa || media.title : media.title}</span>
                      </div>

                      {/* Video Player */}
                      {media.mediaType === 'VIDEO' && (
                        <div className="aspect-video bg-black rounded-lg overflow-hidden flex items-center justify-center border border-slate-800">
                          <video
                            controls
                            className="w-full h-full"
                            src={media.url}
                            poster={media.thumbnailUrl}
                          >
                            <track kind="captions" label="English" srcLang="en" />
                            Your browser does not support HTML5 video.
                          </video>
                        </div>
                      )}

                      {/* Audio Player */}
                      {media.mediaType === 'AUDIO' && (
                        <div className="p-3 bg-slate-900 rounded-lg">
                          <audio controls className="w-full" src={media.url}>
                            Your browser does not support audio elements.
                          </audio>
                        </div>
                      )}

                      {/* Image Viewer */}
                      {media.mediaType === 'IMAGE' && (
                        <div className="rounded-lg overflow-hidden bg-black max-h-96 flex items-center justify-center">
                          <img
                            src={media.url}
                            alt={isTa ? media.altTextTa || media.altText : media.altText || media.title}
                            className="max-h-96 object-contain"
                          />
                        </div>
                      )}

                      {/* Document Viewer */}
                      {media.mediaType === 'DOCUMENT' && (
                        <div className="p-3 bg-slate-900 rounded-lg flex items-center justify-between text-xs">
                          <span>{media.title} ({media.mimeType || 'PDF'})</span>
                          <a
                            href={media.url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="px-3 py-1 bg-amber-600 text-slate-950 rounded font-bold hover:bg-amber-500 transition"
                          >
                            View
                          </a>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}

              {/* Text Lesson Content */}
              <div className="text-slate-300 leading-relaxed bg-slate-950/60 p-5 rounded-xl border border-slate-800 text-sm whitespace-pre-line">
                {isTa
                  ? currentLesson.contentTa || currentLesson.content
                  : currentLesson.content || 'Lesson content is displayed here.'}
              </div>

              {/* Action Controls */}
              <div className="flex flex-wrap items-center justify-between gap-3 pt-4 border-t border-slate-800">
                <button
                  disabled={!prevLesson}
                  onClick={() => prevLesson && loadLesson(course.slug, prevLesson.id)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 font-semibold rounded-xl hover:bg-slate-700 disabled:opacity-30 flex items-center space-x-1.5 text-xs transition"
                >
                  <ArrowLeft className="w-4 h-4" />
                  <span>{isTa ? 'முந்தைய பாடம்' : 'Previous Lesson'}</span>
                </button>

                <button
                  onClick={handleCompleteLesson}
                  disabled={updating}
                  className="px-6 py-2.5 bg-gradient-to-r from-amber-600 to-amber-500 text-slate-950 font-bold rounded-xl shadow-md hover:brightness-110 transition flex items-center space-x-2 text-xs disabled:opacity-50"
                >
                  <CheckCircle className="w-4 h-4" />
                  <span>
                    {updating
                      ? isTa ? 'சேமிக்கப்படுகிறது...' : 'Saving...'
                      : nextLesson
                      ? isTa ? 'முடித்து அடுத்த பாடம்' : 'Mark Complete & Next'
                      : isTa ? 'பாடத்தை நிறைவு செய்' : 'Complete Lesson'}
                  </span>
                </button>

                <button
                  disabled={!nextLesson}
                  onClick={() => nextLesson && loadLesson(course.slug, nextLesson.id)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 font-semibold rounded-xl hover:bg-slate-700 disabled:opacity-30 flex items-center space-x-1.5 text-xs transition"
                >
                  <span>{isTa ? 'அடுத்த பாடம்' : 'Next Lesson'}</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          ) : (
            <div className="text-center py-20 text-slate-500 text-xs">
              {isTa ? 'இடது பக்கப்பட்டியிலிருந்து ஒரு பாடத்தைத் தேர்ந்தெடுக்கவும்.' : 'Select a lesson from the left sidebar to start learning.'}
            </div>
          )}
        </main>

        {/* Right Column: Progress Dashboard */}
        <aside className="lg:col-span-3 bg-slate-900/60 rounded-2xl border border-slate-800 p-5 space-y-5 backdrop-blur-md">
          <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
            {isTa ? 'உங்கள் முன்னேற்றம்' : 'Your Progress'}
          </h3>

          {progress && (
            <div className="space-y-4">
              <div>
                <div className="flex items-center justify-between text-xs font-semibold text-slate-300 mb-1.5">
                  <span>{isTa ? 'ஒட்டுமொத்த நிறைவு' : 'Overall Completion'}</span>
                  <span className="font-mono text-amber-400 font-bold">{progress.completionPercentage}%</span>
                </div>
                <div className="w-full bg-slate-950 rounded-full h-2.5 overflow-hidden border border-slate-800">
                  <div
                    className="bg-gradient-to-r from-amber-600 to-amber-400 h-2.5 rounded-full transition-all duration-500"
                    style={{ width: `${progress.completionPercentage}%` }}
                  />
                </div>
              </div>

              <div className="bg-slate-950 p-4 rounded-xl space-y-2 text-xs text-slate-300 border border-slate-800/80">
                <div className="flex justify-between">
                  <span className="text-slate-400">{isTa ? 'மொத்தப் பாடங்கள்:' : 'Total Lessons:'}</span>
                  <span className="font-bold text-white">{progress.totalPublishedLessons}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">{isTa ? 'நிறைவு செய்தவை:' : 'Completed:'}</span>
                  <span className="font-bold text-emerald-400">{progress.completedLessons}</span>
                </div>
              </div>

              {progress.isCourseCompleted && (
                <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 rounded-xl text-center space-y-2">
                  <CheckCircle className="w-8 h-8 text-emerald-400 mx-auto" />
                  <p className="font-bold text-emerald-300 text-sm">
                    {isTa ? 'பாடம் வெற்றிகரமாக முடிந்தது!' : 'Course Completed!'}
                  </p>
                  <p className="text-xs text-emerald-200/80">
                    {isTa
                      ? 'அனைத்துப் பாடங்களையும் வெற்றிகரமாக நிறைவு செய்துள்ளீர்கள்.'
                      : 'You have completed all published lessons in this course.'}
                  </p>
                  <Link
                    to="/certificates"
                    className="inline-flex items-center space-x-1.5 px-3 py-1.5 bg-amber-500/20 text-amber-300 border border-amber-500/30 rounded-lg text-xs font-bold hover:bg-amber-500/30 transition mt-1"
                  >
                    <Award className="w-3.5 h-3.5" />
                    <span>{isTa ? 'சான்றிதழ் பார்க்க' : 'View Certificate'}</span>
                  </Link>
                </div>
              )}
            </div>
          )}
        </aside>
      </div>
    </div>
  );
};
