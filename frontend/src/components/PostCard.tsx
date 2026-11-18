import React from 'react';
import { Link } from 'react-router-dom';
import { CommunityPost } from '../types/community';
import { MessageSquare, Flag, User, Clock } from 'lucide-react';

interface PostCardProps {
  post: CommunityPost;
  isTamil?: boolean;
  onReport?: (post: CommunityPost) => void;
}

export const PostCard: React.FC<PostCardProps> = ({ post, isTamil = false, onReport }) => {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 hover:border-slate-700 transition-all shadow-md">
      <div className="flex items-center justify-between gap-3 mb-3">
        <div className="flex items-center gap-2.5">
          {post.authorAvatarUrl ? (
            <img src={post.authorAvatarUrl} alt={post.authorDisplayName} className="w-8 h-8 rounded-full object-cover border border-slate-700" />
          ) : (
            <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-300 font-bold text-xs">
              <User className="w-4 h-4" />
            </div>
          )}
          <div>
            <p className="text-xs font-semibold text-slate-200">{post.authorDisplayName}</p>
            <span className="text-[10px] text-slate-400 font-mono">{post.authorRole}</span>
          </div>
        </div>

        <span className="text-xs font-medium px-2.5 py-0.5 rounded-full bg-slate-800 text-amber-400 border border-slate-700">
          {post.category}
        </span>
      </div>

      <Link to={`/community/post/${post.id}`}>
        <h3 className="text-base font-bold text-slate-100 hover:text-amber-400 transition-colors line-clamp-1 mb-2">
          {post.title}
        </h3>
      </Link>

      <p className="text-sm text-slate-300 line-clamp-3 mb-4 whitespace-pre-wrap font-sans">
        {post.content}
      </p>

      <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
        <div className="flex items-center gap-4">
          <Link to={`/community/post/${post.id}`} className="flex items-center gap-1.5 hover:text-amber-400 transition-colors">
            <MessageSquare className="w-4 h-4 text-sky-400" />
            <span>{post.commentCount} {isTamil ? 'பதில்கள்' : 'Replies'}</span>
          </Link>

          <div className="flex items-center gap-1">
            <Clock className="w-3.5 h-3.5" />
            <span>{new Date(post.createdAt).toLocaleDateString()}</span>
          </div>
        </div>

        {onReport && (
          <button
            onClick={() => onReport(post)}
            className="flex items-center gap-1 text-slate-400 hover:text-rose-400 transition-colors p-1"
            title={isTamil ? 'புகார் செய்' : 'Report Post'}
          >
            <Flag className="w-3.5 h-3.5" />
            <span className="text-[11px]">{isTamil ? 'புகார்' : 'Report'}</span>
          </button>
        )}
      </div>
    </div>
  );
};
