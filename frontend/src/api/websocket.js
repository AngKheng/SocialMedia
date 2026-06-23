import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

/**
 * 1 kết nối STOMP duy nhất, dùng chung cho toàn app (Navbar, ChatPage, NotificationPage...).
 *
 * Vấn đề ban đầu: nếu mỗi component gọi connectWebSocket() riêng,
 * client cũ bị ghi đè → callback của component gọi trước bị mất.
 * Giải pháp: dùng registry các callback, chỉ tạo client 1 lần duy nhất;
 * các lần gọi sau chỉ đăng ký thêm callback vào registry.
 */
let client = null;
let isConnecting = false;

const chatListeners = new Set();
const notificationListeners = new Set();
const presenceListeners = new Set(); // ← Phase 9H: ai muốn nhận presence update

function notifyChatListeners(payload) {
 chatListeners.forEach((cb) => cb(payload));
}

function notifyNotificationListeners(payload) {
 notificationListeners.forEach((cb) => cb(payload));
}

function notifyPresenceListeners(payload) {
 presenceListeners.forEach((cb) => cb(payload));
}

/**
 * Đăng ký lắng nghe. Tự tạo kết nối WebSocket nếu chưa có.
 * Có thể gọi nhiều lần từ nhiều component khác nhau — an toàn.
 *
 * Props:
 * - onConnect (fn) — gọi khi kết nối thành công
 * - onChatMessage (fn) — nhận tin nhắn chat real-time
 * - onNotification (fn) — nhận notification real-time
 * - onPresence (fn) — nhận presence update (Phase 9H)
 *
 * Trả về hàm cleanup để gỡ đăng ký khi component unmount.
 */
export function connectWebSocket({
 onConnect,
 onChatMessage,
 onNotification,
 onPresence,
} = {}) {
 if (onChatMessage) chatListeners.add(onChatMessage);
 if (onNotification) notificationListeners.add(onNotification);
 if (onPresence) presenceListeners.add(onPresence); // ← Phase 9H

 if (!client && !isConnecting) {
 isConnecting = true;
 const token = localStorage.getItem("accessToken");

 if (!token) {
 isConnecting = false;
 return () => {};
 }

 client = new Client({
 webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
 connectHeaders: {
 Authorization: `Bearer ${token}`,
 },
 reconnectDelay: 5000,
 onConnect: () => {
 client.subscribe("/user/queue/messages", (msg) => {
 notifyChatListeners(JSON.parse(msg.body));
 });

 client.subscribe("/user/queue/notifications", (msg) => {
 notifyNotificationListeners(JSON.parse(msg.body));
 });

 // ← Phase 9H: subscribe presence update
 client.subscribe("/user/queue/presence", (msg) => {
 notifyPresenceListeners(JSON.parse(msg.body));
 });

 if (onConnect) onConnect();
 },
 onStompError: (frame) => {
 console.error("STOMP error:", frame.headers["message"]);
 },
 });

 client.activate();
 isConnecting = false;
 } else if (onConnect && client?.connected) {
 onConnect();
 }

 return () => {
 if (onChatMessage) chatListeners.delete(onChatMessage);
 if (onNotification) notificationListeners.delete(onNotification);
 if (onPresence) presenceListeners.delete(onPresence); // ← Phase 9H
 };
}

/**
 * Chỉ gọi khi người dùng thực sự rời hẳn ứng dụng (ví dụ logout).
 * Không gọi ở cleanup của useEffect trong từng trang riêng lẻ.
 */
export function disconnectWebSocket() {
 if (client) {
 client.deactivate();
 client = null;
 }
 chatListeners.clear();
 notificationListeners.clear();
 presenceListeners.clear(); // ← Phase 9H
}

export function sendChatMessage(receiverId, content) {
 if (!client || !client.connected) {
 console.warn("WebSocket chưa kết nối, không gửi được");
 return false;
 }

 client.publish({
 destination: "/app/chat.send",
 body: JSON.stringify({ receiverId, content }),
 });
 return true;
}