import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

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
        <Link to="/notifications" className="text-gray-600 hover:text-blue-600">
          Thông báo
        </Link>

        <span className="text-gray-400">|</span>

        <span className="font-medium text-gray-700">
          {user?.displayName || user?.username}
        </span>
        <button
          onClick={handleLogout}
          className="text-red-500 hover:underline"
        >
          Đăng xuất
        </button>
      </div>
    </nav>
  );
}
