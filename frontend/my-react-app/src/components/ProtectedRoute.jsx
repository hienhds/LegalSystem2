import React from "react";
import { Navigate } from "react-router-dom";
import { isTokenExpired } from "../utils/api";

export default function ProtectedRoute({ children }) {
  const accessToken = localStorage.getItem("accessToken");
  const refreshToken = localStorage.getItem("refreshToken");
  
  // Nếu không có token hoặc cả 2 token đều hết hạn
  if (!accessToken || !refreshToken) {
    return <Navigate to="/login" replace />;
  }
  
  // Nếu access token hết hạn và refresh token cũng hết hạn
  if (isTokenExpired(accessToken) && isTokenExpired(refreshToken)) {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    return <Navigate to="/login" replace />;
  }
  
  return children;
}
