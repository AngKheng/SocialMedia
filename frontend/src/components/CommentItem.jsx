import { useState } from "react";
import api from "../api/axios";
import UserAvatar from "./UserAvatar";

export default function CommentItem({ comment, postId, onReplyAdded }) {
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyText, setReplyText] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(comment.likeCount);

  async function handleReplySubmit(e) {
    e.preventDefault();
    if (!replyText.trim()) return;

    setSubmitting(true);
    try {
      const res = await api.post("/comments", {
        postId,
        content: replyText,
        parentCommentId: comment.id,
      });
      onReplyAdded(comment.id, res.data);
      setReplyText("");
      setShowReplyForm(false);
    } catch (err) {
      console.error("Reply thất bại:", err);
    } finally {
      setSubmitting(false);
    }
  }

  async function toggleLike() {
    try {
      const res = isLiked
        ? await api.delete(`/comments/${comment.id}/like`)
        : await api.post(`/comments/${comment.id}/like`);
      setIsLiked(res.data.isLiked);
      setLikeCount(res.data.likeCount);
    } catch (err) {
      console.error("Like comment thất bại:", err);
    }
  }

  return (
    <div className="border-b border-gray-100 py-3 last:border-0">
      <CommentBubble
        comment={comment}
        isLiked={isLiked}
        likeCount={likeCount}
        onToggleLike={toggleLike}
        onReplyClick={() => setShowReplyForm((v) => !v)}
      />

      {comment.replies?.length > 0 && (
        <div className="mt-2 space-y-2 pl-8">
          {comment.replies.map((reply) => (
            <ReplyBubble key={reply.id} comment={reply} />
          ))}
        </div>
      )}

      {showReplyForm && (
        <form onSubmit={handleReplySubmit} className="mt-2 pl-8">
          <div className="flex gap-2">
            <input
              type="text"
              value={replyText}
              onChange={(e) => setReplyText(e.target.value)}
              placeholder="Viết phản hồi..."
              autoFocus
              className="flex-1 rounded-md border border-gray-300 px-3 py-1.5 text-sm outline-none focus:border-blue-500"
            />
            <button
              type="submit"
              disabled={submitting || !replyText.trim()}
              className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
            >
              Gửi
            </button>
          </div>
        </form>
      )}
    </div>
  );
}

function CommentBubble({ comment, isLiked, likeCount, onToggleLike, onReplyClick }) {
  const isAi = comment.isAiGenerated;

  return (
    <div className="flex gap-2">
      {isAi ? (
        <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-purple-100 text-xs font-medium text-purple-700">
          🤖
        </div>
      ) : (
        <UserAvatar user={comment.user} size="md" className="h-8 w-8 text-xs" />
      )}

      <div className="flex-1">
        <div
          className={`inline-block rounded-2xl px-3 py-2 text-sm ${
            isAi ? "bg-purple-50 text-purple-900" : "bg-gray-100 text-gray-800"
          }`}
        >
          <p className="font-medium">
            {comment.user.displayName || comment.user.username}
            {isAi && <span className="ml-1 text-xs text-purple-500">AI</span>}
          </p>
          <p className="whitespace-pre-wrap">{comment.content}</p>
        </div>

        <div className="mt-1 flex gap-3 pl-2 text-xs text-gray-500">
          <button
            onClick={onToggleLike}
            className={isLiked ? "text-red-500" : "hover:text-red-500"}
          >
            {isLiked ? "♥" : "♡"} {likeCount}
          </button>
          <button onClick={onReplyClick} className="hover:text-blue-600">
            Phản hồi
          </button>
        </div>
      </div>
    </div>
  );
}

function ReplyBubble({ comment }) {
  const isAi = comment.isAiGenerated;

  return (
    <div className="flex gap-2">
      {isAi ? (
        <div className="flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full bg-purple-100 text-[10px] font-medium text-purple-700">
          🤖
        </div>
      ) : (
        <UserAvatar user={comment.user} size="sm" />
      )}
      <div
        className={`inline-block rounded-2xl px-3 py-1.5 text-xs ${
          isAi ? "bg-purple-50 text-purple-900" : "bg-gray-100 text-gray-800"
        }`}
      >
        <p className="font-medium">
          {comment.user.displayName || comment.user.username}
          {isAi && <span className="ml-1 text-purple-500">AI</span>}
        </p>
        <p className="whitespace-pre-wrap">{comment.content}</p>
      </div>
    </div>
  );
}
