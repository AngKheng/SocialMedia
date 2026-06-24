import { useState, useRef, useCallback, useEffect } from "react";
import api from "../api/axios";

/**
 * Modal crop avatar hình tròn.
 *
 * Props:
 *   onClose()           – đóng modal
 *   onSaved(avatarUrl)  – gọi sau khi upload + cập nhật profile thành công
 */
export default function AvatarCropModal({ onClose, onSaved }) {
  const [imgSrc, setImgSrc] = useState(null);
  const [dragging, setDragging] = useState(false);
  const [scale, setScale] = useState(1);
  const [offset, setOffset] = useState({ x: 0, y: 0 });
  const [dragStart, setDragStart] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const canvasRef = useRef(null);
  const previewCanvasRef = useRef(null);
  const imgRef = useRef(null);
  const fileInputRef = useRef(null);

  const CANVAS_SIZE = 300; // px - kích thước vùng crop hiển thị
  const CROP_RADIUS = 130; // px - bán kính vòng tròn crop

  // ── Vẽ canvas preview ──────────────────────────────────────────────
  const draw = useCallback(() => {
    const canvas = canvasRef.current;
    const img = imgRef.current;
    if (!canvas || !img) return;

    const ctx = canvas.getContext("2d");
    const cx = CANVAS_SIZE / 2;
    const cy = CANVAS_SIZE / 2;

    ctx.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

    // Vẽ ảnh với scale + offset
    const drawW = img.naturalWidth * scale;
    const drawH = img.naturalHeight * scale;
    const drawX = cx + offset.x - drawW / 2;
    const drawY = cy + offset.y - drawH / 2;
    ctx.drawImage(img, drawX, drawY, drawW, drawH);

    // Overlay tối bên ngoài vòng tròn
    ctx.save();
    ctx.fillStyle = "rgba(0,0,0,0.52)";
    ctx.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
    ctx.globalCompositeOperation = "destination-out";
    ctx.beginPath();
    ctx.arc(cx, cy, CROP_RADIUS, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();

    // Viền tròn trắng
    ctx.save();
    ctx.strokeStyle = "rgba(255,255,255,0.85)";
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.arc(cx, cy, CROP_RADIUS, 0, Math.PI * 2);
    ctx.stroke();
    ctx.restore();
  }, [imgSrc, scale, offset]);

  useEffect(() => { draw(); }, [draw]);

  // ── Chọn file ──────────────────────────────────────────────────────
  function handleFileChange(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setError("");

    if (!file.type.startsWith("image/")) {
      setError("Chỉ chấp nhận file ảnh (jpg, png, webp, gif)");
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      setError("Ảnh không được vượt quá 10MB");
      return;
    }

    const reader = new FileReader();
    reader.onload = (ev) => {
      const src = ev.target.result;
      setImgSrc(src);
      setOffset({ x: 0, y: 0 });
      setScale(1);

      // Khi ảnh load xong, auto-fit vào canvas
      const img = new Image();
      img.onload = () => {
        imgRef.current = img;
        const fitScale = Math.max(
          (CROP_RADIUS * 2) / img.naturalWidth,
          (CROP_RADIUS * 2) / img.naturalHeight
        );
        setScale(fitScale * 1.05); // hơi zoom ra 1 chút
      };
      img.src = src;
    };
    reader.readAsDataURL(file);
  }

  // ── Drag để di chuyển ảnh ──────────────────────────────────────────
  function onMouseDown(e) {
    e.preventDefault();
    setDragging(true);
    setDragStart({ x: e.clientX - offset.x, y: e.clientY - offset.y });
  }

  function onMouseMove(e) {
    if (!dragging || !dragStart) return;
    setOffset({ x: e.clientX - dragStart.x, y: e.clientY - dragStart.y });
  }

  function onMouseUp() { setDragging(false); }

  // Touch support
  function onTouchStart(e) {
    const t = e.touches[0];
    setDragging(true);
    setDragStart({ x: t.clientX - offset.x, y: t.clientY - offset.y });
  }
  function onTouchMove(e) {
    if (!dragging || !dragStart) return;
    const t = e.touches[0];
    setOffset({ x: t.clientX - dragStart.x, y: t.clientY - dragStart.y });
  }
  function onTouchEnd() { setDragging(false); }

  // ── Scroll để zoom ─────────────────────────────────────────────────
  function onWheel(e) {
    e.preventDefault();
    setScale((s) => Math.min(10, Math.max(0.1, s - e.deltaY * 0.001)));
  }

  // ── Export vùng tròn ra Blob rồi upload ────────────────────────────
  async function handleSave() {
    if (!imgRef.current) return;
    setSaving(true);
    setError("");

    try {
      // Vẽ kết quả crop lên canvas tạm (output 400x400)
      const OUT = 400;
      const out = document.createElement("canvas");
      out.width = OUT;
      out.height = OUT;
      const ctx = out.getContext("2d");

      // Clip hình tròn
      ctx.beginPath();
      ctx.arc(OUT / 2, OUT / 2, OUT / 2, 0, Math.PI * 2);
      ctx.clip();

      // Scale từ canvas preview → canvas output
      const ratio = OUT / CANVAS_SIZE;
      const cx = CANVAS_SIZE / 2;
      const cy = CANVAS_SIZE / 2;
      const img = imgRef.current;
      const drawW = img.naturalWidth * scale;
      const drawH = img.naturalHeight * scale;
      const drawX = cx + offset.x - drawW / 2;
      const drawY = cy + offset.y - drawH / 2;

      ctx.drawImage(img, drawX * ratio, drawY * ratio, drawW * ratio, drawH * ratio);

      // Canvas → Blob → FormData → upload
      const blob = await new Promise((res) =>
        out.toBlob(res, "image/jpeg", 0.92)
      );

      const formData = new FormData();
      formData.append("file", blob, "avatar.jpg");

      const uploadRes = await api.post("/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      const avatarUrl = uploadRes.data.url;

      // Cập nhật profile
      await api.put("/users/me", { avatarUrl });

      onSaved(avatarUrl);
    } catch (err) {
      setError(
        err.response?.data?.message || "Lưu ảnh thất bại, vui lòng thử lại"
      );
    } finally {
      setSaving(false);
    }
  }

  // ── Render ─────────────────────────────────────────────────────────
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="w-full max-w-sm rounded-2xl bg-white shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-100 px-5 py-4">
          <h2 className="text-base font-semibold text-gray-900">
            Cập nhật ảnh đại diện
          </h2>
          <button
            onClick={onClose}
            className="flex h-8 w-8 items-center justify-center rounded-full text-gray-400 hover:bg-gray-100 hover:text-gray-600"
          >
            ✕
          </button>
        </div>

        {/* Body */}
        <div className="px-5 py-5">
          {!imgSrc ? (
            /* Vùng chọn file */
            <button
              onClick={() => fileInputRef.current?.click()}
              className="flex w-full flex-col items-center gap-3 rounded-xl border-2 border-dashed border-gray-200 bg-gray-50 py-10 text-center transition hover:border-blue-400 hover:bg-blue-50"
            >
              <span className="text-4xl">🖼️</span>
              <div>
                <p className="text-sm font-medium text-gray-700">
                  Nhấn để chọn ảnh
                </p>
                <p className="mt-0.5 text-xs text-gray-400">
                  JPG, PNG, WEBP — tối đa 10MB
                </p>
              </div>
            </button>
          ) : (
            /* Canvas crop */
            <div className="flex flex-col items-center gap-4">
              {/* Hướng dẫn */}
              <p className="text-xs text-gray-500">
                Kéo ảnh để điều chỉnh vị trí · Cuộn chuột để zoom
              </p>

              {/* Canvas */}
              <div className="relative overflow-hidden rounded-xl bg-gray-900">
                <canvas
                  ref={canvasRef}
                  width={CANVAS_SIZE}
                  height={CANVAS_SIZE}
                  className="block"
                  style={{ cursor: dragging ? "grabbing" : "grab", touchAction: "none" }}
                  onMouseDown={onMouseDown}
                  onMouseMove={onMouseMove}
                  onMouseUp={onMouseUp}
                  onMouseLeave={onMouseUp}
                  onWheel={onWheel}
                  onTouchStart={onTouchStart}
                  onTouchMove={onTouchMove}
                  onTouchEnd={onTouchEnd}
                />
              </div>

              {/* Slider zoom */}
              <div className="flex w-full items-center gap-3">
                <span className="text-xs text-gray-400">Thu nhỏ</span>
                <input
                  type="range"
                  min="0.1"
                  max="5"
                  step="0.01"
                  value={scale}
                  onChange={(e) => setScale(parseFloat(e.target.value))}
                  className="flex-1 accent-blue-600"
                />
                <span className="text-xs text-gray-400">Phóng to</span>
              </div>

              {/* Đổi ảnh khác */}
              <button
                onClick={() => fileInputRef.current?.click()}
                className="text-xs text-blue-600 hover:underline"
              >
                Chọn ảnh khác
              </button>
            </div>
          )}

          {/* Input file ẩn */}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className="hidden"
            onChange={handleFileChange}
          />

          {/* Lỗi */}
          {error && (
            <p className="mt-3 text-center text-xs text-red-500">{error}</p>
          )}
        </div>

        {/* Footer */}
        <div className="flex gap-2 border-t border-gray-100 px-5 py-4">
          <button
            onClick={onClose}
            className="flex-1 rounded-lg border border-gray-200 py-2 text-sm text-gray-600 hover:bg-gray-50"
          >
            Huỷ
          </button>
          <button
            onClick={handleSave}
            disabled={!imgSrc || saving}
            className="flex-1 rounded-lg bg-blue-600 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-40"
          >
            {saving ? "Đang lưu..." : "Lưu ảnh đại diện"}
          </button>
        </div>
      </div>
    </div>
  );
}
