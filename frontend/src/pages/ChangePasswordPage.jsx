import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";

/**
 * Trang đổi mật khẩu — yêu cầu nhập mật khẩu hiện tại để xác nhận.
 * Sau khi đổi thành công, thông báo xanh + redirect về /feed sau 2s.
 */
export default function ChangePasswordPage() {
 const navigate = useNavigate();

 const [form, setForm] = useState({
 currentPassword: "",
 newPassword: "",
 confirmPassword: "",
 });
 const [error, setError] = useState("");
 const [success, setSuccess] = useState(false);
 const [loading, setLoading] = useState(false);

 function handleChange(e) {
 setForm({ ...form, [e.target.name]: e.target.value });
 }

 async function handleSubmit(e) {
 e.preventDefault();
 setError("");

 // Validate frontend trước khi gọi API
 if (form.newPassword.length < 6) {
 setError("Mật khẩu mới phải có ít nhất 6 ký tự");
 return;
 }
 if (form.newPassword !== form.confirmPassword) {
 setError("Mật khẩu xác nhận không khớp");
 return;
 }
 if (form.currentPassword === form.newPassword) {
 setError("Mật khẩu mới phải khác mật khẩu hiện tại");
 return;
 }

 setLoading(true);
 try {
 await api.put("/users/me/password", form);
 setSuccess(true);
 setForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
 setTimeout(() => navigate("/feed"), 2000);
 } catch (err) {
 const message =
 err.response?.data?.message || "Đổi mật khẩu thất bại";
 setError(message);
 } finally {
 setLoading(false);
 }
 }

 return (
 <div className="flex h-screen flex-col bg-gray-50">
 <Navbar />

 <div className="flex flex-1 items-center justify-center p-4">
 <form
 onSubmit={handleSubmit}
 className="w-full max-w-sm space-y-4 rounded-xl bg-white p-8 shadow-md"
 >
 <h1 className="text-2xl font-bold text-gray-900">Đổi mật khẩu</h1>

 {error && (
 <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">
 {error}
 </div>
 )}

 {success && (
 <div className="rounded-md bg-green-50 px-3 py-2 text-sm text-green-700">
 Đổi mật khẩu thành công! Đang chuyển về trang chủ...
 </div>
 )}

 <div>
 <label className="mb-1 block text-sm font-medium text-gray-700">
 Mật khẩu hiện tại
 </label>
 <input
 type="password"
 name="currentPassword"
 value={form.currentPassword}
 onChange={handleChange}
 required
 className="w-full rounded-md border border-gray-300 px-3 py-2 outline-none focus:border-blue-500"
 />
 </div>

 <div>
 <label className="mb-1 block text-sm font-medium text-gray-700">
 Mật khẩu mới (tối thiểu 6 ký tự)
 </label>
 <input
 type="password"
 name="newPassword"
 value={form.newPassword}
 onChange={handleChange}
 required
 minLength={6}
 className="w-full rounded-md border border-gray-300 px-3 py-2 outline-none focus:border-blue-500"
 />
 </div>

 <div>
 <label className="mb-1 block text-sm font-medium text-gray-700">
 Xác nhận mật khẩu mới
 </label>
 <input
 type="password"
 name="confirmPassword"
 value={form.confirmPassword}
 onChange={handleChange}
 required
 className="w-full rounded-md border border-gray-300 px-3 py-2 outline-none focus:border-blue-500"
 />
 </div>

 <button
 type="submit"
 disabled={loading || success}
 className="w-full rounded-md bg-blue-600 px-4 py-2 font-medium text-white transition hover:bg-blue-700 disabled:opacity-50"
 >
 {loading ? "Đang đổi..." : "Đổi mật khẩu"}
 </button>

 <button
 type="button"
 onClick={() => navigate(-1)}
 className="w-full rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50"
 >
 Hủy
 </button>
 </form>
 </div>
 </div>
 );
}
