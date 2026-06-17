import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * Bọc quanh các route cần đăng nhập.
 * Chưa login → tự động chuyển về /login.
 *
 * Cách dùng:
 * <Route path="/feed" element={
 *   <ProtectedRoute><FeedPage /></ProtectedRoute>
 * } />
 */
export default function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
