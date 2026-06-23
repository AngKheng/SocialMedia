import { Link, useNavigate } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import api from "../api/axios";
import FollowButton from "./FollowButton";
import { useAuth } from "../context/AuthContext";
import Modal from "./Modal";
import ConfirmDialog from "./ConfirmDialog";

/**
 * Hiển thị 1 bài viết trong Feed/Profile.
 *
 * Props:
 * - post (object)
 * - onDelete (fn?) — callback khi xóa thành công (để parent xóa khỏi list)
 *
 * Avatar + tên → click mở /profile/:id (Phase 9E).
 * Nếu là chủ bài (me.id === post.user.id) → hiện nút ⋯ với menu Sửa/Xóa (Phase 9G).
 */
export default function PostCard({ post, onDelete }) {
 const navigate = useNavigate();
 const { user: me } = useAuth();
 const isOwner = me?.id === post.user.id;

 const [isLiked, setIsLiked] = useState(post.isLiked || false);
 const [likeCount, setLikeCount] = useState(post.likeCount);
 const [loading, setLoading] = useState(false);

 // ====== Menu ⋯ state ======
 const [menuOpen, setMenuOpen] = useState(false);
 const menuRef = useRef(null);

 // ====== Edit modal state ======
 const [editOpen, setEditOpen] = useState(false);
 const [editContent, setEditContent] = useState(post.content);
 const [editMediaUrls, setEditMediaUrls] = useState(post.mediaUrls || []);
 const [editFiles, setEditFiles] = useState([]);
 const [editPreviews, setEditPreviews] = useState([]);
 const [editSaving, setEditSaving] = useState(false);
 const [editError, setEditError] = useState("");

 // ====== Delete confirm state ======
 const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
 const [deleting, setDeleting] = useState(false);

 // ====== Live state cho post hiển thị (cập nhật sau khi save edit) ======
 const [currentContent, setCurrentContent] = useState(post.content);
 const [currentMediaUrls, setCurrentMediaUrls] = useState(post.mediaUrls || []);

 // Click ngoài dropdown menu thì đóng
 useEffect(() => {
 function handleClickOutside(e) {
 if (menuRef.current && !menuRef.current.contains(e.target)) {
 setMenuOpen(false);
 }
 }
 if (menuOpen) {
 document.addEventListener("mousedown", handleClickOutside);
 return () => document.removeEventListener("mousedown", handleClickOutside);
 }
 }, [menuOpen]);

 // Cleanup object URL previews khi unmount / đóng edit
 useEffect(() => {
 return () => {
 editPreviews.forEach((src) => URL.revokeObjectURL(src));
 };
 // eslint-disable-next-line react-hooks/exhaustive-deps
 }, [editPreviews]);

 async function toggleLike(e) {
 e.preventDefault();
 if (loading) return;
 setLoading(true);

 try {
 const res = isLiked
 ? await api.delete(`/posts/${post.id}/like`)
 : await api.post(`/posts/${post.id}/like`);

 setIsLiked(res.data.isLiked);
 setLikeCount(res.data.likeCount);
 } catch (err) {
 console.error("Like/unlike thất bại:", err);
 } finally {
 setLoading(false);
 }
 }

 function openEdit() {
 setMenuOpen(false);
 setEditContent(currentContent);
 setEditMediaUrls(currentMediaUrls);
 setEditFiles([]);
 setEditPreviews([]);
 setEditError("");
 setEditOpen(true);
 }

 function handleEditFileChange(e) {
 const selected = Array.from(e.target.files || []);
 setEditError("");

 const hasVideo = selected.some((f) => f.type.startsWith("video/"));
 const hasImage = selected.some((f) => f.type.startsWith("image/"));

 if (hasVideo && hasImage) {
 setEditError("Không thể chọn ảnh và video cùng lúc");
 return;
 }
 if (hasVideo && selected.length > 1) {
 setEditError("Chỉ được chọn 1 video");
 return;
 }

 setEditFiles(selected);
 setEditPreviews(selected.map((f) => URL.createObjectURL(f)));
 }

 function removeEditMedia(idx) {
 setEditMediaUrls((prev) => prev.filter((_, i) => i !== idx));
 }

 function removeEditNewFile(idx) {
 setEditFiles((prev) => prev.filter((_, i) => i !== idx));
 setEditPreviews((prev) => prev.filter((_, i) => i !== idx));
 }

 async function saveEdit(e) {
 e.preventDefault();
 setEditError("");

 if (!editContent.trim() && editMediaUrls.length === 0 && editFiles.length === 0) {
 setEditError("Bài viết phải có nội dung hoặc media");
 return;
 }

 setEditSaving(true);
 try {
 // Upload file mới (nếu có) — append vào editMediaUrls
 const finalMediaUrls = [...editMediaUrls];
 for (const file of editFiles) {
 const formData = new FormData();
 formData.append("file", file);
 const res = await api.post("/upload", formData, {
 headers: { "Content-Type": "multipart/form-data" },
 });
 finalMediaUrls.push(res.data.url);
 }

 const res = await api.put(`/posts/${post.id}`, {
 content: editContent,
 mediaUrls: finalMediaUrls,
 });

 setCurrentContent(res.data.content);
 setCurrentMediaUrls(res.data.mediaUrls || []);
 setEditOpen(false);
 } catch (err) {
 setEditError(err.response?.data?.message || "Cập nhật thất bại");
 } finally {
 setEditSaving(false);
 }
 }

 async function confirmDelete() {
 setDeleting(true);
 try {
 await api.delete(`/posts/${post.id}`);
 setShowDeleteConfirm(false);
 // Nếu đang ở trang PostDetail (URL chứa /posts/{id}) → quay về /feed
 if (window.location.pathname.startsWith(`/posts/${post.id}`)) {
 navigate("/feed");
 } else if (onDelete) {
 onDelete(post.id);
 }
 } catch (err) {
 console.error("Xóa bài thất bại:", err);
 alert(err.response?.data?.message || "Xóa bài thất bại");
 } finally {
 setDeleting(false);
 }
 }

 const initials = (post.user.displayName || post.user.username)
 .slice(0, 2)
 .toUpperCase();

 return (
 <div className="rounded-xl border border-gray-200 bg-white p-4">
 {/* Header: avatar + tên (link profile) + Follow hoặc nút ⋯ */}
 <div className="mb-2 flex items-center justify-between">
 <Link to={`/profile/${post.user.id}`} className="flex items-center gap-2">
 <div className="flex h-9 w-9 items-center justify-center rounded-full bg-blue-100 text-sm font-medium text-blue-700">
 {initials}
 </div>
 <div>
 <p className="text-sm font-medium text-gray-900">
 {post.user.displayName || post.user.username}
 </p>
 <p className="text-xs text-gray-500">@{post.user.username}</p>
 </div>
 </Link>

 <div className="flex items-center gap-2">
 {!isOwner && (
 <FollowButton
 userId={post.user.id}
 initialState={post.user.isFollowing || false}
 />
 )}

 {/* Menu ⋯ cho chủ bài */}
 {isOwner && (
 <div className="relative" ref={menuRef}>
 <button
 onClick={(e) => {
 e.preventDefault();
 setMenuOpen(!menuOpen);
 }}
 className="flex h-7 w-7 items-center justify-center rounded-full text-gray-500 hover:bg-gray-100"
 aria-label="Tùy chọn"
 >
 ⋯
 </button>

 {menuOpen && (
 <div className="absolute right-0 top-full z-10 mt-1 w-32 overflow-hidden rounded-md border border-gray-200 bg-white shadow-lg">
 <button
 onClick={openEdit}
 className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50"
 >
 Sửa bài
 </button>
 <button
 onClick={() => {
 setMenuOpen(false);
 setShowDeleteConfirm(true);
 }}
 className="block w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-gray-50"
 >
 Xóa bài
 </button>
 </div>
 )}
 </div>
 )}
 </div>
 </div>

 {/* Nội dung bài viết */}
 <Link to={`/posts/${post.id}`} className="block">
 <p className="whitespace-pre-wrap text-sm text-gray-800">
 {currentContent}
 </p>

 {currentMediaUrls?.length > 0 && (
 <div className="mt-2 grid grid-cols-2 gap-1">
 {currentMediaUrls.map((url, i) =>
 url.match(/\.(mp4|mov|avi|webm)$/i) ? (
 <video key={i} src={url} controls className="rounded-lg" />
 ) : (
 <img key={i} src={url} alt="" className="rounded-lg object-cover" />
 )
 )}
 </div>
 )}
 </Link>

 {/* Actions: like + comment count */}
 <div className="mt-3 flex items-center gap-4 border-t border-gray-100 pt-2 text-sm text-gray-500">
 <button
 onClick={toggleLike}
 disabled={loading}
 className={`flex items-center gap-1 ${
 isLiked ? "text-red-500" : "hover:text-red-500"
 }`}
 >
 {isLiked ? "♥" : "♡"} {likeCount}
 </button>

 <Link
 to={`/posts/${post.id}`}
 className="flex items-center gap-1 hover:text-blue-600"
 >
 💬 {post.commentCount} bình luận
 </Link>
 </div>

 {/* ====== Edit Modal ====== */}
 <Modal isOpen={editOpen} onClose={() => setEditOpen(false)} title="Sửa bài viết">
 <form onSubmit={saveEdit}>
 {editError && (
 <div className="mb-2 rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">
 {editError}
 </div>
 )}

 <textarea
 value={editContent}
 onChange={(e) => setEditContent(e.target.value)}
 placeholder="Nội dung bài viết..."
 rows={4}
 className="w-full resize-none rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-blue-500"
 />

 {/* Media hiện tại */}
 {editMediaUrls.length > 0 && (
 <div className="mt-2">
 <p className="mb-1 text-xs text-gray-500">Media hiện tại (bấm X để xóa):</p>
 <div className="grid grid-cols-3 gap-1">
 {editMediaUrls.map((url, i) => (
 <div key={i} className="relative">
 {url.match(/\.(mp4|mov|avi|webm)$/i) ? (
 <video src={url} className="rounded-lg" />
 ) : (
 <img src={url} alt="" className="rounded-lg object-cover" />
 )}
 <button
 type="button"
 onClick={() => removeEditMedia(i)}
 className="absolute right-1 top-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-600 text-xs text-white"
 >
 ×
 </button>
 </div>
 ))}
 </div>
 </div>
 )}

 {/* File mới chọn (chưa upload) */}
 {editPreviews.length > 0 && (
 <div className="mt-2">
 <p className="mb-1 text-xs text-gray-500">Ảnh mới (sẽ upload khi bấm Lưu):</p>
 <div className="grid grid-cols-3 gap-1">
 {editPreviews.map((src, i) => (
 <div key={i} className="relative">
 {editFiles[i].type.startsWith("video/") ? (
 <video src={src} className="rounded-lg" />
 ) : (
 <img src={src} alt="" className="rounded-lg object-cover" />
 )}
 <button
 type="button"
 onClick={() => removeEditNewFile(i)}
 className="absolute right-1 top-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-600 text-xs text-white"
 >
 ×
 </button>
 </div>
 ))}
 </div>
 </div>
 )}

 {/* Nút thêm ảnh */}
 <label className="mt-2 inline-block cursor-pointer text-sm text-blue-600 hover:underline">
 📷 Thêm ảnh/video
 <input
 type="file"
 accept="image/*,video/*"
 multiple
 onChange={handleEditFileChange}
 className="hidden"
 />
 </label>

 <div className="mt-4 flex justify-end gap-2">
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

 {/* ====== Delete Confirm ====== */}
 <ConfirmDialog
 isOpen={showDeleteConfirm}
 title="Xóa bài viết?"
 message="Bài viết sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác."
 confirmText={deleting ? "Đang xóa..." : "Xóa"}
 onConfirm={confirmDelete}
 onClose={() => !deleting && setShowDeleteConfirm(false)}
 />
 </div>
 );
}