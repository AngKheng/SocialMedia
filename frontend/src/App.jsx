import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import FeedPage from "./pages/FeedPage";
import PostDetailPage from "./pages/PostDetailPage";
import ChatPage from "./pages/ChatPage";
import NotificationPage from "./pages/NotificationPage";
import ChangePasswordPage from "./pages/ChangePasswordPage";
import ProfilePage from "./pages/ProfilePage";
import FollowListPage from "./pages/FollowListPage";

export default function App() {
 return (
 <AuthProvider>
 <BrowserRouter>
 <Routes>
 {/* Public routes */}
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
 path="/posts/:id"
 element={
 <ProtectedRoute>
 <PostDetailPage />
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
 <Route
 path="/change-password"
 element={
 <ProtectedRoute>
 <ChangePasswordPage />
 </ProtectedRoute>
 }
 />
 <Route
 path="/profile/me"
 element={
 <ProtectedRoute>
 <ProfilePage />
 </ProtectedRoute>
 }
 />
 <Route
 path="/profile/:id"
 element={
 <ProtectedRoute>
 <ProfilePage />
 </ProtectedRoute>
 }
 />
 <Route
 path="/users/:id/followers"
 element={
 <ProtectedRoute>
 <FollowListPage />
 </ProtectedRoute>
 }
 />
 <Route
 path="/users/:id/following"
 element={
 <ProtectedRoute>
 <FollowListPage />
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
