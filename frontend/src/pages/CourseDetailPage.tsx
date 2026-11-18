import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { BookOpen, Clock, Award, CheckCircle2, PlayCircle, Lock, ArrowLeft } from 'lucide-react';
import { learningService } from '../services/learningService';
import { CourseDetail, Enrollment } from '../types/learning';
import { useAuth } from '../context/AuthContext';

export const CourseDetailPage: React.FC = () => {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null);
  const [loading, setLoading] = useState(true);
  const [enrolling, setEnrolling] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lang, setLang] = useState<'en' | 'ta'>('en');

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
      setError(err?.response?.data?.error?.message || 'Failed to load course details.');
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
      setError(err?.response?.data?.error?.message || 'Failed to enroll in course.');
    } finally {
      setEnrolling(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#FFFFF0]/30 py-16 flex justify-center items-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#800020]"></div>
      </div>
    );
  }

  if (error || !course) {
    return (
      <div className="min-h-screen bg-[#FFFFF0]/30 py-16 px-4">
        <div className="max-w-2xl mx-auto bg-white p-8 rounded-xl shadow text-center space-y-4">
          <h2 className="text-2xl font-bold text-gray-800">Course Not Found</h2>
          <p className="text-gray-600">{error || 'The requested course does not exist or is unavailable.'}</p>
          <Link to="/courses" className="inline-block px-6 py-2.5 bg-[#800020] text-white font-semibold rounded-lg">
            Back to Courses
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#FFFFF0]/30 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-8">

        {/* Back Link */}
        <Link to="/courses" className="inline-flex items-center gap-2 text-sm font-semibold text-[#800020] hover:underline">
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Course Catalog</span>
        </Link>

        {/* Course Header Banner */}
        <div className="bg-gradient-to-br from-[#800020] via-[#5c0017] to-[#333333] text-[#FFFFF0] rounded-2xl p-8 shadow-xl space-y-6">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <span className="text-xs font-semibold px-3 py-1 bg-[#D4AF37] text-[#333333] rounded-full uppercase tracking-wider">
              {course.difficulty}
            </span>
            <button
              onClick={() => setLang(lang === 'en' ? 'ta' : 'en')}
              className="text-xs font-medium px-3 py-1 bg-white/10 hover:bg-white/20 rounded-lg transition"
            >
              {lang === 'en' ? 'தமிழ் உரைக்கு மாறவும்' : 'Switch to English'}
            </button>
          </div>

          <div className="space-y-2">
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
              {lang === 'ta' ? course.titleTa : course.title}
            </h1>
            {course.titleTa && (
              <p className="text-lg font-medium text-[#D4AF37]">{course.titleTa}</p>
            )}
          </div>

          <p className="text-[#FFFFF0]/80 text-lg max-w-3xl">
            {lang === 'ta' ? course.shortDescriptionTa : course.shortDescription}
          </p>

          <div className="flex flex-wrap items-center gap-6 pt-4 border-t border-white/10 text-sm text-[#FFFFF0]/90">
            <div className="flex items-center gap-2">
              <BookOpen className="w-5 h-5 text-[#D4AF37]" />
              <span>{course.artForm?.name} ({course.artForm?.nameTa})</span>
            </div>
            <div className="flex items-center gap-2">
              <Clock className="w-5 h-5 text-[#D4AF37]" />
              <span>{course.estimatedDurationMinutes} minutes total</span>
            </div>
            <div className="flex items-center gap-2">
              <Award className="w-5 h-5 text-[#D4AF37]" />
              <span>{course.totalLessonsCount} Lessons</span>
            </div>
          </div>

          {/* Action CTA */}
          <div className="pt-2">
            {enrollment && enrollment.status === 'ACTIVE' ? (
              <div className="flex flex-wrap items-center gap-4">
                <Link
                  to={`/course/${course.slug}/learn`}
                  className="px-8 py-3 bg-[#D4AF37] text-[#333333] font-bold rounded-xl shadow hover:bg-[#c49f27] transition flex items-center gap-2"
                >
                  <PlayCircle className="w-5 h-5" />
                  <span>Continue Learning ({enrollment.completionPercentage}% Complete)</span>
                </Link>
              </div>
            ) : enrollment && enrollment.status === 'COMPLETED' ? (
              <div className="flex items-center gap-4">
                <div className="inline-flex items-center gap-2 px-4 py-2 bg-emerald-500/20 text-emerald-300 border border-emerald-400/40 rounded-xl font-bold">
                  <CheckCircle2 className="w-5 h-5" />
                  <span>Course Completed!</span>
                </div>
                <Link
                  to={`/course/${course.slug}/learn`}
                  className="px-6 py-2.5 bg-[#D4AF37] text-[#333333] font-bold rounded-xl hover:bg-[#c49f27]"
                >
                  Review Content
                </Link>
              </div>
            ) : (
              <button
                onClick={handleEnroll}
                disabled={enrolling}
                className="px-8 py-3.5 bg-[#D4AF37] text-[#333333] text-lg font-bold rounded-xl shadow-lg hover:bg-[#c49f27] transition disabled:opacity-50"
              >
                {enrolling ? 'Enrolling...' : 'Enroll in Course (Free)'}
              </button>
            )}
          </div>
        </div>

        {/* Detailed Description */}
        <div className="bg-white rounded-xl shadow-md p-8 border border-[#D4AF37]/20 space-y-4">
          <h2 className="text-2xl font-bold text-[#333333]">Course Overview</h2>
          <p className="text-gray-700 leading-relaxed whitespace-pre-line">
            {lang === 'ta' ? course.descriptionTa : course.description}
          </p>
        </div>

        {/* Syllabus / Modules & Lessons */}
        <div className="bg-white rounded-xl shadow-md p-8 border border-[#D4AF37]/20 space-y-6">
          <h2 className="text-2xl font-bold text-[#333333]">Syllabus ({course.modules.length} Modules)</h2>

          {course.modules.length === 0 ? (
            <p className="text-gray-500 italic">No published modules available yet.</p>
          ) : (
            <div className="space-y-6">
              {course.modules.map((mod, idx) => (
                <div key={mod.id} className="border border-gray-200 rounded-xl overflow-hidden">
                  <div className="bg-gray-50 p-4 border-b border-gray-200 flex items-center justify-between">
                    <div>
                      <h3 className="font-bold text-gray-900 text-lg">
                        Module {idx + 1}: {lang === 'ta' ? mod.titleTa : mod.title}
                      </h3>
                      {mod.description && (
                        <p className="text-xs text-gray-500 mt-1">
                          {lang === 'ta' ? mod.descriptionTa : mod.description}
                        </p>
                      )}
                    </div>
                    <span className="text-xs font-semibold text-gray-500 bg-white px-2.5 py-1 rounded border">
                      {mod.lessons.length} Lessons
                    </span>
                  </div>

                  <div className="divide-y divide-gray-100">
                    {mod.lessons.map((les) => (
                      <div key={les.id} className="p-4 flex items-center justify-between hover:bg-gray-50/50 transition">
                        <div className="flex items-center gap-3">
                          <PlayCircle className="w-5 h-5 text-[#800020]" />
                          <div>
                            <p className="font-semibold text-gray-800 text-sm">
                              {lang === 'ta' ? les.titleTa : les.title}
                            </p>
                            {les.estimatedDurationMinutes > 0 && (
                              <p className="text-xs text-gray-500">{les.estimatedDurationMinutes} mins • {les.lessonType}</p>
                            )}
                          </div>
                        </div>

                        {enrollment ? (
                          <Link
                            to={`/course/${course.slug}/learn`}
                            className="text-xs font-semibold text-[#800020] hover:underline"
                          >
                            Start Lesson &rarr;
                          </Link>
                        ) : (
                          <span className="text-xs text-gray-400 flex items-center gap-1">
                            <Lock className="w-3.5 h-3.5" /> Enrolled learners only
                          </span>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

      </div>
    </div>
  );
};
