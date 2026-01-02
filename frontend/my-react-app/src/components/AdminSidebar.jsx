import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function AdminSidebar() {
  const [adminInfo, setAdminInfo] = useState({
    fullName: "Admin",
    email: "admin@legalconnect.com",
    avatarUrl: null,
  });
  const navigate = useNavigate();

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const token = localStorage.getItem("accessToken");
        const response = await axios.get("http://localhost:8080/api/users/profile", {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (response.data.success) {
          const user = response.data.data;
          setAdminInfo({
            fullName: user.fullName || "Admin",
            email: user.email || "admin@legalconnect.com",
            avatarUrl: user.avatarUrl,
          });
        }
      } catch (error) {
        console.error("Error fetching admin profile:", error);
      }
    };
    
    loadProfile();
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.dispatchEvent(new CustomEvent('user-logout'));
    navigate("/login");
  };

  const getAvatarUrl = () => {
    if (adminInfo.avatarUrl) {
      return adminInfo.avatarUrl.startsWith("http")
        ? adminInfo.avatarUrl
        : `http://localhost:8080${adminInfo.avatarUrl}`;
    }
    const initials = adminInfo.fullName.split(" ").map(n => n[0]).join("");
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(initials)}&background=137fec&color=fff&size=128`;
  };

  return (
    <aside className="w-72 flex-shrink-0 bg-white dark:bg-gray-900/50 border-r border-slate-200 dark:border-slate-800 flex flex-col">
      {/* Logo Header */}
      <a href="/home" className="flex items-center gap-3 p-4 border-b border-slate-200 dark:border-slate-800 h-16 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors cursor-pointer">
        <div className="bg-primary rounded-lg p-1.5 text-white">
          <span className="material-symbols-outlined">gavel</span>
        </div>
        <h1 className="text-xl font-bold text-slate-900 dark:text-white">LegalConnect</h1>
      </a>

      {/* Navigation */}
      <div className="flex flex-col justify-between flex-1 p-3">
        <nav className="flex flex-col gap-1.5">
          <a className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 group" href="/admin/dashboard">
            <span className="material-symbols-outlined text-slate-500 dark:text-slate-400 group-hover:text-slate-700 dark:group-hover:text-slate-200">dashboard</span>
            <p className="text-slate-600 dark:text-slate-300 group-hover:text-slate-800 dark:group-hover:text-white text-sm font-medium tracking-wide">Tổng quan</p>
          </a>
          <a className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 group" href="/admin/users">
            <span className="material-symbols-outlined text-slate-500 dark:text-slate-400 group-hover:text-slate-700 dark:group-hover:text-slate-200">group</span>
            <p className="text-slate-600 dark:text-slate-300 group-hover:text-slate-800 dark:group-hover:text-white text-sm font-medium tracking-wide">Quản lý người dùng</p>
          </a>
          <a className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 group" href="/admin/lawyers">
            <span className="material-symbols-outlined text-slate-500 dark:text-slate-400 group-hover:text-slate-700 dark:group-hover:text-slate-200">balance</span>
            <p className="text-slate-600 dark:text-slate-300 group-hover:text-slate-800 dark:group-hover:text-white text-sm font-medium tracking-wide">Quản lý luật sư</p>
          </a>
          <a className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 group" href="/admin/documents">
            <span className="material-symbols-outlined text-slate-500 dark:text-slate-400 group-hover:text-slate-700 dark:group-hover:text-slate-200">folder_managed</span>
            <p className="text-slate-600 dark:text-slate-300 group-hover:text-slate-800 dark:group-hover:text-white text-sm font-medium tracking-wide">Quản lý nội dung</p>
          </a>
          <a className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 group" href="#">
            <span className="material-symbols-outlined text-slate-500 dark:text-slate-400 group-hover:text-slate-700 dark:group-hover:text-slate-200">flag</span>
            <p className="text-slate-600 dark:text-slate-300 group-hover:text-slate-800 dark:group-hover:text-white text-sm font-medium tracking-wide">Quản lý báo cáo & khiếu nại</p>
          </a>
          <a className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 group" href="#">
            <span className="material-symbols-outlined text-slate-500 dark:text-slate-400 group-hover:text-slate-700 dark:group-hover:text-slate-200">settings</span>
            <p className="text-slate-600 dark:text-slate-300 group-hover:text-slate-800 dark:group-hover:text-white text-sm font-medium tracking-wide">Quản lý hệ thống</p>
          </a>
        </nav>

        {/* Admin Profile */}
        <div className="flex flex-col gap-4 mt-4">
          <hr className="border-slate-200 dark:border-slate-800" />
          <div className="flex gap-3 items-center">
            <div
              className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-10"
              style={{
                backgroundImage: `url("${getAvatarUrl()}")`,
              }}
            ></div>
            <div className="flex flex-col">
              <h1 className="text-slate-900 dark:text-white text-sm font-medium leading-normal">{adminInfo.fullName}</h1>
              <p className="text-slate-500 dark:text-slate-400 text-xs font-normal leading-normal">{adminInfo.email}</p>
            </div>
            <button 
              onClick={handleLogout}
              className="ml-auto text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white"
            >
              <span className="material-symbols-outlined">logout</span>
            </button>
          </div>
        </div>
      </div>
    </aside>
  );
}
