import React from 'react';

import { CommunityComment } from '../types/community';
import { User, CornerDownRight, Flag, Trash2 } from 'lucide-react';

interface CommentThreadProps {
  comments: CommunityComment[];
  isTamil?: boolean;
  onReply?: (parentCommentId: string) => void;
  onDelete?: (commentId: string) => void;
  onReport?: (comment: CommunityComment) => void;
  currentUserId?: string;
}

export const CommentThread: React.FC<CommentThreadProps> = ({
  comments,
  isTamil = false,
  onReply,
  onDelete,
  onReport
}) => {
  return (
    <div className="space-y-4">
      {comments.map((comment) => (
        <div key={comment.id} className="bg-slate-900/90 border border-slate-800 rounded-xl p-4">
          <div className="flex items-center justify-between gap-2 mb-2">
            <div className="flex items-center gap-2">
              {comment.authorAvatarUrl ? (
                <img src={comment.authorAvatarUrl} alt={comment.authorDisplayName} className="w-6 h-6 rounded-full object-cover" />
              ) : (
                <div className="w-6 h-6 rounded-full bg-slate-800 flex items-center justify-center text-slate-400 text-xs">
                  <User className="w-3.5 h-3.5" />
                </div>
              )}
              <span className="text-xs font-semibold text-slate-200">{comment.authorDisplayName}</span>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 font-mono">{comment.authorRole}</span>
            </div>

            <span className="text-[11px] text-slate-500">{new Date(comment.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
          </div>

          <p className="text-sm text-slate-300 whitespace-pre-wrap mb-3 pl-8">
            {comment.content}
          </p>

          <div className="flex items-center gap-4 pl-8 text-xs text-slate-400">
            {onReply && (
              <button
                onClick={() => onReply(comment.id)}
                className="flex items-center gap-1 hover:text-amber-400 transition-colors"
              >
                <CornerDownRight className="w-3.5 h-3.5" />
                <span>{isTamil ? 'பதிலளி' : 'Reply'}</span>
              </button>
            )}

            {onReport && (
              <button
                onClick={() => onReport(comment)}
                className="flex items-center gap-1 hover:text-rose-400 transition-colors"
              >
                <Flag className="w-3.5 h-3.5" />
                <span>{isTamil ? 'புகார்' : 'Report'}</span>
              </button>
            )}

            {onDelete && (
              <button
                onClick={() => onDelete(comment.id)}
                className="flex items-center gap-1 hover:text-rose-400 transition-colors text-slate-500 ml-auto"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          {/* Nested Replies (Single Depth) */}
          {comment.replies && comment.replies.length > 0 && (
            <div className="mt-3 pl-6 space-y-3 border-l-2 border-slate-800">
              {comment.replies.map((reply) => (
                <div key={reply.id} className="bg-slate-950/60 rounded-lg p-3 border border-slate-800/60">
                  <div className="flex items-center justify-between gap-2 mb-1.5">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-semibold text-slate-300">{reply.authorDisplayName}</span>
                      <span className="text-[10px] text-slate-500 font-mono">{reply.authorRole}</span>
                    </div>
                    <span className="text-[10px] text-slate-500">{new Date(reply.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                  </div>
                  <p className="text-xs text-slate-300 whitespace-pre-wrap">{reply.content}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      ))}
    </div>
  );
};
