import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";
import UserAvatar from "../components/UserAvatar";
import AvatarCropModal from "../components/AvatarCropModal";
import PostCard from "../components/PostCard";
import { useAuth } from "../context/AuthContext";

export default function ProfilePage() {
  const { id } = useParams();          // string từ URL
  const { user: me, login } = useAuth();

  const [profile, setProfile] = useState(null);
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showCropModal, setShowCropModal] = useState(false);

  // So sánh dạng string để tránh lỗi kiểu number vs Long
  const isOwner = me && profile && String(me.id) === String(profile.id);

  useEffect(() => {
    if (!id || id === "undefined") {
      setError("Không tìm thấy người dùng");
      setLoading(false);
      return;
    }
    loadProfile();
    loadPosts();
  }, [id]);

  async function loadProfile() {
    setLoading(true);
    setError("");
    try {
      const res = await api.get(`/users/${id}`);
      setProfile(res.data);
    } catch (err) {
      if (err.response?.status === 404) {
        setError("Người dùng không tồn tại");
      } else {
        setError("Không tải được thông tin người dùng");
      }
    } finally {
      setLoading(false);
    }
  }

  async function loadPosts() {
    try {
      const res = await api.get(`/posts/user/${id}`, {
        params: { page: 0, size: 20 },
      });
      setPosts(res.data.content);
    } catch (err) {
      console.error("Load posts thất bại:", err);
    }
  }

  async function toggleFollow() {
    if (!profile) return;
    try {
      const res = profile.isFollowing
        ? await api.delete(`/follow/${profile.id}`)
        : await api.post(`/follow/${profile.id}`);
      setProfile((prev) => ({
        ...prev,
        isFollowing: res.data.isFollowing,
        followerCount: res.data.followerCount,
      }));
    } catch (err) {
      console.error("Follow thất bại:", err);
    }
  }

  function handleAvatarSaved(avatarUrl) {
    // Cập nhật state trang
    setProfile((prev) => ({ ...prev, avatarUrl }));

    // Cập nhật AuthContext để Navbar + mọi nơi dùng user đổi ngay
    login({
      accessToken: localStorage.getItem("accessToken"),
      refreshToken: localStorage.getItem("refreshToken"),
      user: { ...me, avatarUrl },
    });

    setShowCropModal(false);
  }

  // ── Loading / Error ────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <p className="py-16 text-center text-sm text-gray-400">Đang tải...</p>
      </div>
    );
  }

  if (error || !profile) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <p className="py-16 text-center text-sm text-red-500">
          {error || "Không tìm thấy người dùng"}
        </p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="mx-auto max-w-xl px-4 py-6">
        {/* Profile card */}
        <div className="mb-4 rounded-xl border border-gray-200 bg-white p-5">
          <div className="flex items-start gap-4">
            {/* Avatar — click để đổi nếu là chủ tài khoản */}
            <UserAvatar
              user={profile}
              size="xl"
              editable={isOwner}
              onClick={isOwner ? () => setShowCropModal(true) : undefined}
            />

            <div className="min-w-0 flex-1">
              <div className="flex items-center justify-between gap-2">
                <div className="min-w-0">
                  <p className="truncate text-base font-semibold text-gray-900">
                    {profile.displayName || profile.username}
                  </p>
                  <p className="text-sm text-gray-500">@{profile.username}</p>
                </div>

                {!isOwner && (
                  <button
                    onClick={toggleFollow}
                    className={`flex-shrink-0 rounded-full px-4 py-1.5 text-sm font-medium transition ${
                      profile.isFollowing
                        ? "border border-gray-300 text-gray-600 hover:border-red-300 hover:text-red-500"
                        : "bg-blue-600 text-white hover:bg-blue-700"
                    }`}
                  >
                    {profile.isFollowing ? "Đang theo dõi" : "Theo dõi"}
                  </button>
                )}
              </div>

              {profile.bio && (
                <p className="mt-2 text-sm text-gray-700">{profile.bio}</p>
              )}

              <div className="mt-3 flex gap-4 text-sm">
                <span>
                  <strong className="text-gray-900">
                    {profile.followerCount}
                  </strong>{" "}
                  <span className="text-gray-500">người theo dõi</span>
                </span>
                <span>
                  <strong className="text-gray-900">
                    {profile.followingCount}
                  </strong>{" "}
                  <span className="text-gray-500">đang theo dõi</span>
                </span>
              </div>
            </div>
          </div>

          {isOwner && (
            <p className="mt-3 text-center text-xs text-gray-400">
              Nhấn vào ảnh đại diện để thay đổi
            </p>
          )}
        </div>

        {/* Bài viết */}
        <div className="space-y-3">
          {posts.length === 0 ? (
            <p className="py-8 text-center text-sm text-gray-400">
              {isOwner ? "Bạn chưa có bài viết nào" : "Chưa có bài viết nào"}
            </p>
          ) : (
            posts.map((post) => <PostCard key={post.id} post={post} />)
          )}
        </div>
      </div>

      {showCropModal && (
        <AvatarCropModal
          onClose={() => setShowCropModal(false)}
          onSaved={handleAvatarSaved}
        />
      )}
    </div>
  );
}
