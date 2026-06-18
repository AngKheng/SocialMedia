import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

/**
 * Tạo và quản lý 1 kết nối STOMP duy nhất cho toàn app.
 * Dùng accessToken hiện tại để xác thực lúc CONNECT
 * (khớp với WebSocketAuthInterceptor ở backend).
 */
let client = null;

export function connectWebSocket({ onConnect, onChatMessage, onNotification }) {
  const token = localStorage.getItem("accessToken");
  if (!token) return null;

  client = new Client({
    webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 5000, // tự reconnect sau 5s nếu mất kết nối
    onConnect: () => {
      // Lắng nghe tin nhắn chat real-time
      if (onChatMessage) {
        client.subscribe("/user/queue/messages", (msg) => {
          onChatMessage(JSON.parse(msg.body));
        });
      }

      // Lắng nghe thông báo real-time
      if (onNotification) {
        client.subscribe("/user/queue/notifications", (msg) => {
          onNotification(JSON.parse(msg.body));
        });
      }

      if (onConnect) onConnect();
    },
    onStompError: (frame) => {
      console.error("STOMP error:", frame.headers["message"]);
    },
  });

  client.activate();
  return client;
}

export function disconnectWebSocket() {
  if (client) {
    client.deactivate();
    client = null;
  }
}

/**
 * Gửi tin nhắn qua WebSocket (thay thế cho gọi REST POST /api/messages).
 * Khớp với @MessageMapping("/chat.send") ở backend.
 */
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
