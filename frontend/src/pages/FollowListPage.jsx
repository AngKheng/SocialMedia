import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";
import FollowButton from "../components/FollowButton";
import { useAuth } from "../context/AuthContext";

/**
 * Trang danh sách Followers / Following của 1 user.
 * Dùng chung cho 2 route:
 * - /users/:id/followers → tab "Followers"
 * - /users/:id/following → tab "Following"
 *
 * Mỗi item: avatar + displayName + @username + FollowButton (ẩn nếu là chính mình).
 */
export default function FollowListPage() {
 const { id, type } = useParams();
 const navigate = useNavigate();
 const { user: me } = useAuth();

 // type ∈ {followers, following} — xác định endpoint + tab active
 const isFollowers = type === "followers";

 const [users, setUsers] = useState([]);
 const [profile, setProfile] = useState(null); // để hiển thị tên user ở header
 const [loading, setLoading] = useState(true);
 const [error, setError] = useState("");

 useEffect(() => {
 loadList();
 loadProfileHeader();
 // eslint-disable-next-line react-hooks/exhaustive-deps
 }, [id, type]);

 async function loadList() {
 setLoading(true);
 setError("");
 try {
 const url = isFollowers
 ? `/follow/${id}/followers`
 : `/follow/${id}/following`;
 const res = await api.get(url);
 setUsers(res.data);
 } catch (err) {
 setError("Không tải được danh sách");
 } finally {
 setLoading(false);
 }
 }

 async function loadProfileHeader() {
 try {
 const res = await api.get(`/users/${id}`);
 setProfile(res.data);
 } catch (err) {
 // Không quan trọng — chỉ để hiển thị tên, fail thì ẩn
 }
 }

 function handleFollowToggle(_userId) {
 // Sau khi follow/unfollow 1 user trong list, có thể update UI ở đây
 // Hiện tại để đơn giản: chỉ reload list
 // (vì backend không trả về isFollowing cho mỗi user trong list)
 }

 return (
 <div className="min-h-screen bg-gray-50">
 <Navbar />

 <div className="mx-auto max-w-xl px-4 py-6">
 {/* Nút quay lại */}
 <button
 onClick={() => navigate(`/profile/${id}`)}
 className="mb-3 text-sm text-blue-600 hover:underline"
 >
 ← Quay lại hồ sơ
 </button>

 {/* Header */}
 <div className="mb-4 rounded-xl border border-gray-200 bg-white p-4">
 <p className="text-xs text-gray-500">{profile?.displayName || profile?.username || "User"}</p>
 <h1 className="text-xl font-bold text-gray-900">
 {isFollowers ? "Người theo dõi" : "Đang theo dõi"}
 <span className="ml-2 text-base font-normal text-gray-500">
 ({users.length})
 </span>
 </h1>
 </div>

 {/* Tab switch */}
 <div className="mb-4 flex border-b border-gray-200">
 <Link
 to={`/users/${id}/followers`}
 className={`flex-1 border-b-2 py-2 text-center text-sm font-medium transition ${
 isFollowers
 ? "border-blue-600 text-blue-600"
 : "border-transparent text-gray-500 hover:text-gray-700"
}`}
 >
 Followers
 </Link>
 <Link
 to={`/users/${id}/following`}
 className={`flex-1 border-b-2 py-2 text-center text-sm font-medium transition ${
 !isFollowers
 ? "border-blue-600 text-blue-600"
 : "border-transparent text-gray-500 hover:text-gray-700"
}`}
 >
 Following
 </Link>
 </div>

 {/* List */}
 {loading ? (
 <p className="py-8 text-center text-sm text-gray-400">Đang tải...</p>
 ) : error ? (
 <p className="py-8 text-center text-sm text-red-500">{error}</p>
 ) : users.length === 0 ? (
 <p className="rounded-xl border border-gray-200 bg-white py-8 text-center text-sm text-gray-400">
 {isFollowers
 ? "Chưa có ai theo dõi user này."
 : "User này chưa theo dõi ai."}
 </p>
 ) : (
 <div className="space-y-2">
 {users.map((u) => {
 const initials = (u.displayName || u.username)
 .slice(0, 2)
 .toUpperCase();
 const isMe = me?.id === u.id;

 return (
 <div
 key={u.id}
 className="flex items-center gap-3 rounded-xl border border-gray-200 bg-white p-3"
 >
 {/* Avatar + tên (link đến profile của user trong list) */}
 <Link
 to={`/profile/${u.id}`}
 className="flex flex-1 items-center gap-3 overflow-hidden"
 >
 {u.avatarUrl ? (
 <img
 src={u.avatarUrl}
 alt=""
 className="h-10 w-10 flex-shrink-0 rounded-full object-cover"
 />
 ) : (
 <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 text-sm font-medium text-blue-700">
 {initials}
 </div>
 )}
 <div className="flex-1 overflow-hidden">
 <p className="truncate text-sm font-medium text-gray-900">
 {u.displayName || u.username}
 </p>
 <p className="truncate text-xs text-gray-500">@{u.username}</p>
 </div>
 </Link>

 {/* FollowButton — ẩn nếu là chính mình */}
 {!isMe && (
 <FollowButton
 userId={u.id}
 initialState={false}
 onToggle={() => handleFollowToggle(u.id)}
 />
 )}
 </div>
 );
 })}
 </div>
 )}
 </div>
 </div>
 );
}