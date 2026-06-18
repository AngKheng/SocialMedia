import { useEffect, useState } from "react";
import api from "../api/axios";
import Navbar from "../components/Navbar";
import CreatePostForm from "../components/CreatePostForm";
import PostCard from "../components/PostCard";

export default function FeedPage() {
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadFeed(pageNum) {
    setLoading(true);
    setError("");
    try {
      const res = await api.get("/posts/feed", {
        params: { page: pageNum, size: 10 },
      });
      // PageResponse: { content, page, size, totalElements, totalPages, hasNext, hasPrevious }
      setPosts((prev) =>
        pageNum === 0 ? res.data.content : [...prev, ...res.data.content]
      );
      setHasNext(res.data.hasNext);
      setPage(pageNum);
    } catch (err) {
      setError("Không tải được feed, thử lại sau");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadFeed(0);
  }, []);

  function handlePostCreated(newPost) {
    // Thêm bài mới lên đầu feed, không cần gọi lại API
    setPosts((prev) => [newPost, ...prev]);
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="mx-auto max-w-xl space-y-4 px-4 py-6">
        <CreatePostForm onPostCreated={handlePostCreated} />

        {error && (
          <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">
            {error}
          </div>
        )}

        {posts.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}

        {loading && (
          <p className="text-center text-sm text-gray-400">Đang tải...</p>
        )}

        {!loading && posts.length === 0 && (
          <p className="text-center text-sm text-gray-400">
            Chưa có bài viết nào. Follow ai đó hoặc tự đăng bài đầu tiên!
          </p>
        )}

        {!loading && hasNext && (
          <button
            onClick={() => loadFeed(page + 1)}
            className="w-full rounded-md border border-gray-300 py-2 text-sm text-gray-600 hover:bg-gray-100"
          >
            Xem thêm
          </button>
        )}
      </div>
    </div>
  );
}
