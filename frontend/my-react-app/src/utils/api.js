// Promise để tránh gọi refresh token song song
let refreshPromise = null;
let tokenExpiryTimer = null;

// Decode JWT để lấy thời gian hết hạn
function decodeToken(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => 
      '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

// Kiểm tra xem token có hết hạn không
export function isTokenExpired(token) {
  if (!token) return true;
  const decoded = decodeToken(token);
  if (!decoded || !decoded.exp) return true;
  return Date.now() >= decoded.exp * 1000;
}

// Lấy thời gian còn lại của token (ms)
function getTokenTimeRemaining(token) {
  if (!token) return 0;
  const decoded = decodeToken(token);
  if (!decoded || !decoded.exp) return 0;
  return Math.max(0, decoded.exp * 1000 - Date.now());
}

// Refresh token trước khi hết hạn
export async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) {
    throw new Error("No refresh token available");
  }

  if (refreshPromise) {
    return refreshPromise;
  }

  refreshPromise = (async () => {
    try {
      const refreshRes = await fetch("http://localhost:8080/api/auth/refresh", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      });

      if (refreshRes.ok) {
        const refreshData = await refreshRes.json();
        // Backend returns JwtResponse directly (not wrapped in ApiResponse)
        if (refreshData.accessToken) {
          localStorage.setItem("accessToken", refreshData.accessToken);
          if (refreshData.refreshToken) {
            localStorage.setItem("refreshToken", refreshData.refreshToken);
          }
          // Setup timer để refresh lại trước khi token mới hết hạn
          setupTokenRefreshTimer();
          return refreshData.accessToken;
        }
      }
      throw new Error("Refresh token failed");
    } catch (error) {
      // Clear tokens và dispatch event để components biết
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      window.dispatchEvent(new CustomEvent('token-expired'));
      throw error;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

// Setup timer để auto refresh token trước 5 phút khi hết hạn
export function setupTokenRefreshTimer() {
  if (tokenExpiryTimer) {
    clearTimeout(tokenExpiryTimer);
  }

  const accessToken = localStorage.getItem("accessToken");
  if (!accessToken) return;

  const timeRemaining = getTokenTimeRemaining(accessToken);
  // Refresh trước 5 phút (300000ms) khi sắp hết hạn
  const refreshTime = Math.max(0, timeRemaining - 300000);

  if (refreshTime > 0) {
    tokenExpiryTimer = setTimeout(async () => {
      try {
        await refreshAccessToken();
      } catch (error) {
        console.error('Auto refresh failed:', error);
      }
    }, refreshTime);
  } else if (timeRemaining > 0) {
    // Token sắp hết hạn, refresh ngay
    refreshAccessToken().catch(console.error);
  }
}

// Kiểm tra token khi app khởi động
export function checkTokenOnStartup() {
  const accessToken = localStorage.getItem("accessToken");
  const refreshToken = localStorage.getItem("refreshToken");

  if (!accessToken || !refreshToken) {
    return false;
  }

  // Nếu access token hết hạn nhưng còn refresh token
  if (isTokenExpired(accessToken)) {
    if (!isTokenExpired(refreshToken)) {
      // Thử refresh ngay
      refreshAccessToken().catch(() => {
        return false;
      });
      return true;
    }
    // Cả 2 đều hết hạn
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    return false;
  }

  // Token còn hạn, setup auto refresh
  setupTokenRefreshTimer();
  return true;
}

// Wrapper cho các request API, tự động gắn token và xử lý refresh
export async function apiFetch(url, options = {}, retry = true) {
  const accessToken = localStorage.getItem("accessToken");
  const refreshToken = localStorage.getItem("refreshToken");
  
  options.headers = options.headers || {};
  if (accessToken) {
    options.headers["Authorization"] = `Bearer ${accessToken}`;
  }
  
  let res = await fetch(url, options);
  
  // Nếu token hết hạn, thử refresh
  if (res.status === 401 && refreshToken && retry) {
    try {
      const newAccessToken = await refreshAccessToken();
      // Thử lại request với token mới
      options.headers["Authorization"] = `Bearer ${newAccessToken}`;
      res = await fetch(url, options);
    } catch (error) {
      // Dispatch event để hiển thị modal đăng nhập
      window.dispatchEvent(new CustomEvent('token-expired'));
      throw new Error("Token expired, please login again.");
    }
  }
  
  return res;
}
