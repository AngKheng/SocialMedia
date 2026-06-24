import { useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import Modal from "./Modal";
import ConfirmDialog from "./ConfirmDialog";

export default function CommentItem({
  comment,
  postId,
  onReplyAdded,
  onCommentDeleted,
  onCommentUpdated,
  onReloadRequest,
}) {
  const { user: me } = useAuth();
  const isOwner = me?.id === comment.user.id;

  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyText, setReplyText] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(comment.likeCount);
  const [currentContent, setCurrentContent] = useState(comment.content);

  const [editOpen, setEditOpen] = useState(false);
  const [editContent, setEditContent] = useState(comment.content);
  const [editSaving, setEditSaving] = useState(false);
  const [editError, setEditError] = useState("");

  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  // Mở form reply, tuỳ chọn điền sẵn text (ví dụ "@groq ")
  function handleReplyToReply(preText = "") {
    setReplyText(preText);
    setShowReplyForm(true);
  }

  async function handleReplySubmit(e) {
    e.preventDefault();
    if (!replyText.trim()) return;

    const hasGroq = replyText.toLowerCase().includes("@groq");
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

      if (hasGroq && onReloadRequest) {
        setTimeout(() => onReloadRequest(), 3000);
      }
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

  function openEdit() {
    setEditContent(currentContent);
    setEditError("");
    setEditOpen(true);
  }

  async function saveEdit(e) {
    e.preventDefault();
    if (!editContent.trim()) {
      setEditError("Nội dung không được để trống");
      return;
    }
    setEditSaving(true);
    try {
      const res = await api.put(`/comments/${comment.id}`, {
        content: editContent,
      });
      setCurrentContent(res.data.content);
      setEditOpen(false);
      if (onCommentUpdated) onCommentUpdated(comment.id, res.data);
    } catch (err) {
      setEditError(err.response?.data?.message || "Cập nhật thất bại");
    } finally {
      setEditSaving(false);
    }
  }

  async function confirmDelete() {
    setDeleting(true);
    try {
      await api.delete(`/comments/${comment.id}`);
      setShowDeleteConfirm(false);
      if (onCommentDeleted) onCommentDeleted(comment.id);
    } catch (err) {
      console.error("Xóa comment thất bại:", err);
      alert(err.response?.data?.message || "Xóa comment thất bại");
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="border-b border-gray-100 py-3 last:border-0">
      <CommentBubble
        comment={{ ...comment, content: currentContent }}
        isLiked={isLiked}
        likeCount={likeCount}
        isOwner={isOwner}
        onToggleLike={toggleLike}
        onReplyClick={() => setShowReplyForm((v) => !v)}
        onEditClick={openEdit}
        onDeleteClick={() => setShowDeleteConfirm(true)}
      />

      {/* Replies — lồng 1 cấp */}
      {comment.replies?.length > 0 && (
        <div className="mt-2 space-y-2 pl-8">
          {comment.replies.map((reply) => (
            <ReplyBubble
              key={reply.id}
              comment={reply}
              onReplyClick={() =>
                handleReplyToReply(reply.isAiGenerated ? "@groq " : "")
              }
            />
          ))}
        </div>
      )}

      {/* Form reply */}
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

      {/* Edit Modal */}
      <Modal isOpen={editOpen} onClose={() => setEditOpen(false)} title="Sửa bình luận">
        <form onSubmit={saveEdit}>
          {editError && (
            <div className="mb-2 rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">
              {editError}
            </div>
          )}
          <textarea
            value={editContent}
            onChange={(e) => setEditContent(e.target.value)}
            rows={3}
            className="w-full resize-none rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-blue-500"
          />
          <div className="mt-3 flex justify-end gap-2">
            <button
              type="button"
              onClick={() => setEditOpen(false)}
              className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={editSaving}
              className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
            >
              {editSaving ? "Đang lưu..." : "Lưu"}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirm */}
      <ConfirmDialog
        isOpen={showDeleteConfirm}
        title="Xóa bình luận?"
        message="Bình luận sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác."
        confirmText={deleting ? "Đang xóa..." : "Xóa"}
        onConfirm={confirmDelete}
        onClose={() => !deleting && setShowDeleteConfirm(false)}
      />
    </div>
  );
}

// ─── CommentBubble ────────────────────────────────────────────────────────────

function CommentBubble({
  comment,
  isLiked,
  likeCount,
  isOwner,
  onToggleLike,
  onReplyClick,
  onEditClick,
  onDeleteClick,
}) {
  const isAi = comment.isAiGenerated;
  const name = comment.user.displayName || comment.user.username;

  return (
    <div className="flex gap-2">
      <div
        className={`flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full text-xs font-medium ${
          isAi ? "bg-purple-100 text-purple-700" : "bg-blue-100 text-blue-700"
        }`}
      >
        {isAi ? "🤖" : name.slice(0, 2).toUpperCase()}
      </div>

      <div className="flex-1">
        <div
          className={`inline-block rounded-2xl px-3 py-2 text-sm ${
            isAi ? "bg-purple-50 text-purple-900" : "bg-gray-100 text-gray-800"
          }`}
        >
          <p className="font-medium">
            {name}
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
          {isOwner && (
            <>
              <button onClick={onEditClick} className="hover:text-blue-600">
                Sửa
              </button>
              <button onClick={onDeleteClick} className="hover:text-red-600">
                Xóa
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── ReplyBubble ──────────────────────────────────────────────────────────────

function ReplyBubble({ comment, onReplyClick }) {
  const isAi = comment.isAiGenerated;
  const name = comment.user.displayName || comment.user.username;

  return (
    <div className="flex gap-2">
      <div
        className={`flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full text-[10px] font-medium ${
          isAi ? "bg-purple-100 text-purple-700" : "bg-blue-100 text-blue-700"
        }`}
      >
        {isAi ? "🤖" : name.slice(0, 2).toUpperCase()}
      </div>
      <div className="flex-1">
        <div
          className={`inline-block rounded-2xl px-3 py-1.5 text-xs ${
            isAi ? "bg-purple-50 text-purple-900" : "bg-gray-100 text-gray-800"
          }`}
        >
          <p className="font-medium">
            {name}
            {isAi && <span className="ml-1 text-purple-500">AI</span>}
          </p>
          <p className="whitespace-pre-wrap">{comment.content}</p>
        </div>

        <div className="mt-1 pl-2">
          <button
            onClick={onReplyClick}
            className="text-xs text-gray-500 hover:text-blue-600"
          >
            Phản hồi{isAi && " @groq"}
          </button>
        </div>
      </div>
    </div>
  );
}
