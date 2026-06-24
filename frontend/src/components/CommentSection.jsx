import { useState } from "react";
import api from "../api/axios";
import CommentItem from "./CommentItem";

/**
 * Toàn bộ phần comment của 1 post:
 * - form viết comment gốc
 * - danh sách comment gốc + replies
 *
 * Gõ "@groq <câu hỏi>" sẽ tự động trigger AI trả lời ở backend (async),
 * nên sau khi gửi, đợi vài giây rồi load lại để thấy reply từ bot.
 */
export default function CommentSection({ postId, comments, setComments }) {
 const [newComment, setNewComment] = useState("");
 const [submitting, setSubmitting] = useState(false);
 const isGroqMention = newComment.toLowerCase().includes("@groq");

 async function handleSubmit(e) {
 e.preventDefault();
 if (!newComment.trim()) return;

 setSubmitting(true);
 try {
 const res = await api.post("/comments", {
 postId,
 content: newComment,
 });
 // Comment mới chưa có replies → thêm vào đầu danh sách
 setComments((prev) => [{ ...res.data, replies: [] }, ...prev]);
 setNewComment("");

 // Nếu có @groq, load lại sau 3s để lấy reply của bot
 if (isGroqMention) {
 setTimeout(() => reloadComments(), 3000);
 }
 } catch (err) {
 console.error("Comment thất bại:", err);
 } finally {
 setSubmitting(false);
 }
 }

 async function reloadComments() {
 try {
 const res = await api.get(`/comments/post/${postId}`);
 setComments(res.data);
 } catch (err) {
 console.error("Reload comments thất bại:", err);
 }
 }

 function handleReplyAdded(parentId, newReply) {
 setComments((prev) =>
 prev.map((c) =>
 c.id === parentId
 ? { ...c, replies: [...(c.replies || []), newReply] }
 : c
 )
 );
 }

 function handleCommentDeleted(commentId) {
 setComments((prev) => prev.filter((c) => c.id !== commentId));
 }

 function handleCommentUpdated(commentId, updated) {
 setComments((prev) =>
 prev.map((c) => (c.id === commentId ? { ...c, content: updated.content } : c))
 );
 }

 return (
 <div className="mt-4">
 <form onSubmit={handleSubmit} className="mb-4">
 <div className="flex gap-2">
 <input
 type="text"
 value={newComment}
 onChange={(e) => setNewComment(e.target.value)}
 placeholder="Viết bình luận... (gõ @groq để hỏi AI)"
 className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-blue-500"
 />
 <button
 type="submit"
 disabled={submitting || !newComment.trim()}
 className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
 >
 Gửi
 </button>
 </div>
 {isGroqMention && (
 <p className="mt-1 text-xs text-purple-500">
 🤖 Groq AI sẽ tự động trả lời sau khi bạn gửi
 </p>
 )}
 </form>

 {comments.length === 0 ? (
 <p className="text-center text-sm text-gray-400">
 Chưa có bình luận nào. Hãy là người đầu tiên!
 </p>
 ) : (
 <div>
 {comments.map((comment) => (
 <CommentItem
 key={comment.id}
 comment={comment}
 postId={postId}
 onReplyAdded={handleReplyAdded}
 onCommentDeleted={handleCommentDeleted}
 onCommentUpdated={handleCommentUpdated}
onReloadRequest={reloadComments}
 />
 ))}
 </div>
 )}
 </div>
 );
}