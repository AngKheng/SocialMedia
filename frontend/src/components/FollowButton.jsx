import { useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

/**
 * Nút Follow/Unfollow tái sử dụng.
 * Props:
 *   - userId        (number)  — ID người muốn follow/unfollow
 *   - initialState  (boolean) — true nếu đang follow rồi
 *   - onToggle?     (fn)      — callback sau khi toggle (nhận { isFollowing, followerCount })
 *
 * Tự ẩn nếu userId chính là người đang đăng nhập (không follow chính mình).
 */
export default function FollowButton({ userId, initialState = false, onToggle }) {
  const { user: me } = useAuth();
  const [isFollowing, setIsFollowing] = useState(initialState);
  const [loading, setLoading] = useState(false);

  // Ẩn nút nếu đây là profile của chính mình
  if (!userId || me?.id === userId) return null;

  async function handleToggle(e) {
    e.preventDefault();
    e.stopPropagation(); // tránh trigger Link bọc bên ngoài (PostCard)
    if (loading) return;
    setLoading(true);

    try {
      const res = isFollowing
        ? await api.delete(`/follow/${userId}`)
        : await api.post(`/follow/${userId}`);

      setIsFollowing(res.data.isFollowing);
      if (onToggle) onToggle(res.data);
    } catch (err) {
      console.error("Follow/unfollow thất bại:", err);
    } finally {
      setLoading(false);
    }
  }

  return (
    <button
      onClick={handleToggle}
      disabled={loading}
      className={`rounded-full px-3 py-1 text-xs font-medium transition ${
        isFollowing
          ? "border border-gray-300 text-gray-600 hover:border-red-400 hover:text-red-500"
          : "bg-blue-600 text-white hover:bg-blue-700"
      } disabled:opacity-50`}
    >
      {loading ? "..." : isFollowing ? "Đang theo dõi" : "Theo dõi"}
    </button>
  );
}
