import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { BookOpen, CheckCircle, PlayCircle, XCircle } from 'lucide-react';
import { learningService } from '../services/learningService';
import { Enrollment } from '../types/learning';

export const MyLearningPage: React.FC = () => {
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
      setEnrollments(data);
    } catch (err: any) {
      setError(err?.response?.data?.error?.message || 'Failed to load your enrollments.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelEnrollment = async (courseId: string) => {
    if (!window.confirm('Are you sure you want to cancel this course enrollment? You can re-enroll anytime.')) {
      return;
    }

    try {
      await learningService.cancelEnrollment(courseId);
      await fetchMyEnrollments();
    } catch (err: any) {
      alert(err?.response?.data?.error?.message || 'Failed to cancel enrollment.');
    }
  };

  const filteredEnrollments = enrollments.filter(e => e.status === activeTab);

  return (
    <div className="min-h-screen bg-[#FFFFF0]/30 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto space-y-8">

        {/* Page Header */}
        <div className="bg-white rounded-2xl p-8 shadow-md border border-[#D4AF37]/20 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-extrabold text-[#333333]">My Learning Dashboard</h1>
            <p className="text-gray-600 mt-1">Track your progress across Tamil traditional heritage art courses.</p>
          </div>
          <Link
            to="/courses"
            className="px-5 py-2.5 bg-[#800020] text-white font-semibold rounded-xl hover:bg-[#600018] shadow transition"
          >
            Explore Catalog
          </Link>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-gray-200 gap-6 text-sm font-semibold">
          <button
            onClick={() => setActiveTab('ACTIVE')}
            className={`pb-3 border-b-2 transition ${
              activeTab === 'ACTIVE'
                ? 'border-[#800020] text-[#800020]'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            Active Courses ({enrollments.filter(e => e.status === 'ACTIVE').length})
          </button>
          <button
            onClick={() => setActiveTab('COMPLETED')}
            className={`pb-3 border-b-2 transition ${
              activeTab === 'COMPLETED'
                ? 'border-[#800020] text-[#800020]'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            Completed ({enrollments.filter(e => e.status === 'COMPLETED').length})
          </button>
          <button
            onClick={() => setActiveTab('CANCELLED')}
            className={`pb-3 border-b-2 transition ${
              activeTab === 'CANCELLED'
                ? 'border-[#800020] text-[#800020]'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            Cancelled ({enrollments.filter(e => e.status === 'CANCELLED').length})
          </button>
        </div>

        {/* Error Notification */}
        {error && (
          <div className="p-4 bg-rose-50 text-rose-700 border border-rose-200 rounded-xl">
            {error}
          </div>
        )}

        {/* Loading Skeleton */}
        {loading ? (
          <div className="space-y-4">
            {[1, 2].map((i) => (
              <div key={i} className="bg-white p-6 rounded-xl shadow animate-pulse h-28"></div>
            ))}
          </div>
        ) : filteredEnrollments.length === 0 ? (
          <div className="bg-white rounded-xl shadow p-12 text-center space-y-3">
            <BookOpen className="w-12 h-12 text-gray-300 mx-auto" />
            <h3 className="text-xl font-bold text-gray-700">No courses in this view</h3>
            <p className="text-gray-500 text-sm">
              {activeTab === 'ACTIVE' ? "You haven't enrolled in any courses yet." : 'No courses match this filter.'}
            </p>
          </div>
        ) : (
          /* Enrollment Cards */
          <div className="space-y-4">
            {filteredEnrollments.map((enr) => (
              <div
                key={enr.id}
                className="bg-white rounded-xl shadow-md p-6 border border-[#D4AF37]/20 flex flex-col md:flex-row items-start md:items-center justify-between gap-6"
              >
                <div className="space-y-2 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-semibold px-2.5 py-0.5 bg-[#800020]/10 text-[#800020] rounded border border-[#800020]/20">
                      {enr.course?.artForm?.name}
                    </span>
                    <span className="text-xs text-gray-400">
                      Enrolled: {new Date(enr.enrolledAt).toLocaleDateString()}
                    </span>
                  </div>

                  <h3 className="text-xl font-bold text-gray-900">
                    {enr.course?.title}
                  </h3>
                  <p className="text-sm font-medium text-[#D4AF37]">{enr.course?.titleTa}</p>

                  {/* Progress Bar */}
                  <div className="pt-2 max-w-md space-y-1">
                    <div className="flex justify-between text-xs font-semibold text-gray-600">
                      <span>Progress</span>
                      <span>{enr.completionPercentage}%</span>
                    </div>
                    <div className="w-full bg-gray-200 h-2 rounded-full overflow-hidden">
                      <div
                        className="bg-[#800020] h-2 rounded-full transition-all duration-300"
                        style={{ width: `${enr.completionPercentage}%` }}
                      ></div>
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex items-center gap-3">
                  {enr.status === 'ACTIVE' && (
                    <>
                      <Link
                        to={`/course/${enr.course?.slug}/learn`}
                        className="px-6 py-2.5 bg-[#D4AF37] text-[#333333] font-bold rounded-xl hover:bg-[#c49f27] transition flex items-center gap-2 text-sm shadow"
                      >
                        <PlayCircle className="w-4 h-4" />
                        <span>Continue</span>
                      </Link>
                      <button
                        onClick={() => handleCancelEnrollment(enr.course.id)}
                        className="px-3 py-2 text-gray-400 hover:text-rose-600 text-xs font-semibold"
                        title="Cancel Enrollment"
                      >
                        <XCircle className="w-5 h-5" />
                      </button>
                    </>
                  )}

                  {enr.status === 'COMPLETED' && (
                    <div className="flex items-center gap-3">
                      <span className="inline-flex items-center gap-1 text-xs font-bold text-emerald-600 bg-emerald-50 border border-emerald-200 px-3 py-1.5 rounded-lg">
                        <CheckCircle className="w-4 h-4" /> Completed
                      </span>
                      <Link
                        to={`/course/${enr.course?.slug}/learn`}
                        className="px-4 py-2 bg-gray-100 text-gray-700 text-xs font-bold rounded-lg hover:bg-gray-200"
                      >
                        Review
                      </Link>
                    </div>
                  )}

                  {enr.status === 'CANCELLED' && (
                    <Link
                      to={`/course/${enr.course?.slug}`}
                      className="px-4 py-2 bg-[#800020] text-white text-xs font-bold rounded-lg hover:bg-[#600018]"
                    >
                      Re-enroll
                    </Link>
                  )}
                </div>

              </div>
            ))}
          </div>
        )}

      </div>
    </div>
  );
};
