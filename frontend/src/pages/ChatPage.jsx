import { useEffect, useRef, useState } from "react";
import api from "../api/axios";
import { connectWebSocket, sendChatMessage } from "../api/websocket";
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

 // Đăng ký lắng nghe tin nhắn real-time.
 // Dùng hàm cleanup do connectWebSocket trả về để gỡ đăng ký khi unmount —
 // KHÔNG disconnect toàn bộ kết nối, vì Navbar/NotificationPage vẫn cần dùng chung.
 useEffect(() => {
 const unsubscribe = connectWebSocket({
 onChatMessage: (msg) => {
 setActiveUser((current) => {
 if (current && (msg.senderId === current.id || msg.receiverId === current.id)) {
 setMessages((prev) => [...prev, msg]);
 }
 return current;
 });
 loadConversations();
 },
 });

 return unsubscribe;
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
 * Ô search để bắt đầu chat với 1 user (kể cả khi chưa từng nhắn tin).
 * Gõ username hoặc displayName (>= 2 ký tự) — dropdown hiện kết quả để chọn.
 * Pattern debounce + click-outside lấy cảm hứng từ FeedPage search.
 */
function NewChatBox({ onStart }) {
 const [query, setQuery] = useState("");
 const [results, setResults] = useState([]);
 const [loading, setLoading] = useState(false);
 const [showDropdown, setShowDropdown] = useState(false);
 const debounceRef = useRef(null);
 const containerRef = useRef(null);

 // Search với debounce 400ms — y hệt pattern FeedPage.
 useEffect(() => {
 clearTimeout(debounceRef.current);

 if (query.trim().length < 2) {
 setResults([]);
 setShowDropdown(false);
 return;
 }

 setLoading(true);
 setShowDropdown(true);
 debounceRef.current = setTimeout(async () => {
 try {
 const res = await api.get("/search/users", {
 params: { q: query.trim() },
 });
 setResults(res.data);
 } catch (err) {
 console.error("Search user thất bại:", err);
 setResults([]);
 } finally {
 setLoading(false);
 }
 }, 400);

 return () => clearTimeout(debounceRef.current);
 }, [query]);

 // Click ra ngoài container thì đóng dropdown.
 useEffect(() => {
 function handleClickOutside(e) {
 if (containerRef.current && !containerRef.current.contains(e.target)) {
 setShowDropdown(false);
 }
 }
 document.addEventListener("mousedown", handleClickOutside);
 return () => document.removeEventListener("mousedown", handleClickOutside);
 }, []);

 function handleSelect(user) {
 onStart({
 id: user.id,
 username: user.username,
 displayName: user.displayName,
 });
 setQuery("");
 setResults([]);
 setShowDropdown(false);
 }

 return (
 <form
 onSubmit={(e) => e.preventDefault()}
 ref={containerRef}
 className="relative border-b border-gray-200 p-3"
 >
 <input
 type="text"
 value={query}
 onChange={(e) => setQuery(e.target.value)}
 placeholder="Tìm theo username hoặc tên..."
 className="w-full rounded-md border border-gray-300 px-2 py-1.5 text-xs outline-none focus:border-blue-500"
 />

 {showDropdown && (
 <div className="absolute left-3 right-3 top-full z-10 mt-1 max-h-80 overflow-y-auto rounded-md border border-gray-200 bg-white shadow-lg">
 {loading && (
 <p className="p-3 text-xs text-gray-400">Đang tìm...</p>
 )}

 {!loading && results.length === 0 && (
 <p className="p-3 text-xs text-gray-400">
 Không tìm thấy người dùng nào.
 </p>
 )}

 {!loading &&
 results.map((u) => (
 <button
 key={u.id}
 type="button"
 onClick={() => handleSelect(u)}
 className="flex w-full items-center gap-3 border-b border-gray-100 p-2 text-left hover:bg-gray-50"
 >
 <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 text-xs font-medium text-blue-700">
 {(u.displayName || u.username).slice(0, 2).toUpperCase()}
 </div>
 <div className="flex-1 overflow-hidden">
 <p className="truncate text-xs font-medium text-gray-900">
 {u.displayName || u.username}
 </p>
 <p className="truncate text-[11px] text-gray-500">
 @{u.username}
 </p>
 </div>
 </button>
 ))}
 </div>
 )}
 </form>
 );
}