import { useState } from "react";
import api from "../api/axios";

/**
 * Form tạo bài viết mới.
 * Upload ảnh/video lên Cloudinary trước (qua /api/upload),
 * rồi gửi URL kèm content khi tạo post.
 *
 * Giới hạn: tối đa 4 ảnh HOẶC 1 video, không trộn lẫn.
 */
export default function CreatePostForm({ onPostCreated }) {
  const [content, setContent] = useState("");
  const [files, setFiles] = useState([]);
  const [previews, setPreviews] = useState([]);
  const [posting, setPosting] = useState(false);
  const [error, setError] = useState("");

  function handleFileChange(e) {
    const selected = Array.from(e.target.files);
    setError("");

    const hasVideo = selected.some((f) => f.type.startsWith("video/"));
    const hasImage = selected.some((f) => f.type.startsWith("image/"));

    if (hasVideo && hasImage) {
      setError("Không thể chọn ảnh và video cùng lúc");
      return;
    }
    if (hasVideo && selected.length > 1) {
      setError("Chỉ được chọn 1 video");
      return;
    }
    if (hasImage && selected.length > 4) {
      setError("Chỉ được chọn tối đa 4 ảnh");
      return;
    }

    setFiles(selected);
    setPreviews(selected.map((f) => URL.createObjectURL(f)));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!content.trim() && files.length === 0) return;

    setPosting(true);
    setError("");

    try {
      // Upload từng file lên Cloudinary trước
      const mediaUrls = [];
      for (const file of files) {
        const formData = new FormData();
        formData.append("file", file);
        const res = await api.post("/upload", formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
        mediaUrls.push(res.data.url);
      }

      // Tạo post với URL vừa upload
      const res = await api.post("/posts", { content, mediaUrls });

      // Reset form
      setContent("");
      setFiles([]);
      setPreviews([]);
      onPostCreated(res.data);
    } catch (err) {
      const message = err.response?.data?.message || "Đăng bài thất bại";
      setError(message);
    } finally {
      setPosting(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="rounded-xl border border-gray-200 bg-white p-4"
    >
      {error && (
        <div className="mb-2 rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">
          {error}
        </div>
      )}

      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="Bạn đang nghĩ gì?"
        rows={3}
        className="w-full resize-none rounded-md border border-gray-200 p-2 text-sm outline-none focus:border-blue-500"
      />

      {previews.length > 0 && (
        <div className="mt-2 grid grid-cols-2 gap-1">
          {previews.map((src, i) =>
            files[i].type.startsWith("video/") ? (
              <video key={i} src={src} className="rounded-lg" controls />
            ) : (
              <img key={i} src={src} alt="" className="rounded-lg" />
            )
          )}
        </div>
      )}

      <div className="mt-2 flex items-center justify-between">
        <label className="cursor-pointer text-sm text-blue-600 hover:underline">
          📷 Thêm ảnh/video
          <input
            type="file"
            accept="image/*,video/*"
            multiple
            onChange={handleFileChange}
            className="hidden"
          />
        </label>

        <button
          type="submit"
          disabled={posting || (!content.trim() && files.length === 0)}
          className="rounded-md bg-blue-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {posting ? "Đang đăng..." : "Đăng bài"}
        </button>
      </div>
    </form>
  );
}
