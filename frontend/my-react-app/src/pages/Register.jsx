import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Register.css";

export default function Register() {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setError("");
    if (password !== confirmPassword) {
      setError("Mật khẩu xác nhận không khớp!");
      return;
    }
    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, phoneNumber, fullName, password }),
      });
      const data = await res.json();

      if (!res.ok) {
        // Lấy message lỗi từ ApiResponse của Backend
        setError(data.message || "Đăng ký thất bại! Vui lòng kiểm tra lại dữ liệu.");
        return;
      }

      // Đăng ký thành công (ApiResponse.success = true)
      if (data.success) {
        setSuccess(true);
      }
    } catch (err) {
      setError("Không thể kết nối đến máy chủ. Vui lòng thử lại sau.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative flex min-h-screen w-full flex-col items-center justify-center bg-background-light dark:bg-background-dark group/design-root overflow-x-hidden font-display">
      <div className="flex w-full max-w-lg flex-col gap-6 p-4 md:p-8 bg-white dark:bg-background-dark rounded-xl shadow-lg">
        <div className="flex flex-col items-center gap-2 text-center">
          <span className="material-symbols-outlined text-primary text-5xl">balance</span>
          <p className="text-3xl font-bold text-slate-900 dark:text-white">Tạo tài khoản mới</p>
          <p className="text-base text-slate-500 dark:text-slate-400">Truy cập không giới hạn vào kho văn bản pháp luật của chúng tôi.</p>
        </div>
        {success ? (
          <div className="flex flex-col items-center gap-4 py-8">
            <span className="material-symbols-outlined text-green-500 text-5xl">mark_email_unread</span>
            <p className="text-xl font-semibold text-slate-800 dark:text-white">Đăng ký thành công!</p>
            <p className="text-base text-slate-600 dark:text-slate-400 text-center">Vui lòng kiểm tra email để xác thực tài khoản.<br />Nếu không thấy email, hãy kiểm tra cả mục Spam hoặc Thư rác.</p>
            <button className="mt-4 px-6 py-2 rounded-lg bg-primary text-white font-medium hover:bg-opacity-90" onClick={() => navigate("/")}>Quay lại đăng nhập</button>
          </div>
        ) : (
          <form className="flex flex-col gap-4" onSubmit={handleRegister}>
            <div className="flex flex-col">
              <label className="flex flex-col">
                <p className="pb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Họ và Tên</p>
                <input className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/20 dark:border-slate-700 dark:bg-background-dark dark:text-white dark:placeholder:text-slate-500" placeholder="Nhập họ và tên của bạn" value={fullName} onChange={e => setFullName(e.target.value)} required />
              </label>
            </div>
            <div className="flex flex-col">
              <label className="flex flex-col">
                <p className="pb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Địa chỉ Email</p>
                <input className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/20 dark:border-slate-700 dark:bg-background-dark dark:text-white dark:placeholder:text-slate-500" placeholder="nguyenvana@email.com" type="email" value={email} onChange={e => setEmail(e.target.value)} required />
              </label>
            </div>
            <div className="flex flex-col">
              <label className="flex flex-col">
                <p className="pb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Số điện thoại</p>
                <input className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/20 dark:border-slate-700 dark:bg-background-dark dark:text-white dark:placeholder:text-slate-500" placeholder="0123456789" type="tel" value={phoneNumber} onChange={e => setPhoneNumber(e.target.value)} required />
              </label>
            </div>
            <div className="flex flex-col">
              <label className="flex flex-col">
                <p className="pb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Mật khẩu</p>
                <div className="relative flex w-full items-center">
                  <input className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg border border-slate-300 bg-white px-4 py-3 pr-10 text-slate-900 placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/20 dark:border-slate-700 dark:bg-background-dark dark:text-white dark:placeholder:text-slate-500" placeholder="Nhập mật khẩu của bạn" type="password" value={password} onChange={e => setPassword(e.target.value)} required />
                </div>
              </label>
            </div>
            <div className="flex flex-col">
              <label className="flex flex-col">
                <p className="pb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Xác nhận mật khẩu</p>
                <div className="relative flex w-full items-center">
                  <input className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg border border-slate-300 bg-white px-4 py-3 pr-10 text-slate-900 placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/20 dark:border-slate-700 dark:bg-background-dark dark:text-white dark:placeholder:text-slate-500" placeholder="Nhập lại mật khẩu" type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
                </div>
              </label>
            </div>
            {error && <div className="text-red-500 text-sm text-center font-medium">{error}</div>}
            <div className="flex flex-col items-center gap-4">
              <button type="submit" className="flex h-12 w-full items-center justify-center rounded-lg bg-primary px-6 text-base font-medium text-white hover:bg-opacity-90 disabled:opacity-50" disabled={loading}>
                {loading ? "Đang đăng ký..." : "Đăng ký"}
              </button>
              <p className="text-sm text-slate-600 dark:text-slate-400">
                Đã có tài khoản?
                <span className="font-semibold text-primary hover:underline cursor-pointer ml-1" onClick={() => navigate("/")}>Đăng nhập</span>
              </p>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}