import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import api from "../api/axios";
import { connectWebSocket } from "../api/websocket";

export default function Navbar() {
 const { user, logout } = useAuth();
 const navigate = useNavigate();
 const [unreadCount, setUnreadCount] = useState(0);
 const [menuOpen, setMenuOpen] = useState(false);
 const menuRef = useRef(null);

 function handleLogout() {
 setMenuOpen(false);
 logout();
 navigate("/login");
 }

 // Lấy số chưa đọc lúc mount (vào trang nào cũng thấy badge đúng)
 useEffect(() => {
 api.get("/notifications/unread-count")
 .then((res) => setUnreadCount(res.data.unreadCount))
 .catch(() => {});
 }, []);

 // Có notification mới qua WebSocket → tăng badge ngay, không cần chờ refresh
 useEffect(() => {
 const unsubscribe = connectWebSocket({
 onNotification: () => {
 setUnreadCount((prev) => prev + 1);
 },
 });
 return unsubscribe;
 }, []);

 // Click ra ngoài dropdown thì đóng
 useEffect(() => {
 function handleClickOutside(e) {
 if (menuRef.current && !menuRef.current.contains(e.target)) {
 setMenuOpen(false);
 }
 }
 if (menuOpen) {
 document.addEventListener("mousedown", handleClickOutside);
 return () => document.removeEventListener("mousedown", handleClickOutside);
 }
 }, [menuOpen]);

 return (
 <nav className="sticky top-0 z-10 flex items-center justify-between border-b border-gray-200 bg-white px-4 py-3">
 <Link to="/feed" className="text-lg font-bold text-blue-600">
 SocialApp
 </Link>

 <div className="flex items-center gap-4 text-sm">
 <Link to="/feed" className="text-gray-600 hover:text-blue-600">
 Feed
 </Link>
 <Link to="/chat" className="text-gray-600 hover:text-blue-600">
 Chat
 </Link>
 <Link
 to="/notifications"
 onClick={() => setUnreadCount(0)}
 className="relative text-gray-600 hover:text-blue-600"
 >
 Thông báo
 {unreadCount > 0 && (
 <span className="absolute -right-3 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-medium text-white">
 {unreadCount > 9 ? "9+" : unreadCount}
 </span>
 )}
 </Link>

 <span className="text-gray-400">|</span>

 {/* Dropdown menu user */}
 <div className="relative" ref={menuRef}>
 <button
 onClick={() => setMenuOpen(!menuOpen)}
 className="font-medium text-gray-700 hover:text-blue-600"
 >
 {user?.displayName || user?.username} ▾
 </button>

 {menuOpen && (
 <div className="absolute right-0 top-full z-20 mt-1 w-48 overflow-hidden rounded-md border border-gray-200 bg-white shadow-lg">
 <Link
 to="/profile/me"
 onClick={() => setMenuOpen(false)}
 className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50"
 >
 Trang cá nhân
 </Link>
 <Link
 to="/change-password"
 onClick={() => setMenuOpen(false)}
 className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50"
 >
 Đổi mật khẩu
 </Link>
 <div className="border-t border-gray-100" />
 <button
 onClick={handleLogout}
 className="block w-full px-4 py-2 text-left text-sm text-red-500 hover:bg-gray-50"
 >
 Đăng xuất
 </button>
 </div>
 )}
 </div>
 </div>
 </nav>
 );
}
