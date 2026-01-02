import React, { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";


export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const tokenFromUrl = searchParams.get("token") || "";
  const [token, setToken] = useState(tokenFromUrl);
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!token) {
      setError("Vui lòng nhập mã xác thực (token)!");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("Mật khẩu xác nhận không khớp.");
      return;
    }
    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/api/auth/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token, newPassword }),
      });
      const data = await res.json();
      if (!res.ok || data.success === false) {
        setError(data.message || "Mã xác thực không đúng hoặc đã hết hạn.");
        return;
      }
      setSuccess(true);
    } catch {
      setError("Có lỗi xảy ra. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-background-light dark:bg-background-dark font-display">
      <div className="flex flex-col items-center gap-6 bg-white dark:bg-background-dark p-8 rounded-xl shadow-lg max-w-lg w-full">
        <span className="material-symbols-outlined text-primary text-5xl">vpn_key</span>
        <p className="text-2xl font-bold text-slate-900 dark:text-white text-center">Đặt lại mật khẩu</p>
        {success ? (
          <>
            <p className="text-base text-slate-600 dark:text-slate-400 text-center">
              Mật khẩu đã được đặt lại thành công!<br />Bạn có thể đăng nhập bằng mật khẩu mới.
            </p>
            <button className="mt-4 px-6 py-2 rounded-lg bg-primary text-white font-semibold hover:bg-primary/90" onClick={() => navigate("/")}>Quay lại đăng nhập</button>
          </>
        ) : (
          <form className="w-full flex flex-col gap-4" onSubmit={handleSubmit}>
            <label className="flex flex-col">
              <p className="pb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Mã xác thực (token)</p>
              <input
                className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/20 dark:border-slate-700 dark:bg-background-dark dark:text-white dark:placeholder:text-slate-500"
                placeholder="Nhập mã xác thực từ email"
                type="text"
                value={token}
                onChange={e => setToken(e.target.value)}
                required
              />
            </label>
            <label className="flex flex-col">
              <p className="pb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Mật khẩu mới</p>
              <input
                className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/20 dark:border-slate-700 dark:bg-background-dark dark:text-white dark:placeholder:text-slate-500"
                placeholder="Nhập mật khẩu mới"
                type="password"
                value={newPassword}
                onChange={e => setNewPassword(e.target.value)}
                required
              />
            </label>
            <label className="flex flex-col">
              <p className="pb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Xác nhận mật khẩu mới</p>
              <input
                className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/20 dark:border-slate-700 dark:bg-background-dark dark:text-white dark:placeholder:text-slate-500"
                placeholder="Nhập lại mật khẩu mới"
                type="password"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                required
              />
            </label>
            {error && <div className="text-red-500 text-sm text-center">{error}</div>}
            <button type="submit" className="flex h-12 w-full items-center justify-center rounded-lg bg-primary px-6 text-base font-medium text-white hover:bg-opacity-90" disabled={loading}>
              {loading ? "Đang đặt lại mật khẩu..." : "Đặt lại mật khẩu"}
            </button>
            <button type="button" className="text-sm text-primary mt-2" onClick={() => navigate("/")}>Quay lại đăng nhập</button>
          </form>
        )}
      </div>
    </div>
  );
}
