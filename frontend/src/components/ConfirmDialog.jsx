import Modal from "./Modal";

/**
 * Hộp thoại xác nhận (dùng lại Modal làm wrapper).
 * Props:
 * - isOpen (bool)
 * - title (string)
 * - message (string)
 * - confirmText (string, mặc định "Xác nhận")
 * - cancelText (string, mặc định "Hủy")
 * - onConfirm (fn) — bấm nút confirm
 * - onClose (fn) — bấm nút cancel / ESC / backdrop
 * - danger (bool, mặc định true) — true = nút confirm màu đỏ
 */
export default function ConfirmDialog({
 isOpen,
 title,
 message,
 confirmText = "Xác nhận",
 cancelText = "Hủy",
 onConfirm,
 onClose,
 danger = true,
}) {
 return (
 <Modal isOpen={isOpen} onClose={onClose} title={title}>
 <p className="mb-5 text-sm text-gray-700">{message}</p>

 <div className="flex justify-end gap-2">
 <button
 type="button"
 onClick={onClose}
 className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
 >
 {cancelText}
 </button>
 <button
 type="button"
 onClick={onConfirm}
 className={`rounded-md px-3 py-1.5 text-sm font-medium text-white ${
 danger
 ? "bg-red-600 hover:bg-red-700"
 : "bg-blue-600 hover:bg-blue-700"
}`}
 >
 {confirmText}
 </button>
 </div>
 </Modal>
 );
}