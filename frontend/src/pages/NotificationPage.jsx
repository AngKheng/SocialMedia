import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";
import { connectWebSocket } from "../api/websocket";
import Navbar from "../components/Navbar";

const TYPE_LABEL = {
  LIKE: "đã thích bài viết của bạn",
  COMMENT: "đã bình luận vào bài viết của bạn",
  FOLLOW: "đã theo dõi bạn",
  MENTION: "đã nhắc đến bạn trong một bình luận",
  GROQ_REPLY: "Groq AI đã trả lời bạn",
  NEW_MESSAGE: "đã gửi tin nhắn cho bạn",
};

export default function NotificationPage() {
  const navigate = useNavigate();

  const [notifications, setNotifications] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  const loadPage = useCallback(async (pageNum) => {
    const res = await api.get("/notifications", {
      params: { page: pageNum, size: 20 },
    });
    return res.data; // PageResponse<NotificationResponse>
  }, []);

  // Load trang đầu lúc vào trang
  useEffect(() => {
    let active = true;
    setLoading(true);

    loadPage(0)
      .then((data) => {
        if (!active) return;
        setNotifications(data.content);
        setHasMore(!data.last);
        setPage(0);
      })
      .catch((err) => console.error("Load notifications thất bại:", err))
      .finally(() => active && setLoading(false));

    // Vào trang này coi như đã xem hết → đánh dấu đã đọc luôn,
    // để badge ở Navbar tự về 0 ngay khi rời trang.
    api.put("/notifications/read").catch(() => {});

    return () => {
      active = false;
    };
  }, [loadPage]);

  // Nhận thông báo real-time qua WebSocket, chèn lên đầu danh sách
  useEffect(() => {
    const unsubscribe = connectWebSocket({
      onNotification: (notif) => {
        setNotifications((prev) => [notif, ...prev]);
      },
    });
    return unsubscribe;
  }, []);

  async function loadMore() {
    if (loadingMore || !hasMore) return;
    setLoadingMore(true);
    try {
      const nextPage = page + 1;
      const data = await loadPage(nextPage);
      setNotifications((prev) => [...prev, ...data.content]);
      setHasMore(!data.last);
      setPage(nextPage);
    } catch (err) {
      console.error("Load more thất bại:", err);
    } finally {
      setLoadingMore(false);
    }
  }

  function handleClick(notif) {
    if (notif.postId) {
      navigate(`/posts/${notif.postId}`);
    } else if (notif.type === "FOLLOW" && notif.actor) {
      // Chưa có trang profile riêng theo username, tạm thời không điều hướng
      // nếu sau này có /users/:id thì đổi navigate ở đây
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="mx-auto max-w-2xl px-4 py-6">
        <h1 className="mb-4 text-xl font-semibold text-gray-900">
          Thông báo
        </h1>

        {loading && (
          <p className="text-sm text-gray-400">Đang tải...</p>
        )}

        {!loading && notifications.length === 0 && (
          <p className="text-sm text-gray-400">
            Bạn chưa có thông báo nào.
          </p>
        )}

        <div className="space-y-1">
          {notifications.map((notif) => (
            <button
              key={notif.id}
              onClick={() => handleClick(notif)}
              className={`flex w-full items-start gap-3 rounded-lg p-3 text-left transition hover:bg-white ${
                notif.isRead ? "" : "bg-blue-50"
              }`}
            >
              <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 text-xs font-medium text-blue-700">
                {notif.actor
                  ? (notif.actor.displayName || notif.actor.username)
                      .slice(0, 2)
                      .toUpperCase()
                  : "AI"}
              </div>

              <div className="flex-1">
                <p className="text-sm text-gray-800">
                  <span className="font-medium">
                    {notif.actor?.displayName || notif.actor?.username || "Groq AI"}
                  </span>{" "}
                  {TYPE_LABEL[notif.type] || "đã tương tác với bạn"}
                </p>
                <p className="mt-0.5 text-xs text-gray-400">
                  {formatTime(notif.createdAt)}
                </p>
              </div>

              {!notif.isRead && (
                <span className="mt-1.5 h-2 w-2 flex-shrink-0 rounded-full bg-blue-600" />
              )}
            </button>
          ))}
        </div>

        {hasMore && !loading && (
          <button
            onClick={loadMore}
            disabled={loadingMore}
            className="mt-4 w-full rounded-md border border-gray-300 bg-white py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            {loadingMore ? "Đang tải..." : "Xem thêm"}
          </button>
        )}
      </div>
    </div>
  );
}

function formatTime(isoString) {
  const date = new Date(isoString);
  const diffMs = Date.now() - date.getTime();
  const diffMin = Math.floor(diffMs / 60000);

  if (diffMin < 1) return "Vừa xong";
  if (diffMin < 60) return `${diffMin} phút trước`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour} giờ trước`;
  const diffDay = Math.floor(diffHour / 24);
  if (diffDay < 7) return `${diffDay} ngày trước`;
  return date.toLocaleDateString("vi-VN");
}
