import React from "react";
import { Link, useNavigate } from "react-router-dom";
import useUserProfile from "../hooks/useUserProfile";

export default function Header() {
  const navigate = useNavigate();
  const accessToken = localStorage.getItem("accessToken");
  const { user } = useUserProfile();
  return (
    <header className="flex w-full flex-col bg-white dark:bg-slate-900 shadow-md sticky top-0 z-50">
      <div className="flex items-center justify-between whitespace-nowrap px-4 py-4">
        <div
          className="flex items-center gap-4 text-slate-900 dark:text-slate-50 cursor-pointer"
          onClick={() => navigate("/")}
        >
          <div className="size-8 text-custom-blue-dark">
            <svg
              fill="none"
              viewBox="0 0 48 48"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M6 6H42L36 24L42 42H6L12 24L6 6Z"
                fill="currentColor"
              ></path>
            </svg>
          </div>
          <h2 className="text-slate-900 dark:text-slate-50 text-xl font-bold leading-tight tracking-[-0.015em]">
            LegalConnect
          </h2>
        </div>
        <nav className="hidden md:flex items-center gap-6">
          <Link
            to="/find-lawyer"
            className="text-slate-900 dark:text-slate-200 hover:text-custom-blue-dark font-semibold text-base"
          >
            Luật Sư
          </Link>
          <a
            className="text-slate-900 dark:text-slate-200 hover:text-custom-blue-dark font-semibold text-base"
            href="#"
          >
            Hỏi Đáp
          </a>
          <Link
            to="/legal-documents"
            className="text-slate-900 dark:text-slate-200 hover:text-custom-blue-dark font-semibold text-base"
          >
            Văn Bản Pháp Luật
          </Link>
          <Link
            to="/contact"
            className="text-slate-900 dark:text-slate-200 hover:text-custom-blue-dark font-semibold text-base"
          >
            Liên Hệ
          </Link>
          <Link
            to="/cases"
            className="text-slate-900 dark:text-slate-200 hover:text-custom-blue-dark font-semibold text-base"
          >
            Hồ Sơ Vụ Án
          </Link>
          {user?.roles?.includes("ADMIN") && (
            <button
              onClick={() => navigate("/admin/dashboard")}
              className="flex items-center gap-2 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white px-4 py-2 rounded-lg font-semibold text-sm shadow-md transition-all duration-200"
            >
              <span className="material-symbols-outlined text-lg">
                admin_panel_settings
              </span>
              Admin Dashboard
            </button>
          )}
        </nav>
        <div className="flex items-center gap-2">
          <button className="flex min-w-[40px] max-w-[40px] cursor-pointer items-center justify-center overflow-hidden rounded-full h-10 w-10 bg-slate-100 dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm font-bold leading-normal tracking-[0.015em] hover:bg-slate-200 dark:hover:bg-slate-700">
            <span className="material-symbols-outlined">search</span>
          </button>
          {accessToken ? (
            <>
              <button
                className="hidden md:flex items-center justify-center cursor-pointer overflow-hidden rounded-full h-10 w-10 bg-slate-100 dark:bg-slate-800 border-2 border-white shadow-lg relative"
                onClick={() => {
                  if (user?.lawyerId) {
                    navigate("/lawyer-profile");
                  } else {
                    navigate("/profile");
                  }
                }}
                title={user?.fullName || "Profile"}
              >
                <img
                  src={
                    user?.avatarUrl
                      ? user.avatarUrl.startsWith("http")
                        ? user.avatarUrl
                        : `http://localhost:8080${user.avatarUrl}`
                      : "https://ui-avatars.com/api/?name=User&background=cccccc&color=222222&size=128"
                  }
                  alt="avatar"
                  className="object-cover w-full h-full rounded-full"
                />
              </button>
              <button
                className="hidden md:flex min-w-[84px] max-w-[480px] cursor-pointer items-center justify-center overflow-hidden rounded-lg h-10 px-4 bg-red-100 dark:bg-red-800 text-red-700 dark:text-red-50 text-sm font-bold leading-normal tracking-[0.015em] hover:bg-red-200 dark:hover:bg-red-700 ml-2"
                onClick={() => {
                  localStorage.removeItem("accessToken");
                  localStorage.removeItem("refreshToken");
                  window.dispatchEvent(new CustomEvent("user-logout"));
                  navigate("/login");
                }}
              >
                Đăng xuất
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="hidden md:flex min-w-[84px] max-w-[480px] cursor-pointer items-center justify-center overflow-hidden rounded-lg h-10 px-4 bg-slate-200 dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm font-bold leading-normal tracking-[0.015em] hover:bg-slate-300 dark:hover:bg-slate-700"
              >
                <span className="truncate">Đăng nhập</span>
              </Link>
              <Link
                to="/register"
                className="hidden md:flex min-w-[84px] max-w-[480px] cursor-pointer items-center justify-center overflow-hidden rounded-lg h-10 px-4 bg-slate-200 dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm font-bold leading-normal tracking-[0.015em] hover:bg-slate-300 dark:hover:bg-slate-700"
              >
                <span className="truncate">Đăng ký</span>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
