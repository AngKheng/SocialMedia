import axios from "axios";

/**
 * axios instance chính — tất cả request API đều dùng instance này.
 * Base URL, JWT interceptor, refresh token interceptor (Phase 9J).
 */
const api = axios.create({
 baseURL: "http://localhost:8080/api",
});

// =============================================
// Refresh token state (Phase 9J)
// =============================================
// Dùng để tránh nhiều request cùng lúc đều gọi /auth/refresh.
// Khi 1 request 401, set _isRefreshing=true; các request khác cũng 401 sẽ
// chờ promise này resolve thay vì gọi refresh mới.
let _isRefreshing = false;
let _refreshSubscribers = [];

/**
 * Đăng ký 1 callback để được retry sau khi refresh xong.
 * Mỗi request đang chờ sẽ nhận accessToken mới → retry.
 */
function subscribeTokenRefresh(callback) {
 _refreshSubscribers.push(callback);
}

function onRefreshed(newAccessToken) {
 _refreshSubscribers.forEach((callback) => callback(newAccessToken));
 _refreshSubscribers = [];
}

function onRefreshFailed() {
 _refreshSubscribers.forEach((callback) => callback(null));
 _refreshSubscribers = [];
}

// =============================================
// Instance riêng cho refresh — KHÔNG có interceptor
// (tránh gọi refresh trong refresh → infinite loop)
// =============================================
const refreshClient = axios.create({
 baseURL: "http://localhost:8080/api",
});

// =============================================
// Request interceptor: gắn accessToken
// =============================================
api.interceptors.request.use((config) => {
 const token = localStorage.getItem("accessToken");
 if (token) {
 config.headers.Authorization = `Bearer ${token}`;
 }
 return config;
});

// =============================================
// Response interceptor: tự động refresh khi 401 (Phase 9J)
// =============================================
api.interceptors.response.use(
 (response) => response,
 async (error) => {
 const originalRequest = error.config;

 // Không có response (network error) hoặc không phải 401 → reject luôn
 if (!error.response || error.response.status !== 401) {
 return Promise.reject(error);
 }

 // Endpoint /auth/refresh fail → không retry, logout luôn
 if (originalRequest.url?.includes("/auth/refresh")) {
 handleLogout();
 return Promise.reject(error);
 }

 // Endpoint login/register fail → không retry
 if (
 originalRequest.url?.includes("/auth/login") ||
 originalRequest.url?.includes("/auth/register")
 ) {
 return Promise.reject(error);
 }

 const refreshToken = localStorage.getItem("refreshToken");
 if (!refreshToken) {
 handleLogout();
 return Promise.reject(error);
 }

 // Đánh dấu request này là đã retry (tránh loop vô tận)
 if (originalRequest._isRetry) {
 handleLogout();
 return Promise.reject(error);
 }

 // Nếu đang refresh → chờ kết quả rồi retry với token mới
 if (_isRefreshing) {
 return new Promise((resolve, reject) => {
 subscribeTokenRefresh((newToken) => {
 if (!newToken) {
 reject(error);
 return;
 }
 originalRequest.headers.Authorization = `Bearer ${newToken}`;
 originalRequest._isRetry = true;
 resolve(api(originalRequest));
 });
 });
 }

 // Bắt đầu refresh
 _isRefreshing = true;
 originalRequest._isRetry = true;

 try {
 const res = await refreshClient.post("/auth/refresh", {
 refreshToken,
 });

 const { accessToken: newAccessToken, refreshToken: newRefreshToken } = res.data;

 // Lưu token mới vào localStorage
 localStorage.setItem("accessToken", newAccessToken);
 localStorage.setItem("refreshToken", newRefreshToken);

 _isRefreshing = false;
 onRefreshed(newAccessToken);

 // Retry request ban đầu với token mới
 originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
 return api(originalRequest);
 } catch (refreshErr) {
 _isRefreshing = false;
 onRefreshFailed();
 handleLogout();
 return Promise.reject(refreshErr);
 }
 }
);

function handleLogout() {
 localStorage.removeItem("accessToken");
 localStorage.removeItem("refreshToken");
 localStorage.removeItem("user");
 // Tránh loop nếu đang ở trang login
 if (!window.location.pathname.startsWith("/login")) {
 window.location.href = "/login";
 }
}

export default api;