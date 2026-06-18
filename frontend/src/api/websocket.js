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

function notifyChatListeners(payload) {
  chatListeners.forEach((cb) => cb(payload));
}

function notifyNotificationListeners(payload) {
  notificationListeners.forEach((cb) => cb(payload));
}

/**
 * Đăng ký lắng nghe. Tự tạo kết nối WebSocket nếu chưa có.
 * Có thể gọi nhiều lần từ nhiều component khác nhau — an toàn.
 *
 * Trả về hàm cleanup để gỡ đăng ký khi component unmount
 * (tránh setState trên component đã unmount).
 */
export function connectWebSocket({ onConnect, onChatMessage, onNotification } = {}) {
  if (onChatMessage) chatListeners.add(onChatMessage);
  if (onNotification) notificationListeners.add(onNotification);

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

        if (onConnect) onConnect();
      },
      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers["message"]);
      },
    });

    client.activate();
    isConnecting = false;
  } else if (onConnect && client?.connected) {
    // Client đã kết nối từ trước (do component khác gọi trước) → gọi callback ngay
    onConnect();
  }

  return () => {
    if (onChatMessage) chatListeners.delete(onChatMessage);
    if (onNotification) notificationListeners.delete(onNotification);
  };
}

/**
 * Chỉ gọi khi người dùng thực sự rời hẳn ứng dụng (ví dụ logout).
 * Không gọi ở cleanup của useEffect trong từng trang riêng lẻ,
 * vì các trang khác có thể vẫn cần dùng chung kết nối này.
 */
export function disconnectWebSocket() {
  if (client) {
    client.deactivate();
    client = null;
  }
  chatListeners.clear();
  notificationListeners.clear();
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
