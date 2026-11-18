import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { CommunityPost } from '../types/community';

import { communityService } from '../services/communityService';
import { CommentThread } from '../components/CommentThread';
import { ReportModal } from '../components/ReportModal';
import { ArrowLeft, MessageSquare, Send, User, Flag } from 'lucide-react';

export const PostDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [post, setPost] = useState<CommunityPost | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [commentContent, setCommentContent] = useState('');
  const [replyParentId, setReplyParentId] = useState<string | undefined>(undefined);
  const [submitting, setSubmitting] = useState(false);
  const [reportingTarget, setReportingTarget] = useState<{ type: 'POST' | 'COMMENT'; id: string } | null>(null);

  useEffect(() => {
    if (id) fetchPost();
  }, [id]);

  const fetchPost = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await communityService.getPostDetail(id!);
      setPost(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Post not found');
    } finally {
      setLoading(false);
    }
  };

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentContent.trim() || !post) return;
    setSubmitting(true);

    try {
      await communityService.addComment(post.id, {
        parentCommentId: replyParentId,
        content: commentContent
      });
      setCommentContent('');
      setReplyParentId(undefined);
      fetchPost();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to submit comment. Authentication required.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteComment = async (commentId: string) => {
    if (!window.confirm('Delete this comment?')) return;
    try {
      await communityService.deleteComment(commentId);
      fetchPost();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete comment');
    }
  };

  if (loading) return <div className="p-10 text-center text-slate-400">Loading post...</div>;
  if (error || !post) return <div className="p-10 text-center text-rose-400">{error || 'Post not found'}</div>;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-6 md:p-10 max-w-4xl mx-auto space-y-8">
      <Link to="/community" className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-amber-400 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Back to Community Forum
      </Link>

      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 md:p-8 space-y-6 shadow-xl">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            {post.authorAvatarUrl ? (
              <img src={post.authorAvatarUrl} alt={post.authorDisplayName} className="w-9 h-9 rounded-full object-cover border border-slate-700" />
            ) : (
              <div className="w-9 h-9 rounded-full bg-slate-800 flex items-center justify-center text-slate-300">
                <User className="w-5 h-5" />
              </div>
            )}
            <div>
              <p className="text-sm font-bold text-slate-100">{post.authorDisplayName}</p>
              <span className="text-xs text-slate-400 font-mono">{post.authorRole}</span>
            </div>
          </div>

          <span className="text-xs font-semibold px-3 py-1 rounded-full bg-slate-800 text-amber-400 border border-slate-700">
            {post.category}
          </span>
        </div>

        <div>
          <h1 className="text-2xl font-black text-slate-100 mb-3">{post.title}</h1>
          <p className="text-sm text-slate-300 whitespace-pre-wrap leading-relaxed font-sans">{post.content}</p>
        </div>

        <div className="pt-4 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400">
          <span>Published on {new Date(post.createdAt).toLocaleString()}</span>
          <button
            onClick={() => setReportingTarget({ type: 'POST', id: post.id })}
            className="flex items-center gap-1 hover:text-rose-400 transition-colors"
          >
            <Flag className="w-3.5 h-3.5" />
            <span>Report Post</span>
          </button>
        </div>
      </div>

      {/* Comment Creation Box */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-3">
        <h3 className="text-sm font-bold text-slate-200">
          {replyParentId ? 'Replying to comment' : 'Join the discussion'}
        </h3>

        {replyParentId && (
          <div className="text-xs text-amber-400 flex items-center justify-between bg-amber-500/10 px-3 py-1.5 rounded border border-amber-500/20">
            <span>Replying to top comment</span>
            <button onClick={() => setReplyParentId(undefined)} className="hover:underline">Cancel Reply</button>
          </div>
        )}

        <form onSubmit={handleAddComment} className="space-y-3">
          <textarea
            required
            rows={3}
            maxLength={500}
            value={commentContent}
            onChange={(e) => setCommentContent(e.target.value)}
            placeholder="Write a comment (plain text only, max 500 chars)..."
            className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
          />

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-xs font-bold rounded-lg bg-amber-500 hover:bg-amber-400 text-slate-950 transition-colors flex items-center gap-1.5 disabled:opacity-50"
            >
              <Send className="w-3.5 h-3.5" />
              {submitting ? 'Submitting...' : 'Post Comment'}
            </button>
          </div>
        </form>
      </div>

      {/* Comment Thread List */}
      <div className="space-y-4">
        <h3 className="text-base font-bold text-slate-200 flex items-center gap-2">
          <MessageSquare className="w-4 h-4 text-sky-400" />
          <span>Comments ({post.comments ? post.comments.length : 0})</span>
        </h3>

        {post.comments && post.comments.length > 0 ? (
          <CommentThread
            comments={post.comments}
            onReply={(parentId) => setReplyParentId(parentId)}
            onDelete={(commentId) => handleDeleteComment(commentId)}
            onReport={(c) => setReportingTarget({ type: 'COMMENT', id: c.id })}
          />
        ) : (
          <div className="p-8 text-center text-xs text-slate-500 bg-slate-900/40 rounded-xl border border-slate-800">
            No comments yet. Be the first to comment!
          </div>
        )}
      </div>

      {/* Report Modal */}
      {reportingTarget && (
        <ReportModal
          isOpen={true}
          onClose={() => setReportingTarget(null)}
          targetType={reportingTarget.type}
          targetId={reportingTarget.id}
        />
      )}
    </div>
  );
};
