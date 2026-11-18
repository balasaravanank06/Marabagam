import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { artService } from '../services/artService';
import { ArtFormStatus, ArtFormSummary, CreateArtFormPayload } from '../types/art';
import {
  ShieldAlert,
  Plus,
  Filter,
  Eye,
  Trash2,
  Sparkles,
} from 'lucide-react';

export const AdminArtFormsPage: React.FC = () => {
  const { user } = useAuth();
  const [artForms, setArtForms] = useState<ArtFormSummary[]>([]);

  const [statusFilter, setStatusFilter] = useState<ArtFormStatus | ''>('');
  const [loading, setLoading] = useState(true);

  // Form State for Creating New Art Form
  const [isCreating, setIsCreating] = useState(false);
  const [formData, setFormData] = useState<CreateArtFormPayload>({
    name: '',
    nameTa: '',
    slug: '',
    shortDescription: '',
    shortDescriptionTa: '',
    description: '',
    descriptionTa: '',
    origin: '',
    originTa: '',
    categoryIds: [],
    districtIds: [],
  });

  const fetchManagementData = async () => {
    setLoading(true);
    try {
      const data = await artService.getArtFormsForManagement({ status: statusFilter || undefined });
      setArtForms(data.content);
    } catch (err: any) {
      console.error('Failed to load content management list:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchManagementData();
  }, [statusFilter]);

  const handleStatusChange = async (id: string, newStatus: ArtFormStatus) => {
    try {
      await artService.updateArtFormStatus(id, newStatus);
      await fetchManagementData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'நிலை மாற்றுவதில் தோல்வி / Failed to update status');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('இந்த கலை வடிவத்தை நிச்சயமாக நீக்க விரும்புகிறீர்களா? / Are you sure you want to delete this art form?')) return;
    try {
      await artService.deleteArtForm(id);
      await fetchManagementData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'நீக்குவதில் தோல்வி / Failed to delete');
    }
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await artService.createArtForm(formData);
      setIsCreating(false);
      setFormData({
        name: '',
        nameTa: '',
        slug: '',
        shortDescription: '',
        shortDescriptionTa: '',
        description: '',
        descriptionTa: '',
        origin: '',
        originTa: '',
        categoryIds: [],
        districtIds: [],
      });
      await fetchManagementData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'உருவாக்குவதில் தோல்வி / Failed to create art form');
    }
  };

  if (!user || user.role === 'LEARNER' || user.role === 'GUEST') {
    return (
      <div className="max-w-xl mx-auto my-12 p-8 rounded-2xl bg-amber-950/40 border border-red-500/30 text-center space-y-4">
        <ShieldAlert className="w-12 h-12 text-red-400 mx-auto" />
        <h2 className="text-2xl font-bold text-red-200">அனுமதி இல்லை / Access Denied</h2>
        <p className="text-sm text-slate-300">
          Only Artists, Instructors, Cultural Experts, and Administrators may access cultural content governance.
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto space-y-8">
      {/* Admin Header */}
      <div className="p-8 rounded-2xl bg-gradient-to-r from-slate-900 via-amber-950/50 to-slate-950 border border-amber-500/20 shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs font-semibold">
            <Sparkles className="w-4 h-4 text-amber-400" />
            <span>CULTURAL CONTENT GOVERNANCE • பண்பாட்டு உள்ளடக்க மேலாண்மை</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white mt-2">
            கலை வடிவங்கள் மேலாண்மை / Art Directory Management
          </h1>
          <p className="text-xs text-slate-400">
            Active Role: <span className="text-amber-400 font-bold">{user.role}</span>
          </p>
        </div>

        <button
          onClick={() => setIsCreating(!isCreating)}
          className="py-3 px-4 rounded-xl bg-amber-600 hover:bg-amber-500 text-slate-950 font-bold text-xs transition shadow-lg shadow-amber-600/20 flex items-center space-x-2 self-start md:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>புதிய கலை வடிவம் சேர் / Create New Art Form</span>
        </button>
      </div>

      {/* Form Modal for Creating New Art Form */}
      {isCreating && (
        <form onSubmit={handleCreateSubmit} className="p-8 rounded-2xl bg-slate-900 border border-amber-500/30 space-y-6 shadow-2xl">
          <h2 className="text-xl font-bold text-white border-b border-slate-800 pb-3">
            புதிய கலை வடிவம் உருவாக்குதல் / Create New Cultural Art Form (Initial: DRAFT)
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-amber-200 uppercase mb-1">English Name *</label>
              <input
                type="text"
                required
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Silambam"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-white text-xs"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-amber-200 uppercase mb-1">Tamil Name *</label>
              <input
                type="text"
                required
                value={formData.nameTa}
                onChange={(e) => setFormData({ ...formData, nameTa: e.target.value })}
                placeholder="சிலம்பம்"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-white text-xs"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-amber-200 uppercase mb-1">Unique Slug *</label>
              <input
                type="text"
                required
                value={formData.slug}
                onChange={(e) => setFormData({ ...formData, slug: e.target.value.toLowerCase().replace(/\s+/g, '-') })}
                placeholder="silambam"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-white text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-amber-200 uppercase mb-1">Short Description (EN) *</label>
              <textarea
                required
                rows={2}
                value={formData.shortDescription}
                onChange={(e) => setFormData({ ...formData, shortDescription: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-white text-xs"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-amber-200 uppercase mb-1">Short Description (TA) *</label>
              <textarea
                required
                rows={2}
                value={formData.shortDescriptionTa}
                onChange={(e) => setFormData({ ...formData, shortDescriptionTa: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-white text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-amber-200 uppercase mb-1">Full Description (EN) *</label>
              <textarea
                required
                rows={4}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-white text-xs"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-amber-200 uppercase mb-1">Full Description (TA) *</label>
              <textarea
                required
                rows={4}
                value={formData.descriptionTa}
                onChange={(e) => setFormData({ ...formData, descriptionTa: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-white text-xs"
              />
            </div>
          </div>

          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={() => setIsCreating(false)}
              className="px-4 py-2 rounded-lg bg-slate-800 text-slate-300 text-xs font-semibold"
            >
              ரத்து செய் / Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2 rounded-lg bg-amber-600 text-slate-950 font-bold text-xs hover:bg-amber-500"
            >
              சேமி / Submit Draft
            </button>
          </div>
        </form>
      )}

      {/* Filter Toolbar */}
      <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <Filter className="w-4 h-4 text-amber-400" />
          <span className="text-xs font-bold text-slate-300">Filter Status:</span>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as ArtFormStatus | '')}
            className="px-3 py-1.5 bg-slate-950 border border-slate-800 rounded-lg text-white text-xs"
          >
            <option value="">All Statuses (அனைத்தும்)</option>
            <option value="DRAFT">DRAFT</option>
            <option value="PENDING_REVIEW">PENDING_REVIEW</option>
            <option value="APPROVED">APPROVED</option>
            <option value="PUBLISHED">PUBLISHED</option>
            <option value="ARCHIVED">ARCHIVED</option>
          </select>
        </div>
      </div>

      {/* Art Form Management Table */}
      <div className="rounded-2xl bg-slate-900/60 border border-slate-800 overflow-hidden shadow-xl">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-800 bg-slate-950/80 text-[11px] font-bold text-amber-300 uppercase tracking-wider">
              <th className="p-4">Art Form</th>
              <th className="p-4">Categories</th>
              <th className="p-4">Lifecycle Status</th>
              <th className="p-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60 text-xs">
            {loading ? (
              <tr>
                <td colSpan={4} className="p-8 text-center text-slate-400 animate-pulse">
                  ஏற்றுகிறது... / Loading...
                </td>
              </tr>
            ) : artForms.length === 0 ? (
              <tr>
                <td colSpan={4} className="p-8 text-center text-slate-400">
                  உள்ளடக்கங்கள் எதுவும் இல்லை / No art forms found for this filter.
                </td>
              </tr>
            ) : (
              artForms.map((art) => (
                <tr key={art.id} className="hover:bg-slate-950/50 transition">
                  <td className="p-4">
                    <div className="font-bold text-white">{art.name}</div>
                    <div className="text-amber-400 font-medium text-[11px]">{art.nameTa}</div>
                    <div className="text-slate-500 font-mono text-[10px]">slug: {art.slug}</div>
                  </td>
                  <td className="p-4">
                    <div className="flex flex-wrap gap-1">
                      {art.categories.map((c) => (
                        <span key={c.id} className="px-2 py-0.5 rounded bg-slate-800 text-amber-300 text-[10px]">
                          {c.name}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="p-4">
                    <span
                      className={`px-2.5 py-1 rounded-full text-[10px] font-extrabold border ${
                        art.status === 'PUBLISHED'
                          ? 'bg-emerald-500/20 border-emerald-500/40 text-emerald-300'
                          : art.status === 'APPROVED'
                          ? 'bg-cyan-500/20 border-cyan-500/40 text-cyan-300'
                          : art.status === 'PENDING_REVIEW'
                          ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                          : 'bg-slate-800 border-slate-700 text-slate-400'
                      }`}
                    >
                      {art.status}
                    </span>
                  </td>
                  <td className="p-4 text-right space-x-2">
                    {/* View Button */}
                    <a
                      href={`/art-forms/${art.slug}`}
                      target="_blank"
                      rel="noreferrer"
                      className="inline-flex items-center p-1.5 rounded bg-slate-800 text-slate-300 hover:text-white"
                      title="View Details"
                    >
                      <Eye className="w-3.5 h-3.5" />
                    </a>

                    {/* Transition to PENDING_REVIEW */}
                    {art.status === 'DRAFT' && (
                      <button
                        onClick={() => handleStatusChange(art.id, 'PENDING_REVIEW')}
                        className="px-2.5 py-1 rounded bg-amber-500/20 text-amber-300 border border-amber-500/30 text-[10px] font-bold"
                      >
                        Submit Review
                      </button>
                    )}

                    {/* Transition to APPROVED (Cultural Expert or Admin) */}
                    {art.status === 'PENDING_REVIEW' && (user.role === 'CULTURAL_EXPERT' || user.role === 'ADMIN') && (
                      <button
                        onClick={() => handleStatusChange(art.id, 'APPROVED')}
                        className="px-2.5 py-1 rounded bg-cyan-500/20 text-cyan-300 border border-cyan-500/30 text-[10px] font-bold"
                      >
                        Approve
                      </button>
                    )}

                    {/* Transition to PUBLISHED (Admin only or approved) */}
                    {(art.status === 'APPROVED' || art.status === 'PENDING_REVIEW') && user.role === 'ADMIN' && (
                      <button
                        onClick={() => handleStatusChange(art.id, 'PUBLISHED')}
                        className="px-2.5 py-1 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-[10px] font-bold"
                      >
                        Publish
                      </button>
                    )}

                    {/* Delete (Admin only) */}
                    {user.role === 'ADMIN' && (
                      <button
                        onClick={() => handleDelete(art.id)}
                        className="p-1.5 rounded bg-red-950/60 text-red-400 hover:text-red-200 border border-red-500/30"
                        title="Delete Art Form"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
