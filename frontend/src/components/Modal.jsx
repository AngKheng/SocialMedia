import { useEffect } from "react";
import { createPortal } from "react-dom";

/**
 * Modal dùng chung.
 * Props:
 * - isOpen (bool) — có đang mở không
 * - onClose (fn) — callback khi đóng (ESC, click backdrop, click X)
 * - title (string) — tiêu đề hiển thị trên header
 * - children — nội dung body
 *
 * Đóng khi: nhấn ESC, click ra ngoài backdrop, click nút X.
 * Render qua createPortal để tránh xung đột z-index với Navbar.
 */
export default function Modal({ isOpen, onClose, title, children }) {
 useEffect(() => {
 if (!isOpen) return;

 function handleKeyDown(e) {
 if (e.key === "Escape") onClose();
 }
 function lockScroll() {
 document.body.style.overflow = "hidden";
 }
 function unlockScroll() {
 document.body.style.overflow = "";
 }

 document.addEventListener("keydown", handleKeyDown);
 lockScroll();
 return () => {
 document.removeEventListener("keydown", handleKeyDown);
 unlockScroll();
 };
 }, [isOpen, onClose]);

 if (!isOpen) return null;

 return createPortal(
 <div
 className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
 onClick={onClose}
 >
 <div
 className="w-full max-w-lg rounded-xl bg-white shadow-lg"
 onClick={(e) => e.stopPropagation()}
 >
 {/* Header */}
 <div className="flex items-center justify-between border-b border-gray-200 px-5 py-3">
 <h2 className="text-base font-semibold text-gray-900">{title}</h2>
 <button
 onClick={onClose}
 className="text-xl leading-none text-gray-400 hover:text-gray-600"
 aria-label="Đóng"
 >
 ×
 </button>
 </div>

 {/* Body */}
 <div className="px-5 py-4">{children}</div>
 </div>
 </div>,
 document.body
 );
}
