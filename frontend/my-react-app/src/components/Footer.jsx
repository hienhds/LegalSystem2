import React from "react";

export default function Footer() {
  return (
    <footer className="bg-white dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800">
      <div className="mx-auto max-w-7xl px-6 py-12">
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-4">
          <div className="lg:col-span-1">
            <div className="flex items-center gap-4 text-slate-900 dark:text-slate-50">
              <div className="size-8 text-custom-blue-dark">
                <svg fill="none" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg"><path d="M6 6H42L36 24L42 42H6L12 24L6 6Z" fill="currentColor"></path></svg>
              </div>
              <h2 className="text-slate-900 dark:text-slate-50 text-xl font-bold leading-tight">LegalConnect</h2>
            </div>
            <p className="mt-4 max-w-xs text-sm text-slate-600 dark:text-slate-400">Kết nối người dân với luật sư nhanh chóng và hiệu quả.</p>
          </div>
          <div className="grid grid-cols-2 gap-8 lg:col-span-3 lg:grid-cols-3">
            <div>
              <p className="font-semibold text-slate-900 dark:text-slate-100">Liên kết nhanh</p>
              <nav className="mt-4 flex flex-col space-y-2 text-sm text-slate-600 dark:text-slate-400">
                <a className="hover:opacity-75" href="#">Về chúng tôi</a>
                <a className="hover:opacity-75" href="#">Điều khoản sử dụng</a>
                <a className="hover:opacity-75" href="#">Chính sách bảo mật</a>
                <a className="hover:opacity-75" href="#">Hỗ trợ khách hàng</a>
              </nav>
            </div>
            <div>
              <p className="font-semibold text-slate-900 dark:text-slate-100">Thông tin liên hệ</p>
              <nav className="mt-4 flex flex-col space-y-2 text-sm text-slate-600 dark:text-slate-400">
                <a className="hover:opacity-75" href="mailto:contact@legalconnect.vn">contact@legalconnect.vn</a>
                <a className="hover:opacity-75" href="tel:0123456789">0123 456 789</a>
                <span className="hover:opacity-75">123 Đường ABC, Quận 1, TP. HCM</span>
              </nav>
            </div>
            <div>
              <p className="font-semibold text-slate-900 dark:text-slate-100">Mạng xã hội</p>
              <nav className="mt-4 flex flex-col space-y-2 text-sm text-slate-600 dark:text-slate-400">
                <a className="hover:opacity-75" href="#">Facebook</a>
                <a className="hover:opacity-75" href="#">LinkedIn</a>
                <a className="hover:opacity-75" href="#">Twitter</a>
              </nav>
            </div>
          </div>
        </div>
        <div className="mt-8 border-t border-slate-200 dark:border-slate-800 pt-6">
          <p className="text-center text-xs text-slate-500 dark:text-slate-400">© 2024 LegalConnect. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}
