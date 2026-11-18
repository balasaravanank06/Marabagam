import React, { useEffect, useState } from 'react';
import { CommunityPost } from '../types/community';
import { communityService } from '../services/communityService';
import { PostCard } from '../components/PostCard';
import { ReportModal } from '../components/ReportModal';
import { MessageSquare, Plus, Filter } from 'lucide-react';


export const CommunityPage: React.FC = () => {
  const [posts, setPosts] = useState<CommunityPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [reportingPost, setReportingPost] = useState<CommunityPost | null>(null);

  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('DISCUSSION');
  const [content, setContent] = useState('');
  const [createError, setCreateError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);
  const [isTamil, setIsTamil] = useState(false);

  useEffect(() => {
    fetchPosts();
  }, [selectedCategory]);

  const fetchPosts = async () => {
    setLoading(true);
    try {
      const res = await communityService.getPublicPosts(selectedCategory);
      setPosts(res.content);
    } catch (err) {
      console.error('Failed to fetch community posts', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreatePost = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreating(true);
    setCreateError(null);

    try {
      await communityService.createPost({ title, category, content });
      setTitle('');
      setContent('');
      setShowCreateModal(false);
      fetchPosts();
    } catch (err: any) {
      setCreateError(err.response?.data?.message || 'Failed to create post. Authentication required.');
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-6 md:p-10 max-w-7xl mx-auto space-y-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-800 pb-6">
        <div>
          <div className="flex items-center gap-2 text-amber-400 text-sm font-semibold mb-1">
            <MessageSquare className="w-4 h-4" />
            <span>{isTamil ? 'சமூகக் கலந்துரையாடல்கள்' : 'Moderated Cultural Discussions'}</span>
          </div>
          <h1 className="text-3xl font-black tracking-tight text-slate-100">
            {isTamil ? 'சமூக மன்றம்' : 'Community Forum'}
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            {isTamil ? 'பாரம்பரியக் கலைக் கற்றல் அனுபவங்களைப் பகிர்ந்து கொள்ளுங்கள்.' : 'Share questions, learning journeys, and cultural event feedback with fellow practitioners.'}
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setIsTamil(!isTamil)}
            className="text-xs font-semibold px-3 py-1.5 rounded-lg border border-slate-700 text-slate-300 hover:bg-slate-800 transition-colors"
          >
            {isTamil ? 'English' : 'தமிழ்'}
          </button>

          <button
            onClick={() => setShowCreateModal(true)}
            className="text-xs font-bold px-4 py-2 rounded-lg bg-amber-500 hover:bg-amber-400 text-slate-950 transition-colors flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" />
            {isTamil ? 'புதிய பதிவு' : 'New Discussion'}
          </button>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <Filter className="w-4 h-4 text-slate-400" />
        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          className="bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
        >
          <option value="">All Categories</option>
          <option value="DISCUSSION">General Discussion</option>
          <option value="QUESTION">Learning Question</option>
          <option value="LEARNING_JOURNEY">Learning Journey</option>
          <option value="EVENT_DISCUSSION">Event Discussion</option>
          <option value="ART_FORM_DISCUSSION">Art Form Discussion</option>
        </select>
      </div>

      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-32 bg-slate-900/50 rounded-xl animate-pulse border border-slate-800" />
          ))}
        </div>
      ) : posts.length === 0 ? (
        <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-12 text-center text-slate-400 space-y-2">
          <MessageSquare className="w-8 h-8 text-slate-600 mx-auto mb-2" />
          <h3 className="text-base font-semibold">No Community Discussions Yet</h3>
          <p className="text-xs text-slate-500">Be the first to start a conversation.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {posts.map((post) => (
            <PostCard
              key={post.id}
              post={post}
              isTamil={isTamil}
              onReport={(p) => setReportingPost(p)}
            />
          ))}
        </div>
      )}

      {/* Create Post Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl w-full max-w-lg p-6 relative shadow-xl">
            <h2 className="text-lg font-bold text-slate-100 mb-4">Start a New Community Post</h2>

            <form onSubmit={handleCreatePost} className="space-y-4">
              {createError && (
                <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs">
                  {createError}
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Category</label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                >
                  <option value="DISCUSSION">General Discussion</option>
                  <option value="QUESTION">Learning Question</option>
                  <option value="LEARNING_JOURNEY">Learning Journey</option>
                  <option value="EVENT_DISCUSSION">Event Discussion</option>
                  <option value="ART_FORM_DISCUSSION">Art Form Discussion</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Title</label>
                <input
                  type="text"
                  required
                  maxLength={200}
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Discussion title..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Content (Plain text only, max 2000 chars)</label>
                <textarea
                  required
                  rows={5}
                  maxLength={2000}
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="Share your thoughts, question, or learning progress..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-3 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-400 hover:text-slate-200"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creating}
                  className="px-4 py-2 text-xs font-semibold rounded-lg bg-amber-500 hover:bg-amber-400 text-slate-950 transition-colors disabled:opacity-50"
                >
                  {creating ? 'Publishing...' : 'Publish Post'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Report Modal */}
      {reportingPost && (
        <ReportModal
          isOpen={true}
          onClose={() => setReportingPost(null)}
          targetType="POST"
          targetId={reportingPost.id}
          isTamil={isTamil}
        />
      )}
    </div>
  );
};
