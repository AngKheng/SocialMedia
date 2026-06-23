/**
 * Chấm nhỏ báo online/offline.
 * Props:
 * - isOnline (bool)
 * - size (string, mặc định "h-3 w-3")
 * - className (string) — class bổ sung
 *
 * Dùng: <PresenceIndicator isOnline={true} /> → chấm xanh
 * <PresenceIndicator isOnline={false} /> → chấm xám
 */
export default function PresenceIndicator({
 isOnline,
 size = "h-3 w-3",
 className = "",
}) {
 return (
 <span
 title={isOnline ? "Đang online" : "Offline"}
 className={`inline-block flex-shrink-0 rounded-full border-2 border-white ${size} ${
 isOnline ? "bg-green-500" : "bg-gray-400"
 } ${className}`}
 />
 );
}