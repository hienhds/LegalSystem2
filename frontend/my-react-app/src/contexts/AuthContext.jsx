import React, { createContext, useContext, useEffect } from "react";
import { setupTokenRefreshTimer } from "../utils/api";
import TokenExpiredModal from "../components/TokenExpiredModal";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  useEffect(() => {
    // Setup token refresh nếu đã có token
    const token = localStorage.getItem("accessToken");
    if (token) {
      setupTokenRefreshTimer();
    }

    // Lắng nghe sự kiện login
    const handleLogin = () => {
      setupTokenRefreshTimer();
    };

    // Lắng nghe sự kiện logout
    const handleLogout = () => {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
    };

    window.addEventListener('user-login', handleLogin);
    window.addEventListener('user-logout', handleLogout);

    return () => {
      window.removeEventListener('user-login', handleLogin);
      window.removeEventListener('user-logout', handleLogout);
    };
  }, []);

  return (
    <AuthContext.Provider value={{}}>
      {children}
      <TokenExpiredModal />
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
