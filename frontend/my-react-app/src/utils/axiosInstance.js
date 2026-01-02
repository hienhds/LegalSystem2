import axios from "axios";
import { refreshAccessToken } from "./api";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor: Gắn token vào header
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: Xử lý bóc tách ApiResponse và tự động refresh token
axiosInstance.interceptors.response.use(
  (response) => {
    // Nếu Backend trả về ApiResponse (success, status, data...), ta trả về phần data thực sự
    if (response.data && Object.prototype.hasOwnProperty.call(response.data, 'success')) {
      return response.data;
    }
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // Xử lý lỗi 401 Unauthorized
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const newAccessToken = await refreshAccessToken();
        if (newAccessToken) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return axiosInstance(originalRequest);
        }
      } catch (refreshError) {
        console.error("Token refresh failed:", refreshError);
        localStorage.clear();
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }

    // Trả về error response data để component có thể đọc được message lỗi từ Backend
    return Promise.reject(error.response?.data || error);
  }
);

export default axiosInstance;