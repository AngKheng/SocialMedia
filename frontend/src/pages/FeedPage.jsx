import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";
import PostCard from "../components/PostCard";
import CreatePostForm from "../components/CreatePostForm";
import FollowButton from "../components/FollowButton";

export default function FeedPage() {
  // ── Feed state ──────────────────────────────────────────
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [feedLoading, setFeedLoading] = useState(true);

  // ── Search state ─────────────────────────────────────────
  const [query, setQuery] = useState("");
  const [searchUsers, setSearchUsers] = useState([]);
  const [searchPosts, setSearchPosts] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const debounceRef = useRef(null);

  const isSearching = query.trim().length >= 2;

  // ── Load feed lần đầu ────────────────────────────────────
  useEffect(() => {
    loadFeed(0);
  }, []);

  async function loadFeed(pageNum) {
    try {
      const res = await api.get("/posts/feed", {
        params: { page: pageNum, size: 10 },
      });
      if (pageNum === 0) {
        setPosts(res.data.content);
      } else {
        setPosts((prev) => [...prev, ...res.data.content]);
      }
      setHasMore(!res.data.last);
      setPage(pageNum);
    } catch (err) {
      console.error("Load feed thất bại:", err);
    } finally {
      setFeedLoading(false);
    }
  }

  function handlePostCreated(newPost) {
    setPosts((prev) => [newPost, ...prev]);
  }

  // ── Search với debounce 400ms ─────────────────────────────
  useEffect(() => {
    clearTimeout(debounceRef.current);

    if (!isSearching) {
      setSearchUsers([]);
      setSearchPosts([]);
      return;
    }

    setSearchLoading(true);
    debounceRef.current = setTimeout(async () => {
      try {
        const [usersRes, postsRes] = await Promise.all([
          api.get("/search/users", { params: { q: query.trim() } }),
          api.get("/search/posts", { params: { q: query.trim() } }),
        ]);
        setSearchUsers(usersRes.data);
        setSearchPosts(postsRes.data);
      } catch (err) {
        console.error("Search thất bại:", err);
      } finally {
        setSearchLoading(false);
      }
    }, 400);

    return () => clearTimeout(debounceRef.current);
  }, [query]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="mx-auto max-w-2xl px-4 py-6">

        {/* Thanh tìm kiếm */}
        <div className="relative mb-4">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
            🔍
          </span>
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Tìm kiếm người dùng hoặc bài viết..."
            className="w-full rounded-xl border border-gray-200 bg-white py-2.5 pl-9 pr-4 text-sm outline-none focus:border-blue-400 focus:ring-1 focus:ring-blue-100"
          />
          {query && (
            <button
              onClick={() => setQuery("")}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          )}
        </div>

        {/* ── Kết quả tìm kiếm ── */}
        {isSearching && (
          <div className="mb-6 space-y-5">
            {searchLoading && (
              <p className="text-center text-sm text-gray-400">Đang tìm...</p>
            )}

            {!searchLoading && (
              <>
                {/* Section: Người dùng */}
                <div>
                  <p className="mb-2 text-xs font-medium uppercase tracking-wide text-gray-400">
                    Người dùng
                  </p>
                  {searchUsers.length === 0 ? (
                    <p className="text-sm text-gray-400">Không tìm thấy người dùng nào.</p>
                  ) : (
                    <div className="divide-y divide-gray-100 rounded-xl border border-gray-200 bg-white">
                      {searchUsers.map((u) => (
                        <div
                          key={u.id}
                          className="flex items-center gap-3 p-3"
                        >
                          <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 text-sm font-medium text-blue-700">
                            {(u.displayName || u.username).slice(0, 2).toUpperCase()}
                          </div>
                          <div className="flex-1 overflow-hidden">
                            <p className="truncate text-sm font-medium text-gray-900">
                              {u.displayName || u.username}
                            </p>
                            <p className="truncate text-xs text-gray-500">
                              @{u.username}
                            </p>
                          </div>
                          <FollowButton
                            userId={u.id}
                            initialState={false}
                          />
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Section: Bài viết */}
                <div>
                  <p className="mb-2 text-xs font-medium uppercase tracking-wide text-gray-400">
                    Bài viết
                  </p>
                  {searchPosts.length === 0 ? (
                    <p className="text-sm text-gray-400">Không tìm thấy bài viết nào.</p>
                  ) : (
                    <div className="space-y-3">
                      {searchPosts.map((post) => (
                        <PostCard key={post.id} post={post} />
                      ))}
                    </div>
                  )}
                </div>

                {searchUsers.length === 0 && searchPosts.length === 0 && (
                  <p className="text-center text-sm text-gray-400">
                    Không tìm thấy kết quả nào cho &ldquo;{query}&rdquo;
                  </p>
                )}
              </>
            )}
          </div>
        )}

        {/* ── Feed bình thường (ẩn khi đang search) ── */}
        {!isSearching && (
          <>
            <CreatePostForm onPostCreated={handlePostCreated} />

            <div className="mt-4 space-y-4">
              {feedLoading && (
                <p className="text-center text-sm text-gray-400">Đang tải feed...</p>
              )}

              {!feedLoading && posts.length === 0 && (
                <p className="text-center text-sm text-gray-400">
                  Feed trống. Hãy follow người dùng khác để xem bài viết của họ!
                </p>
              )}

              {posts.map((post) => (
                <PostCard key={post.id} post={post} />
              ))}

              {hasMore && !feedLoading && (
                <button
                  onClick={() => loadFeed(page + 1)}
                  className="w-full rounded-xl border border-gray-200 bg-white py-2.5 text-sm font-medium text-gray-600 hover:bg-gray-50"
                >
                  Xem thêm
                </button>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
