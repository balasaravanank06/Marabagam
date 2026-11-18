import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { CheckCircle, ArrowLeft, ArrowRight, Play, FileText, Video, Music, Image as ImageIcon } from 'lucide-react';
import { learningService } from '../services/learningService';
import { CourseDetail, LessonDetail, LessonSummary, CourseProgress } from '../types/learning';

export const LearningExperiencePage: React.FC = () => {
  const { slug } = useParams<{ slug: string }>();

  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [currentLesson, setCurrentLesson] = useState<LessonDetail | null>(null);
  const [progress, setProgress] = useState<CourseProgress | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lang, setLang] = useState<'en' | 'ta'>('en');

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
        const firstModule = courseData.modules[0];
        if (firstModule && firstModule.lessons.length > 0) {
          initialLessonId = firstModule.lessons[0].id;
        }
      }

      if (initialLessonId) {
        await loadLesson(courseData.slug, initialLessonId);
      }
    } catch (err: any) {
      setError(err?.response?.data?.error?.message || 'Failed to initialize learning experience.');
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
      const currentIndex = allLessons.findIndex(l => l.id === currentLesson.id);

      if (currentIndex !== -1 && currentIndex < allLessons.length - 1) {
        const nextLesson = allLessons[currentIndex + 1];
        await loadLesson(course.slug, nextLesson.id);
      }
    } catch (err: any) {
      setError(err?.response?.data?.error?.message || 'Failed to update progress.');
    } finally {
      setUpdating(false);
    }
  };

  const getAllPublishedLessons = (): LessonSummary[] => {
    if (!course) return [];
    const list: LessonSummary[] = [];
    course.modules.forEach(m => {
      m.lessons.forEach(l => list.push(l));
    });
    return list;
  };

  const allLessons = getAllPublishedLessons();
  const currentIndex = currentLesson ? allLessons.findIndex(l => l.id === currentLesson.id) : -1;
  const prevLesson = currentIndex > 0 ? allLessons[currentIndex - 1] : null;
  const nextLesson = currentIndex !== -1 && currentIndex < allLessons.length - 1 ? allLessons[currentIndex + 1] : null;

  if (loading) {
    return (
      <div className="min-h-screen bg-[#333333] text-[#FFFFF0] flex justify-center items-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#D4AF37]"></div>
      </div>
    );
  }

  if (error || !course) {
    return (
      <div className="min-h-screen bg-[#333333] text-[#FFFFF0] py-16 px-4">
        <div className="max-w-xl mx-auto bg-gray-800 p-8 rounded-xl text-center space-y-4">
          <h2 className="text-2xl font-bold">Learning Experience Error</h2>
          <p className="text-gray-300">{error || 'Could not load course.'}</p>
          <Link to="/courses" className="inline-block px-6 py-2.5 bg-[#800020] text-white font-semibold rounded-lg">
            Return to Catalog
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#1e1e1e] text-gray-100 flex flex-col">

      {/* Top Header Navigation */}
      <header className="bg-[#800020] px-6 py-4 flex items-center justify-between border-b border-[#D4AF37]/30 shadow-md">
        <div className="flex items-center gap-4">
          <Link to={`/course/${course.slug}`} className="text-white hover:text-[#D4AF37] transition">
            <ArrowLeft className="w-6 h-6" />
          </Link>
          <div>
            <h1 className="font-extrabold text-lg text-white">
              {lang === 'ta' ? course.titleTa : course.title}
            </h1>
            <p className="text-xs text-[#D4AF37] font-medium">{course.artForm?.name}</p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <button
            onClick={() => setLang(lang === 'en' ? 'ta' : 'en')}
            className="px-3 py-1 bg-black/30 hover:bg-black/50 text-xs font-semibold text-[#D4AF37] border border-[#D4AF37]/40 rounded-md"
          >
            {lang === 'en' ? 'தமிழில் படிக்க' : 'English'}
          </button>
        </div>
      </header>

      {/* 3-Column Learning Body */}
      <div className="flex-1 flex flex-col md:flex-row overflow-hidden">

        {/* Left Column: Module & Lesson Sidebar */}
        <aside className="w-full md:w-80 bg-gray-900 border-r border-gray-800 overflow-y-auto p-4 space-y-4">
          <h2 className="text-sm font-bold text-gray-400 uppercase tracking-wider">Course Syllabus</h2>

          <div className="space-y-4">
            {course.modules.map((mod, idx) => (
              <div key={mod.id} className="space-y-2">
                <div className="text-xs font-semibold text-[#D4AF37] px-2">
                  Module {idx + 1}: {lang === 'ta' ? mod.titleTa : mod.title}
                </div>
                <div className="space-y-1">
                  {mod.lessons.map((les) => {
                    const isSelected = currentLesson?.id === les.id;
                    return (
                      <button
                        key={les.id}
                        onClick={() => loadLesson(course.slug, les.id)}
                        className={`w-full text-left px-3 py-2.5 rounded-lg text-xs font-medium transition flex items-center justify-between ${
                          isSelected
                            ? 'bg-[#800020] text-white shadow font-bold'
                            : 'hover:bg-gray-800 text-gray-300'
                        }`}
                      >
                        <div className="flex items-center gap-2 truncate">
                          <Play className="w-3.5 h-3.5 flex-shrink-0" />
                          <span className="truncate">{lang === 'ta' ? les.titleTa : les.title}</span>
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
        <main className="flex-1 overflow-y-auto p-6 md:p-10 space-y-8 bg-[#181818]">

          {currentLesson ? (
            <div className="max-w-4xl mx-auto space-y-8">

              {/* Lesson Title & Type Badge */}
              <div className="space-y-2 border-b border-gray-800 pb-4">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold uppercase tracking-wider px-2.5 py-1 bg-gray-800 text-[#D4AF37] rounded border border-gray-700">
                    {currentLesson.lessonType} LESSON
                  </span>
                  <span className="text-xs text-gray-400">
                    Duration: {currentLesson.estimatedDurationMinutes} mins
                  </span>
                </div>

                <h2 className="text-2xl sm:text-3xl font-extrabold text-white">
                  {lang === 'ta' ? currentLesson.titleTa : currentLesson.title}
                </h2>
                {currentLesson.titleTa && lang === 'en' && (
                  <p className="text-[#D4AF37] font-semibold">{currentLesson.titleTa}</p>
                )}
              </div>

              {/* Multimedia Media Items Container */}
              {currentLesson.mediaItems && currentLesson.mediaItems.length > 0 && (
                <div className="space-y-4">
                  {currentLesson.mediaItems.map((media) => (
                    <div key={media.id} className="bg-gray-900 rounded-xl overflow-hidden border border-gray-800 p-4 space-y-3">
                      <div className="flex items-center gap-2 text-[#D4AF37] text-sm font-semibold">
                        {media.mediaType === 'VIDEO' && <Video className="w-4 h-4" />}
                        {media.mediaType === 'AUDIO' && <Music className="w-4 h-4" />}
                        {media.mediaType === 'IMAGE' && <ImageIcon className="w-4 h-4" />}
                        {media.mediaType === 'DOCUMENT' && <FileText className="w-4 h-4" />}
                        <span>{lang === 'ta' ? media.titleTa || media.title : media.title}</span>
                      </div>

                      {/* Video Player */}
                      {media.mediaType === 'VIDEO' && (
                        <div className="aspect-video bg-black rounded-lg overflow-hidden flex items-center justify-center">
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
                        <div className="p-4 bg-gray-950 rounded-lg">
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
                            alt={lang === 'ta' ? media.altTextTa || media.altText : media.altText || media.title}
                            className="max-h-96 object-contain"
                          />
                        </div>
                      )}

                      {/* Document Viewer */}
                      {media.mediaType === 'DOCUMENT' && (
                        <div className="p-4 bg-gray-800 rounded-lg flex items-center justify-between text-sm">
                          <span>{media.title} ({media.mimeType || 'PDF/Doc'})</span>
                          <a
                            href={media.url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="px-4 py-1.5 bg-[#800020] text-white rounded font-semibold hover:bg-[#600018]"
                          >
                            View Document
                          </a>
                        </div>
                      )}

                      {media.altText && (
                        <p className="text-xs text-gray-400 italic">
                          Alt: {lang === 'ta' ? media.altTextTa || media.altText : media.altText}
                        </p>
                      )}
                    </div>
                  ))}
                </div>
              )}

              {/* Text Lesson Content */}
              <div className="prose prose-invert max-w-none text-gray-300 leading-relaxed bg-gray-900/60 p-6 rounded-xl border border-gray-800 whitespace-pre-line">
                {lang === 'ta' ? currentLesson.contentTa || currentLesson.content : currentLesson.content || 'Lesson content is displayed here.'}
              </div>

              {/* Action Controls */}
              <div className="flex flex-wrap items-center justify-between gap-4 pt-6 border-t border-gray-800">
                <button
                  disabled={!prevLesson}
                  onClick={() => prevLesson && loadLesson(course.slug, prevLesson.id)}
                  className="px-5 py-2.5 bg-gray-800 text-gray-300 font-semibold rounded-lg hover:bg-gray-700 disabled:opacity-30 flex items-center gap-2 text-sm"
                >
                  <ArrowLeft className="w-4 h-4" /> Previous Lesson
                </button>

                <button
                  onClick={handleCompleteLesson}
                  disabled={updating}
                  className="px-8 py-3 bg-[#D4AF37] text-[#333333] font-bold rounded-xl shadow hover:bg-[#c49f27] transition flex items-center gap-2 text-sm"
                >
                  <CheckCircle className="w-5 h-5" />
                  <span>{updating ? 'Saving...' : nextLesson ? 'Mark Complete & Next' : 'Complete Lesson'}</span>
                </button>

                <button
                  disabled={!nextLesson}
                  onClick={() => nextLesson && loadLesson(course.slug, nextLesson.id)}
                  className="px-5 py-2.5 bg-gray-800 text-gray-300 font-semibold rounded-lg hover:bg-gray-700 disabled:opacity-30 flex items-center gap-2 text-sm"
                >
                  Next Lesson <ArrowRight className="w-4 h-4" />
                </button>
              </div>

            </div>
          ) : (
            <div className="text-center py-20 text-gray-500">
              Select a lesson from the left sidebar to start learning.
            </div>
          )}

        </main>

        {/* Right Column: Progress Dashboard */}
        <aside className="w-full md:w-72 bg-gray-900 border-l border-gray-800 p-6 space-y-6">
          <h3 className="text-sm font-bold text-[#D4AF37] uppercase tracking-wider">Your Progress</h3>

          {progress && (
            <div className="space-y-4">
              <div>
                <div className="flex items-center justify-between text-xs font-semibold text-gray-300 mb-1.5">
                  <span>Overall Completion</span>
                  <span>{progress.completionPercentage}%</span>
                </div>
                <div className="w-full bg-gray-800 rounded-full h-2.5 overflow-hidden">
                  <div
                    className="bg-[#D4AF37] h-2.5 rounded-full transition-all duration-500"
                    style={{ width: `${progress.completionPercentage}%` }}
                  ></div>
                </div>
              </div>

              <div className="bg-gray-800 p-4 rounded-xl space-y-2 text-xs text-gray-300">
                <div className="flex justify-between">
                  <span>Total Published:</span>
                  <span className="font-bold text-white">{progress.totalPublishedLessons}</span>
                </div>
                <div className="flex justify-between">
                  <span>Completed:</span>
                  <span className="font-bold text-emerald-400">{progress.completedLessons}</span>
                </div>
              </div>

              {progress.isCourseCompleted && (
                <div className="p-4 bg-emerald-500/20 border border-emerald-500/40 rounded-xl text-center space-y-1">
                  <CheckCircle className="w-8 h-8 text-emerald-400 mx-auto" />
                  <p className="font-bold text-emerald-300 text-sm">Course Completed!</p>
                  <p className="text-xs text-emerald-200">You have completed all published lessons in this course.</p>
                </div>
              )}
            </div>
          )}
        </aside>

      </div>
    </div>
  );
};
