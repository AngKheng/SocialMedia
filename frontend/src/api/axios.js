import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

// Tự động gắn accessToken vào mọi request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Nếu accessToken hết hạn (401) → tự động logout
// (Refresh token flow sẽ làm chi tiết hơn ở Phase 8B nếu cần)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;
