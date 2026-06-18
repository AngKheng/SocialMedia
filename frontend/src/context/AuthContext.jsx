import { createContext, useContext, useState } from "react";
import { disconnectWebSocket } from "../api/websocket";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  });

  // Gọi sau khi login/register thành công
  function login({ accessToken, refreshToken, user }) {
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
    localStorage.setItem("user", JSON.stringify(user));
    setUser(user);
  }

  function logout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    setUser(null);

    // Đóng kết nối WebSocket đang dùng JWT cũ — tránh trường hợp
    // người dùng login lại bằng tài khoản khác trên cùng tab mà
    // kết nối cũ vẫn còn treo, gây lẫn dữ liệu real-time giữa 2 user.
    disconnectWebSocket();
  }

  const value = {
    user,
    isAuthenticated: !!user,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// Custom hook để dùng trong component: const { user, login, logout } = useAuth();
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth phải được dùng trong AuthProvider");
  }
  return context;
}
