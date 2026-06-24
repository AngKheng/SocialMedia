/**
 * Hiển thị avatar của user:
 * - Nếu có avatarUrl → hiển thị ảnh
 * - Nếu không có     → hiển thị 2 chữ cái đầu (initials)
 *
 * Props:
 *   user      : { displayName, username, avatarUrl }
 *   size      : "sm" | "md" | "lg" | "xl"  (default: "md")
 *   editable  : boolean – hiện icon camera khi hover để chỉnh sửa
 *   onClick   : function – gọi khi click vào avatar
 *   className : string tùy chọn thêm
 */
export default function UserAvatar({
  user,
  size = "md",
  editable = false,
  onClick,
  className = "",
}) {
  const name = user?.displayName || user?.username || "?";
  const initials = name.slice(0, 2).toUpperCase();
  const avatarUrl = user?.avatarUrl;

  const sizeMap = {
    sm: { outer: "h-6 w-6", text: "text-[10px]" },
    md: { outer: "h-9 w-9", text: "text-sm" },
    lg: { outer: "h-10 w-10", text: "text-sm" },
    xl: { outer: "h-20 w-20", text: "text-xl" },
  };
  const { outer, text } = sizeMap[size] || sizeMap.md;

  const isClickable = editable || !!onClick;

  const wrapperClass = [
    "relative flex-shrink-0",
    isClickable ? "cursor-pointer" : "",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <div className={wrapperClass} onClick={onClick}>
      {avatarUrl ? (
        <img
          src={avatarUrl}
          alt={name}
          className={`${outer} rounded-full object-cover`}
        />
      ) : (
        <div
          className={`${outer} ${text} flex items-center justify-center rounded-full bg-blue-100 font-medium text-blue-700`}
        >
          {initials}
        </div>
      )}

      {/* Overlay camera khi editable */}
      {editable && (
        <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 transition-opacity hover:opacity-100">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="white"
            strokeWidth={1.8}
            strokeLinecap="round"
            strokeLinejoin="round"
            className="h-5 w-5"
          >
            <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
            <circle cx="12" cy="13" r="4" />
          </svg>
        </div>
      )}
    </div>
  );
}
