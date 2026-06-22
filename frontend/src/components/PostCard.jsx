import { Link } from "react-router-dom";
import { useState } from "react";
import api from "../api/axios";
import FollowButton from "./FollowButton";

/**
 * Hiển thị 1 bài viết trong Feed.
 * Bấm vào nội dung/avatar → chuyển sang trang chi tiết /posts/{id}.
 * Nút like/unlike xử lý ngay tại đây, không cần qua trang chi tiết.
 * Nút Follow/Unfollow hiện cạnh tên tác giả (ẩn tự động nếu là bài của chính mình).
 */
export default function PostCard({ post }) {
  const [isLiked, setIsLiked] = useState(post.isLiked || false);
  const [likeCount, setLikeCount] = useState(post.likeCount);
  const [loading, setLoading] = useState(false);

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

  const initials = (post.user.displayName || post.user.username)
    .slice(0, 2)
    .toUpperCase();

  return (
    <div className="rounded-xl border border-gray-200 bg-white p-4">
      {/* Header: avatar + tên + nút Follow */}
      <div className="mb-2 flex items-center justify-between">
        <Link to={`/posts/${post.id}`} className="flex items-center gap-2">
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

        <FollowButton
          userId={post.user.id}
          initialState={post.user.isFollowing || false}
        />
      </div>

      {/* Nội dung bài viết */}
      <Link to={`/posts/${post.id}`} className="block">
        <p className="whitespace-pre-wrap text-sm text-gray-800">
          {post.content}
        </p>

        {post.mediaUrls?.length > 0 && (
          <div className="mt-2 grid grid-cols-2 gap-1">
            {post.mediaUrls.map((url, i) =>
              url.match(/\.(mp4|mov|avi|webm)$/i) ? (
                <video key={i} src={url} controls className="rounded-lg" />
              ) : (
                <img
                  key={i}
                  src={url}
                  alt=""
                  className="rounded-lg object-cover"
                />
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
    </div>
  );
}
