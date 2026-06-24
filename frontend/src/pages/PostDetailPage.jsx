import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";
import CommentSection from "../components/CommentSection";
import UserAvatar from "../components/UserAvatar";

export default function PostDetailPage() {
  const { id } = useParams();

  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadPost();
    loadComments();
  }, [id]);

  async function loadPost() {
    setLoading(true);
    setError("");
    try {
      const res = await api.get(`/posts/${id}`);
      setPost(res.data);
      setIsLiked(res.data.isLiked);
      setLikeCount(res.data.likeCount);
    } catch (err) {
      setError("Không tìm thấy bài viết này");
    } finally {
      setLoading(false);
    }
  }

  async function loadComments() {
    try {
      const res = await api.get(`/comments/post/${id}`);
      setComments(res.data);
    } catch (err) {
      console.error("Load comments thất bại:", err);
    }
  }

  async function toggleLike() {
    try {
      const res = isLiked
        ? await api.delete(`/posts/${id}/like`)
        : await api.post(`/posts/${id}/like`);
      setIsLiked(res.data.isLiked);
      setLikeCount(res.data.likeCount);
    } catch (err) {
      console.error("Like/unlike thất bại:", err);
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

  if (error || !post) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <p className="py-10 text-center text-sm text-red-500">{error}</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="mx-auto max-w-xl px-4 py-6">
        <Link
          to="/feed"
          className="mb-3 inline-block text-sm text-blue-600 hover:underline"
        >
          ← Quay lại Feed
        </Link>

        <div className="rounded-xl border border-gray-200 bg-white p-4">
          <div className="mb-2 flex items-center gap-2">
            <UserAvatar user={post.user} size="lg" />
            <div>
              <p className="text-sm font-medium text-gray-900">
                {post.user.displayName || post.user.username}
              </p>
              <p className="text-xs text-gray-500">@{post.user.username}</p>
            </div>
          </div>

          <p className="whitespace-pre-wrap text-sm text-gray-800">
            {post.content}
          </p>

          {post.mediaUrls?.length > 0 && (
            <div className="mt-2 grid grid-cols-2 gap-1">
              {post.mediaUrls.map((url, i) =>
                url.match(/\.(mp4|mov|avi|webm)$/i) ? (
                  <video key={i} src={url} controls className="rounded-lg" />
                ) : (
                  <img key={i} src={url} alt="" className="rounded-lg object-cover" />
                )
              )}
            </div>
          )}

          <div className="mt-3 flex items-center gap-4 border-t border-gray-100 pt-2 text-sm text-gray-500">
            <button
              onClick={toggleLike}
              className={`flex items-center gap-1 ${
                isLiked ? "text-red-500" : "hover:text-red-500"
              }`}
            >
              {isLiked ? "♥" : "♡"} {likeCount}
            </button>
            <span>💬 {post.commentCount} bình luận</span>
          </div>
        </div>

        <CommentSection
          postId={post.id}
          comments={comments}
          setComments={setComments}
        />
      </div>
    </div>
  );
}
