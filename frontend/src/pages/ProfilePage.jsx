import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import Navbar from "../components/Navbar";
import PostCard from "../components/PostCard";
import FollowButton from "../components/FollowButton";

/**
 * Trang profile:
 * - Nếu id = "me" (hoặc không có) → hiển thị + cho phép sửa profile của chính mình
 * - Nếu id là số → hiển thị profile người khác + FollowButton
 *
 * Số follower/following hiển thị dạng text (chưa link đến list — làm ở 9F).
 */
export default function ProfilePage() {
 const { id: paramId } = useParams();
 const { user: me, setUser } = useAuth();
 const navigate = useNavigate();

 // Xác định userId cần xem
 const targetId = !paramId || paramId === "me" ? me?.id : Number(paramId);
 const isMe = !paramId || paramId === "me";

 const [profile, setProfile] = useState(null);
 const [posts, setPosts] = useState([]);
 const [loading, setLoading] = useState(true);
 const [error, setError] = useState("");
 const [isEditing, setIsEditing] = useState(false);
 const [editForm, setEditForm] = useState({ displayName: "", bio: "", avatarUrl: "" });
 const [saving, setSaving] = useState(false);
 const [saveError, setSaveError] = useState("");

 useEffect(() => {
 if (!targetId) return;
 loadProfile();
 loadPosts();
 // eslint-disable-next-line react-hooks/exhaustive-deps
 }, [targetId]);

 async function loadProfile() {
 setLoading(true);
 setError("");
 try {
 const res = await api.get(`/users/${targetId}`);
 setProfile(res.data);
 if (isMe) {
 setEditForm({
 displayName: res.data.displayName || "",
 bio: res.data.bio || "",
 avatarUrl: res.data.avatarUrl || "",
 });
 }
 } catch (err) {
 setError("Không tìm thấy user này");
 } finally {
 setLoading(false);
 }
 }

 async function loadPosts() {
 try {
 const res = await api.get(`/posts/user/${targetId}`, { params: { page: 0, size: 20 } });
 setPosts(res.data.content || []);
 } catch (err) {
 console.error("Load posts thất bại:", err);
 }
 }

 function handlePostDeleted(postId) {
 setPosts((prev) => prev.filter((p) => p.id !== postId));
 }

 async function handleSaveProfile(e) {
 e.preventDefault();
 setSaving(true);
 setSaveError("");
 try {
 const res = await api.put("/users/me", editForm);
 // Cập nhật localStorage user (giữ các field khác)
 const updatedUser = { ...me, ...res.data };
 localStorage.setItem("user", JSON.stringify(updatedUser));
 setUser(updatedUser);
 setIsEditing(false);
 // Reload để hiển thị data mới từ server (kèm follower/following count)
 await loadProfile();
 } catch (err) {
 setSaveError(err.response?.data?.message || "Cập nhật thất bại");
 } finally {
 setSaving(false);
 }
 }

 if (loading) {
 return (
 <div className="min-h-screen bg-gray-50">
 <Navbar />
 <p className="py-10 text-center text-sm text-gray-400">Đang tải...</p>
 </div>
 );
 }

 if (error || !profile) {
 return (
 <div className="min-h-screen bg-gray-50">
 <Navbar />
 <p className="py-10 text-center text-sm text-red-500">{error}</p>
 <p className="text-center">
 <Link to="/feed" className="text-sm text-blue-600 hover:underline">
 ← Quay lại Feed
 </Link>
 </p>
 </div>
 );
 }

 const initials = (profile.displayName || profile.username).slice(0, 2).toUpperCase();

 return (
 <div className="min-h-screen bg-gray-50">
 <Navbar />

 <div className="mx-auto max-w-2xl px-4 py-6">

 {/* Header card */}
 <div className="mb-4 rounded-xl border border-gray-200 bg-white p-6">
 <div className="flex items-start gap-4">
 {/* Avatar */}
 {profile.avatarUrl ? (
 <img
 src={profile.avatarUrl}
 alt=""
 className="h-20 w-20 flex-shrink-0 rounded-full object-cover"
 />
 ) : (
 <div className="flex h-20 w-20 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 text-2xl font-medium text-blue-700">
 {initials}
 </div>
 )}

 <div className="flex-1">
 <p className="text-xl font-bold text-gray-900">
 {profile.displayName || profile.username}
 </p>
 <p className="text-sm text-gray-500">@{profile.username}</p>

 {profile.bio && (
 <p className="mt-2 text-sm text-gray-700">{profile.bio}</p>
 )}

 <div className="mt-3 flex gap-4 text-sm text-gray-600">
 <span>
 <strong className="text-gray-900">{profile.followerCount}</strong> người theo dõi
 </span>
 <span>
 <strong className="text-gray-900">{profile.followingCount}</strong> đang theo dõi
 </span>
 </div>
 </div>

 {/* Nút Follow hoặc Sửa */}
 <div className="flex-shrink-0">
 {isMe ? (
 <button
 onClick={() => setIsEditing(true)}
 className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
 >
 Sửa profile
 </button>
 ) : (
 <FollowButton userId={profile.id} initialState={profile.isFollowing} />
 )}
 </div>
 </div>
 </div>

 {/* Form sửa profile (chỉ khi isMe và isEditing) */}
 {isMe && isEditing && (
 <form
 onSubmit={handleSaveProfile}
 className="mb-4 rounded-xl border border-gray-200 bg-white p-4"
 >
 <h2 className="mb-3 text-sm font-semibold text-gray-900">Sửa profile</h2>

 {saveError && (
 <div className="mb-2 rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">
 {saveError}
 </div>
 )}

 <div className="mb-3">
 <label className="mb-1 block text-xs font-medium text-gray-700">
 Tên hiển thị
 </label>
 <input
 type="text"
 value={editForm.displayName}
 onChange={(e) => setEditForm({ ...editForm, displayName: e.target.value })}
 maxLength={100}
 className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-blue-500"
 />
 </div>

 <div className="mb-3">
 <label className="mb-1 block text-xs font-medium text-gray-700">
 Bio
 </label>
 <textarea
 value={editForm.bio}
 onChange={(e) => setEditForm({ ...editForm, bio: e.target.value })}
 maxLength={255}
 rows={2}
 className="w-full resize-none rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-blue-500"
 />
 </div>

 <div className="mb-3">
 <label className="mb-1 block text-xs font-medium text-gray-700">
 Avatar URL
 </label>
 <input
 type="text"
 value={editForm.avatarUrl}
 onChange={(e) => setEditForm({ ...editForm, avatarUrl: e.target.value })}
 maxLength={500}
 placeholder="https://..."
 className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-blue-500"
 />
 </div>

 <div className="flex gap-2">
 <button
 type="submit"
 disabled={saving}
 className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
 >
 {saving ? "Đang lưu..." : "Lưu"}
 </button>
 <button
 type="button"
 onClick={() => {
 setIsEditing(false);
 setSaveError("");
 }}
 className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
 >
 Hủy
 </button>
 </div>
 </form>
 )}

 {/* Danh sách post */}
 <h2 className="mb-3 px-1 text-sm font-semibold text-gray-700">
 Bài viết ({posts.length})
 </h2>

 {posts.length === 0 ? (
 <p className="rounded-xl border border-gray-200 bg-white py-8 text-center text-sm text-gray-400">
 {isMe ? "Bạn chưa đăng bài nào." : "User này chưa đăng bài nào."}
 </p>
 ) : (
 <div className="space-y-3">
 {posts.map((post) => (
 <PostCard
 key={post.id}
 post={post}
 onDelete={handlePostDeleted}
 />
 ))}
 </div>
 )}
 </div>
 </div>
 );
}
