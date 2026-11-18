import React, { useState, useEffect } from 'react';
import { Plus, Sparkles } from 'lucide-react';
import { learningService } from '../services/learningService';
import { artService } from '../services/artService';
import { CourseSummary, CourseStatus, CourseDifficulty } from '../types/learning';
import { ArtFormSummary } from '../types/art';
import { useAuth } from '../context/AuthContext';

export const AdminCoursesPage: React.FC = () => {
  const { user } = useAuth();
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [artForms, setArtForms] = useState<ArtFormSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<CourseStatus | ''>('');

  // Modal State
  const [showModal, setShowModal] = useState(false);
  const [formArtFormId, setFormArtFormId] = useState('');
  const [formTitle, setFormTitle] = useState('');
  const [formTitleTa, setFormTitleTa] = useState('');
  const [formSlug, setFormSlug] = useState('');
  const [formShortDesc, setFormShortDesc] = useState('');
  const [formShortDescTa, setFormShortDescTa] = useState('');
  const [formDesc, setFormDesc] = useState('');
  const [formDescTa, setFormDescTa] = useState('');
  const [formDifficulty, setFormDifficulty] = useState<CourseDifficulty>('BEGINNER');
  const [formDuration, setFormDuration] = useState(60);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchArtForms();
    fetchAdminCourses();
  }, [statusFilter]);

  const fetchArtForms = async () => {
    try {
      const res = await artService.getPublishedArtForms({ size: 100 });
      setArtForms(res.content);
      if (res.content.length > 0) {
        setFormArtFormId(res.content[0].id);
      }
    } catch (err) {
      console.error('Failed to load art forms', err);
    }
  };

  const fetchAdminCourses = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await learningService.getAdminCourses({
        status: (statusFilter as CourseStatus) || undefined,
        size: 50
      });
      setCourses(res.content);
    } catch (err: any) {
      setError(err?.response?.data?.error?.message || 'Failed to load courses.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateCourse = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      await learningService.createCourse({
        artFormId: formArtFormId,
        title: formTitle,
        titleTa: formTitleTa,
        slug: formSlug,
        shortDescription: formShortDesc,
        shortDescriptionTa: formShortDescTa,
        description: formDesc,
        descriptionTa: formDescTa,
        difficulty: formDifficulty,
        estimatedDurationMinutes: formDuration
      });
      setShowModal(false);
      resetForm();
      fetchAdminCourses();
    } catch (err: any) {
      alert(err?.response?.data?.error?.message || 'Failed to create course');
    } finally {
      setSubmitting(false);
    }
  };

  const handleStatusChange = async (courseId: string, targetStatus: CourseStatus) => {
    try {
      await learningService.updateCourseStatus(courseId, targetStatus);
      fetchAdminCourses();
    } catch (err: any) {
      alert(err?.response?.data?.error?.message || 'Failed to update course status');
    }
  };

  const resetForm = () => {
    setFormTitle('');
    setFormTitleTa('');
    setFormSlug('');
    setFormShortDesc('');
    setFormShortDescTa('');
    setFormDesc('');
    setFormDescTa('');
  };

  const getStatusBadge = (status: CourseStatus) => {
    switch (status) {
      case 'DRAFT': return 'bg-gray-100 text-gray-800 border-gray-300';
      case 'PENDING_REVIEW': return 'bg-amber-100 text-amber-800 border-amber-300';
      case 'APPROVED': return 'bg-blue-100 text-blue-800 border-blue-300';
      case 'PUBLISHED': return 'bg-emerald-100 text-emerald-800 border-emerald-300';
      case 'ARCHIVED': return 'bg-rose-100 text-rose-800 border-rose-300';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="min-h-screen bg-[#FFFFF0]/30 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-8">

        {/* Page Header */}
        <div className="bg-gradient-to-r from-[#800020] to-[#5c0017] text-white rounded-2xl p-8 shadow-xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 px-3 py-1 bg-[#D4AF37]/20 border border-[#D4AF37]/40 rounded-full text-[#D4AF37] text-xs font-semibold">
              <Sparkles className="w-4 h-4" />
              <span>Cultural Content Governance</span>
            </div>
            <h1 className="text-3xl font-extrabold">Course Content Management</h1>
            <p className="text-white/80 text-sm">Author, review, approve, and publish Tamil heritage courses.</p>
          </div>

          <button
            onClick={() => setShowModal(true)}
            className="px-6 py-3 bg-[#D4AF37] text-[#333333] font-bold rounded-xl shadow hover:bg-[#c49f27] transition flex items-center gap-2 text-sm"
          >
            <Plus className="w-5 h-5" />
            <span>Create New Course</span>
          </button>
        </div>

        {/* Status Filter Tabs */}
        <div className="flex border-b border-gray-200 gap-4 text-xs font-bold overflow-x-auto pb-2">
          {['', 'DRAFT', 'PENDING_REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED'].map((st) => (
            <button
              key={st}
              onClick={() => setStatusFilter(st as CourseStatus)}
              className={`px-4 py-2 rounded-lg transition whitespace-nowrap ${
                statusFilter === st
                  ? 'bg-[#800020] text-white shadow'
                  : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-200'
              }`}
            >
              {st === '' ? 'ALL STATUSES' : st}
            </button>
          ))}
        </div>

        {/* Error Alert */}
        {error && (
          <div className="p-4 bg-rose-50 border border-rose-200 text-rose-700 rounded-xl text-sm">
            {error}
          </div>
        )}

        {/* Table of Courses */}
        <div className="bg-white rounded-xl shadow-md overflow-hidden border border-[#D4AF37]/20">
          {loading ? (
            <div className="p-12 text-center text-gray-500">Loading admin course directory...</div>
          ) : courses.length === 0 ? (
            <div className="p-12 text-center text-gray-500">No courses match the selected status filter.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-gray-700">
                <thead className="bg-gray-50 text-xs font-bold text-gray-500 uppercase border-b">
                  <tr>
                    <th className="px-6 py-4">Title</th>
                    <th className="px-6 py-4">Art Form</th>
                    <th className="px-6 py-4">Difficulty</th>
                    <th className="px-6 py-4">Status</th>
                    <th className="px-6 py-4 text-right">Governance Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {courses.map((course) => (
                    <tr key={course.id} className="hover:bg-gray-50/60">
                      <td className="px-6 py-4">
                        <p className="font-bold text-gray-900">{course.title}</p>
                        <p className="text-xs text-[#D4AF37] font-semibold">{course.titleTa}</p>
                        <p className="text-xs text-gray-400">/{course.slug}</p>
                      </td>
                      <td className="px-6 py-4 text-xs font-medium">
                        {course.artForm?.name}
                      </td>
                      <td className="px-6 py-4 text-xs font-semibold">
                        {course.difficulty}
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-block text-xs font-bold px-2.5 py-1 rounded-md border ${getStatusBadge(course.status)}`}>
                          {course.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right space-x-2">

                        {/* Submit for Review (Artist / Instructor / Admin) */}
                        {(course.status === 'DRAFT') && (
                          <button
                            onClick={() => handleStatusChange(course.id, 'PENDING_REVIEW')}
                            className="px-3 py-1.5 bg-amber-500 text-white rounded text-xs font-semibold hover:bg-amber-600 shadow"
                          >
                            Submit for Review
                          </button>
                        )}

                        {/* Cultural Expert Approve */}
                        {(user?.role === 'CULTURAL_EXPERT' || user?.role === 'ADMIN') && course.status === 'PENDING_REVIEW' && (
                          <button
                            onClick={() => handleStatusChange(course.id, 'APPROVED')}
                            className="px-3 py-1.5 bg-blue-600 text-white rounded text-xs font-semibold hover:bg-blue-700 shadow"
                          >
                            Approve
                          </button>
                        )}

                        {/* Admin Publish */}
                        {user?.role === 'ADMIN' && (course.status === 'APPROVED' || course.status === 'DRAFT' || course.status === 'PENDING_REVIEW') && (
                          <button
                            onClick={() => handleStatusChange(course.id, 'PUBLISHED')}
                            className="px-3 py-1.5 bg-emerald-600 text-white rounded text-xs font-semibold hover:bg-emerald-700 shadow"
                          >
                            Publish
                          </button>
                        )}

                        {/* Admin Archive */}
                        {user?.role === 'ADMIN' && course.status !== 'ARCHIVED' && (
                          <button
                            onClick={() => handleStatusChange(course.id, 'ARCHIVED')}
                            className="px-3 py-1.5 bg-rose-600 text-white rounded text-xs font-semibold hover:bg-rose-700 shadow"
                          >
                            Archive
                          </button>
                        )}

                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Create Course Modal */}
        {showModal && (
          <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl max-w-2xl w-full p-8 shadow-2xl space-y-6 max-h-[90vh] overflow-y-auto">
              <div className="flex justify-between items-center border-b pb-4">
                <h2 className="text-2xl font-bold text-gray-900">Create New Course</h2>
                <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600 text-xl font-bold">&times;</button>
              </div>

              <form onSubmit={handleCreateCourse} className="space-y-4">
                <div>
                  <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Associated Art Form</label>
                  <select
                    value={formArtFormId}
                    onChange={(e) => setFormArtFormId(e.target.value)}
                    required
                    className="w-full p-2.5 bg-gray-50 border rounded-lg text-sm"
                  >
                    {artForms.map((af) => (
                      <option key={af.id} value={af.id}>{af.name} ({af.nameTa})</option>
                    ))}
                  </select>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Course Title (English)</label>
                    <input
                      type="text"
                      required
                      value={formTitle}
                      onChange={(e) => {
                        setFormTitle(e.target.value);
                        setFormSlug(e.target.value.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)+/g, ''));
                      }}
                      className="w-full p-2.5 bg-gray-50 border rounded-lg text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Course Title (Tamil)</label>
                    <input
                      type="text"
                      required
                      value={formTitleTa}
                      onChange={(e) => setFormTitleTa(e.target.value)}
                      className="w-full p-2.5 bg-gray-50 border rounded-lg text-sm"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Slug</label>
                  <input
                    type="text"
                    required
                    value={formSlug}
                    onChange={(e) => setFormSlug(e.target.value)}
                    className="w-full p-2.5 bg-gray-50 border rounded-lg text-sm font-mono text-xs"
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Short Description (English)</label>
                    <textarea
                      required
                      rows={2}
                      value={formShortDesc}
                      onChange={(e) => setFormShortDesc(e.target.value)}
                      className="w-full p-2.5 bg-gray-50 border rounded-lg text-sm"
                    ></textarea>
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Short Description (Tamil)</label>
                    <textarea
                      required
                      rows={2}
                      value={formShortDescTa}
                      onChange={(e) => setFormShortDescTa(e.target.value)}
                      className="w-full p-2.5 bg-gray-50 border rounded-lg text-sm"
                    ></textarea>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Full Description (English)</label>
                    <textarea
                      required
                      rows={3}
                      value={formDesc}
                      onChange={(e) => setFormDesc(e.target.value)}
                      className="w-full p-2.5 bg-gray-50 border rounded-lg text-sm"
                    ></textarea>
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Full Description (Tamil)</label>
                    <textarea
                      required
                      rows={3}
                      value={formDescTa}
                      onChange={(e) => setFormDescTa(e.target.value)}
                      className="w-full p-2.5 bg-gray-50 border rounded-lg text-sm"
                    ></textarea>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Difficulty</label>
                    <select
                      value={formDifficulty}
                      onChange={(e) => setFormDifficulty(e.target.value as CourseDifficulty)}
                      className="w-full p-2.5 bg-gray-50 border rounded-lg text-sm"
                    >
                      <option value="BEGINNER">BEGINNER</option>
                      <option value="INTERMEDIATE">INTERMEDIATE</option>
                      <option value="ADVANCED">ADVANCED</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Duration (Minutes)</label>
                    <input
                      type="number"
                      min="1"
                      value={formDuration}
                      onChange={(e) => setFormDuration(parseInt(e.target.value) || 0)}
                      className="w-full p-2.5 bg-gray-50 border rounded-lg text-sm"
                    />
                  </div>
                </div>

                <div className="flex justify-end gap-3 pt-4 border-t">
                  <button
                    type="button"
                    onClick={() => setShowModal(false)}
                    className="px-5 py-2.5 bg-gray-200 text-gray-700 font-semibold rounded-lg text-sm hover:bg-gray-300"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={submitting}
                    className="px-6 py-2.5 bg-[#800020] text-white font-bold rounded-lg text-sm hover:bg-[#600018] shadow disabled:opacity-50"
                  >
                    {submitting ? 'Creating...' : 'Save Draft Course'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

      </div>
    </div>
  );
};
