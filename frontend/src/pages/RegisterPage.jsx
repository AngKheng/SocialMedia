import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

export default function RegisterPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    displayName: "",
  });
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setFieldErrors({});
    setLoading(true);

    try {
      const res = await api.post("/auth/register", form);
      // res.data: { accessToken, refreshToken, tokenType, user }
      login(res.data);
      navigate("/feed");
    } catch (err) {
      // Validation lỗi (400) trả về { errors: { field: message } }
      if (err.response?.data?.errors) {
        setFieldErrors(err.response.data.errors);
      } else {
        const message =
          err.response?.data?.message || "Đăng ký thất bại, thử lại sau";
        setError(message);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex h-screen items-center justify-center bg-gray-50">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-sm space-y-4 rounded-xl bg-white p-8 shadow-md"
      >
        <h1 className="text-2xl font-bold text-gray-900">Tạo tài khoản</h1>

        {error && (
          <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">
            {error}
          </div>
        )}

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Username
          </label>
          <input
            type="text"
            name="username"
            value={form.username}
            onChange={handleChange}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 outline-none focus:border-blue-500"
          />
          {fieldErrors.username && (
            <p className="mt-1 text-xs text-red-600">{fieldErrors.username}</p>
          )}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Email
          </label>
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 outline-none focus:border-blue-500"
          />
          {fieldErrors.email && (
            <p className="mt-1 text-xs text-red-600">{fieldErrors.email}</p>
          )}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Mật khẩu
          </label>
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 outline-none focus:border-blue-500"
          />
          {fieldErrors.password && (
            <p className="mt-1 text-xs text-red-600">{fieldErrors.password}</p>
          )}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Tên hiển thị <span className="text-gray-400">(tùy chọn)</span>
          </label>
          <input
            type="text"
            name="displayName"
            value={form.displayName}
            onChange={handleChange}
            className="w-full rounded-md border border-gray-300 px-3 py-2 outline-none focus:border-blue-500"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-md bg-blue-600 px-4 py-2 font-medium text-white transition hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? "Đang tạo tài khoản..." : "Đăng ký"}
        </button>

        <p className="text-center text-sm text-gray-600">
          Đã có tài khoản?{" "}
          <Link to="/login" className="text-blue-600 hover:underline">
            Đăng nhập
          </Link>
        </p>
      </form>
    </div>
  );
}
