import React, { useEffect, useState } from "react";

export default function TokenExpiredModal() {
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const handleTokenExpired = () => {
      setShowModal(true);
    };

    window.addEventListener('token-expired', handleTokenExpired);
    
    return () => {
      window.removeEventListener('token-expired', handleTokenExpired);
    };
  }, []);

  const handleLogin = () => {
    setShowModal(false);
    window.location.href = '/login';
  };

  if (!showModal) return null;

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-[9999]">
      <div className="bg-white dark:bg-slate-900 rounded-xl max-w-md w-full p-6 shadow-2xl animate-fade-in">
        <div className="flex flex-col items-center text-center">
          <div className="w-16 h-16 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center mb-4">
            <span className="material-symbols-outlined text-4xl text-red-600 dark:text-red-400">
              lock_clock
            </span>
          </div>
          
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-2">
            Phiên đăng nhập hết hạn
          </h2>
          
          <p className="text-slate-600 dark:text-slate-400 mb-6">
            Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại để tiếp tục sử dụng dịch vụ.
          </p>

          <button
            onClick={handleLogin}
            className="w-full px-6 py-3 rounded-lg bg-primary text-white hover:bg-primary/90 font-semibold transition-colors flex items-center justify-center gap-2"
          >
            <span className="material-symbols-outlined">login</span>
            Đăng nhập lại
          </button>
        </div>
      </div>
    </div>
  );
}
