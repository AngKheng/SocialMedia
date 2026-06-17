import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

// Các trang dưới đây là placeholder — sẽ làm chi tiết ở Phase 8B/8C/8D/8E
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import FeedPage from "./pages/FeedPage";
import ChatPage from "./pages/ChatPage";
import NotificationPage from "./pages/NotificationPage";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public routes — Phase 8B */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected routes — cần đăng nhập */}
          <Route
            path="/feed"
            element={
              <ProtectedRoute>
                <FeedPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/chat"
            element={
              <ProtectedRoute>
                <ChatPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <NotificationPage />
              </ProtectedRoute>
            }
          />

          {/* Mặc định: vào / thì chuyển tới /feed */}
          <Route path="/" element={<Navigate to="/feed" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
