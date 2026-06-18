import { useEffect, useRef, useState } from "react";
import api from "../api/axios";
import { connectWebSocket, disconnectWebSocket, sendChatMessage } from "../api/websocket";
import { useAuth } from "../context/AuthContext";
import Navbar from "../components/Navbar";

export default function ChatPage() {
  const { user } = useAuth();

  const [conversations, setConversations] = useState([]);
  const [activeUser, setActiveUser] = useState(null); // { id, username, displayName }
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(true);

  const bottomRef = useRef(null);

  // Load danh sách hội thoại lúc vào trang
  useEffect(() => {
    loadConversations();
  }, []);

  // Kết nối WebSocket 1 lần, nhận tin nhắn real-time
  useEffect(() => {
    connectWebSocket({
      onChatMessage: (msg) => {
        // Nếu tin nhắn thuộc cuộc hội thoại đang mở → thêm vào ngay
        setActiveUser((current) => {
          if (current && (msg.senderId === current.id || msg.receiverId === current.id)) {
            setMessages((prev) => [...prev, msg]);
          }
          return current;
        });
        // Cập nhật lại danh sách hội thoại (để hiện tin mới nhất + unread)
        loadConversations();
      },
    });

    return () => disconnectWebSocket();
  }, []);

  // Tự cuộn xuống cuối khi có tin nhắn mới
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  async function loadConversations() {
    try {
      const res = await api.get("/messages");
      setConversations(res.data);
    } catch (err) {
      console.error("Load conversations thất bại:", err);
    } finally {
      setLoading(false);
    }
  }

  async function openConversation(otherUser) {
    setActiveUser(otherUser);
    try {
      const res = await api.get(`/messages/${otherUser.id}`);
      setMessages(res.data);
      // Đánh dấu đã đọc xong → refresh lại sidebar để bỏ badge unread
      loadConversations();
    } catch (err) {
      console.error("Load conversation thất bại:", err);
    }
  }

  function handleSend(e) {
    e.preventDefault();
    if (!input.trim() || !activeUser) return;

    const sent = sendChatMessage(activeUser.id, input);

    if (sent) {
      // Optimistic UI: hiện tin nhắn ngay, không cần chờ server
      setMessages((prev) => [
        ...prev,
        {
          senderId: user.id,
          receiverId: activeUser.id,
          content: input,
          createdAt: new Date().toISOString(),
        },
      ]);
      setInput("");
    } else {
      // WebSocket chưa kết nối → fallback gửi qua REST
      api.post("/messages", { receiverId: activeUser.id, content: input })
        .then((res) => {
          setMessages((prev) => [...prev, res.data]);
          setInput("");
        })
        .catch((err) => console.error("Gửi tin nhắn thất bại:", err));
    }
  }

  return (
    <div className="flex h-screen flex-col">
      <Navbar />

      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar danh sách hội thoại */}
        <div className="w-72 flex-shrink-0 overflow-y-auto border-r border-gray-200 bg-white">
          <NewChatBox onStart={openConversation} />

          {loading && (
            <p className="p-4 text-sm text-gray-400">Đang tải...</p>
          )}

          {!loading && conversations.length === 0 && (
            <p className="p-4 text-sm text-gray-400">
              Chưa có cuộc trò chuyện nào.
            </p>
          )}

          {conversations.map((conv) => (
            <button
              key={conv.otherUser.id}
              onClick={() => openConversation(conv.otherUser)}
              className={`flex w-full items-center gap-3 border-b border-gray-100 p-3 text-left hover:bg-gray-50 ${
                activeUser?.id === conv.otherUser.id ? "bg-blue-50" : ""
              }`}
            >
              <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 text-sm font-medium text-blue-700">
                {(conv.otherUser.displayName || conv.otherUser.username)
                  .slice(0, 2)
                  .toUpperCase()}
              </div>
              <div className="flex-1 overflow-hidden">
                <p className="truncate text-sm font-medium text-gray-900">
                  {conv.otherUser.displayName || conv.otherUser.username}
                </p>
                <p className="truncate text-xs text-gray-500">
                  {conv.lastMessage}
                </p>
              </div>
              {conv.unreadCount > 0 && (
                <span className="flex h-5 w-5 items-center justify-center rounded-full bg-blue-600 text-xs text-white">
                  {conv.unreadCount}
                </span>
              )}
            </button>
          ))}
        </div>

        {/* Khung chat */}
        <div className="flex flex-1 flex-col bg-gray-50">
          {!activeUser ? (
            <div className="flex flex-1 items-center justify-center text-sm text-gray-400">
              Chọn một cuộc trò chuyện để bắt đầu
            </div>
          ) : (
            <>
              <div className="border-b border-gray-200 bg-white p-3">
                <p className="text-sm font-medium text-gray-900">
                  {activeUser.displayName || activeUser.username}
                </p>
              </div>

              <div className="flex-1 space-y-2 overflow-y-auto p-4">
                {messages.map((msg, i) => {
                  const isMine = msg.senderId === user.id;
                  return (
                    <div
                      key={i}
                      className={`flex ${isMine ? "justify-end" : "justify-start"}`}
                    >
                      <div
                        className={`max-w-xs rounded-2xl px-3 py-2 text-sm ${
                          isMine
                            ? "bg-blue-600 text-white"
                            : "bg-white text-gray-800"
                        }`}
                      >
                        {msg.content}
                      </div>
                    </div>
                  );
                })}
                <div ref={bottomRef} />
              </div>

              <form onSubmit={handleSend} className="border-t border-gray-200 bg-white p-3">
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder="Nhập tin nhắn..."
                    className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-blue-500"
                  />
                  <button
                    type="submit"
                    disabled={!input.trim()}
                    className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
                  >
                    Gửi
                  </button>
                </div>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

/**
 * Ô nhỏ để bắt đầu chat với 1 user chưa từng nhắn tin.
 * Tạm thời nhập trực tiếp User ID — sau này có thể nâng cấp
 * thành search theo username khi backend hỗ trợ.
 */
function NewChatBox({ onStart }) {
  const [userId, setUserId] = useState("");
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    if (!userId.trim()) return;
    setError("");

    try {
      const res = await api.get(`/users/${userId}`);
      onStart({
        id: res.data.id,
        username: res.data.username,
        displayName: res.data.displayName,
      });
      setUserId("");
    } catch (err) {
      setError("Không tìm thấy user với ID này");
    }
  }

  return (
    <form onSubmit={handleSubmit} className="border-b border-gray-200 p-3">
      <div className="flex gap-2">
        <input
          type="number"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          placeholder="Nhập User ID để chat"
          className="flex-1 rounded-md border border-gray-300 px-2 py-1.5 text-xs outline-none focus:border-blue-500"
        />
        <button
          type="submit"
          className="rounded-md bg-blue-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-blue-700"
        >
          Chat
        </button>
      </div>
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </form>
  );
}
