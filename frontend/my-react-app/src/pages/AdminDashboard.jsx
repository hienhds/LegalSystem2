import React, { useState, useEffect } from "react";
import axios from "axios";
import AdminSidebar from "../components/AdminSidebar";

export default function AdminDashboard() {
  const [stats, setStats] = useState({
    totalUsers: 0,
    userGrowth: 0,
    totalLawyers: 0,
    lawyerGrowth: 0,
    totalQuestions: 0,
    questionGrowth: 0,
    totalLegalDocs: 0,
    legalDocGrowth: 0,
  });
  
  const [registrations, setRegistrations] = useState([]);
  const [verificationStatus, setVerificationStatus] = useState({
    verified: 0,
    pending: 0,
    rejected: 0,
    total: 0,
  });
  
  const [loading, setLoading] = useState(true);
  const [registrationDays, setRegistrationDays] = useState(7);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem("accessToken");
        const config = {
          headers: { Authorization: `Bearer ${token}` },
        };

        // Fetch all data in parallel
        const [statsRes, registrationsRes, verificationRes] = await Promise.all([
          axios.get("http://localhost:8080/api/admin/dashboard/stats", config),
          axios.get(`http://localhost:8080/api/admin/dashboard/registrations?days=${registrationDays}`, config),
          axios.get("http://localhost:8080/api/admin/dashboard/lawyer-verification-status", config),
        ]);

        if (statsRes.data.success) {
          setStats(statsRes.data.data);
        }

        if (registrationsRes.data.success) {
          const data = registrationsRes.data.data;
          // Transform API data to chart format
          const chartData = data.labels.map((label, index) => ({
            date: label,
            lawyerCount: data.lawyers[index],
            userCount: data.users[index]
          }));
          setRegistrations(chartData);
        }

        if (verificationRes.data.success) {
          const data = verificationRes.data.data;
          setVerificationStatus(data);
        }
      } catch (error) {
        console.error("Error fetching dashboard data:", error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchDashboardData();
  }, [registrationDays]);

  const formatGrowth = (growth) => {
    if (growth === null || growth === undefined) return "N/A";
    const sign = growth >= 0 ? "+" : "";
    return `${sign}${growth.toFixed(1)}%`;
  };

  const getGrowthColor = (growth) => {
    if (growth === null || growth === undefined) return "text-slate-500 dark:text-slate-400";
    return growth >= 0 ? "text-green-600 dark:text-green-500" : "text-red-600 dark:text-red-500";
  };

  // Calculate percentages for verification chart
  const verifiedPercent = verificationStatus.total > 0 
    ? ((verificationStatus.verified / verificationStatus.total) * 100).toFixed(0)
    : 0;
  const pendingPercent = verificationStatus.total > 0
    ? ((verificationStatus.pending / verificationStatus.total) * 100).toFixed(0)
    : 0;
  const rejectedPercent = verificationStatus.total > 0
    ? ((verificationStatus.rejected / verificationStatus.total) * 100).toFixed(0)
    : 0;

  // Calculate stroke dasharray for donut chart
  const circumference = 100; // 2 * PI * 15.9155
  const verifiedDash = (verifiedPercent / 100) * circumference;
  const pendingDash = (pendingPercent / 100) * circumference;
  const rejectedDash = (rejectedPercent / 100) * circumference;

  if (loading) {
    return (
      <div className="bg-background-light dark:bg-background-dark min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-slate-600 dark:text-slate-400">Đang tải dữ liệu...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-background-light dark:bg-background-dark min-h-screen flex">
      <AdminSidebar />

      {/* Main Content */}
      <main className="flex-1 p-6 lg:p-8 overflow-y-auto">
        <div className="max-w-7xl mx-auto">
          {/* Header */}
          <div className="flex flex-wrap justify-between items-center gap-3 mb-6">
            <h1 className="text-slate-900 dark:text-white text-3xl font-bold tracking-tight">Tổng quan</h1>
            <div className="flex items-center gap-2">
              <button 
                onClick={() => setRegistrationDays(7)}
                className={`flex h-9 shrink-0 items-center justify-center gap-x-2 rounded-lg px-3 ${
                  registrationDays === 7 
                    ? 'bg-primary text-white' 
                    : 'bg-white dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800'
                }`}
              >
                <p className="text-sm font-medium">7 ngày</p>
              </button>
              <button 
                onClick={() => setRegistrationDays(30)}
                className={`flex h-9 shrink-0 items-center justify-center gap-x-2 rounded-lg px-3 ${
                  registrationDays === 30 
                    ? 'bg-primary text-white' 
                    : 'bg-white dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800'
                }`}
              >
                <p className="text-sm font-medium">30 ngày</p>
              </button>
              <button className="flex h-9 shrink-0 items-center justify-center gap-x-2 rounded-lg bg-primary text-white pl-4 pr-3 hover:bg-primary/90">
                <p className="text-sm font-medium">Làm mới</p>
                <span className="material-symbols-outlined text-base">refresh</span>
              </button>
            </div>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <div className="flex flex-col gap-2 rounded-xl p-5 bg-white dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800">
              <p className="text-slate-600 dark:text-slate-400 text-sm font-medium">Tổng số người dùng</p>
              <p className="text-slate-900 dark:text-white text-3xl font-bold">{stats.totalUsers?.toLocaleString() || 0}</p>
              <p className={`text-sm font-medium ${getGrowthColor(stats.userGrowth)}`}>{formatGrowth(stats.userGrowth)}</p>
            </div>
            <div className="flex flex-col gap-2 rounded-xl p-5 bg-white dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800">
              <p className="text-slate-600 dark:text-slate-400 text-sm font-medium">Tổng số luật sư</p>
              <p className="text-slate-900 dark:text-white text-3xl font-bold">{stats.totalLawyers?.toLocaleString() || 0}</p>
              <p className={`text-sm font-medium ${getGrowthColor(stats.lawyerGrowth)}`}>{formatGrowth(stats.lawyerGrowth)}</p>
            </div>
            <div className="flex flex-col gap-2 rounded-xl p-5 bg-white dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800">
              <p className="text-slate-600 dark:text-slate-400 text-sm font-medium">Tổng số câu hỏi</p>
              <p className="text-slate-900 dark:text-white text-3xl font-bold">{stats.totalQuestions?.toLocaleString() || 'N/A'}</p>
              <p className={`text-sm font-medium ${getGrowthColor(stats.questionGrowth)}`}>{formatGrowth(stats.questionGrowth)}</p>
            </div>
            <div className="flex flex-col gap-2 rounded-xl p-5 bg-white dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800">
              <p className="text-slate-600 dark:text-slate-400 text-sm font-medium">Văn bản pháp luật</p>
              <p className="text-slate-900 dark:text-white text-3xl font-bold">{stats.totalLegalDocs?.toLocaleString() || 0}</p>
              <p className={`text-sm font-medium ${getGrowthColor(stats.legalDocGrowth)}`}>{formatGrowth(stats.legalDocGrowth)}</p>
            </div>
          </div>

          {/* Charts Section */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
            {/* Registration Chart */}
            <div className="lg:col-span-2 flex flex-col gap-4 rounded-xl border border-slate-200 dark:border-slate-800 p-6 bg-white dark:bg-slate-900/50">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-slate-900 dark:text-white text-base font-medium">Lượt đăng ký mới</p>
                  <p className="text-slate-500 dark:text-slate-400 text-sm">Trong {registrationDays} ngày qua</p>
                </div>
                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-2">
                    <div className="w-2.5 h-2.5 rounded-full bg-primary"></div>
                    <span className="text-xs font-medium text-slate-600 dark:text-slate-400">Luật sư</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-2.5 h-2.5 rounded-full bg-slate-300 dark:bg-slate-600"></div>
                    <span className="text-xs font-medium text-slate-600 dark:text-slate-400">Người dùng</span>
                  </div>
                </div>
              </div>
              <div className="h-72">
                <div className="h-60">
                  <svg fill="none" height="100%" preserveAspectRatio="none" viewBox="0 0 500 200" width="100%">
                    {/* Grid lines */}
                    <line className="dark:stroke-slate-800" stroke="#F1F5F9" strokeWidth="1" x1="0" x2="500" y1="50" y2="50"></line>
                    <line className="dark:stroke-slate-800" stroke="#F1F5F9" strokeWidth="1" x1="0" x2="500" y1="100" y2="100"></line>
                    <line className="dark:stroke-slate-800" stroke="#F1F5F9" strokeWidth="1" x1="0" x2="500" y1="150" y2="150"></line>
                    
                    {/* Generate paths from data */}
                    {registrations.length > 0 && (() => {
                      const maxCount = Math.max(...registrations.map(r => r.userCount + r.lawyerCount), 1);
                      const width = 500;
                      const height = 200;
                      const step = registrations.length > 1 ? width / (registrations.length - 1) : width;
                      
                      // User path
                      const userPath = registrations.map((r, i) => {
                        const x = i * step;
                        const y = height - (r.userCount / maxCount) * (height - 20);
                        return `${i === 0 ? 'M' : 'L'}${x},${y}`;
                      }).join(' ');
                      
                      // Lawyer path
                      const lawyerPath = registrations.map((r, i) => {
                        const x = i * step;
                        const y = height - (r.lawyerCount / maxCount) * (height - 20);
                        return `${i === 0 ? 'M' : 'L'}${x},${y}`;
                      }).join(' ');
                      
                      return (
                        <>
                          <path className="dark:stroke-slate-600" d={userPath} stroke="#94a3b8" strokeWidth="2" fill="none"></path>
                          <path d={lawyerPath} stroke="#137fec" strokeWidth="2.5" fill="none"></path>
                        </>
                      );
                    })()}
                  </svg>
                </div>
                {/* X-axis labels */}
                <div className="flex justify-between text-xs text-slate-500 dark:text-slate-400 mt-3 px-1">
                  {registrations.length > 0 && registrations.map((r, i) => {
                    // Show all labels for 7 days, show every 5th for 30 days
                    const shouldShow = registrationDays === 7 || i % 5 === 0 || i === registrations.length - 1;
                    return (
                      <span 
                        key={i} 
                        className={`${shouldShow ? 'opacity-100' : 'opacity-0'}`}
                        style={{flex: 1, textAlign: registrations.length > 1 ? (i === 0 ? 'left' : i === registrations.length - 1 ? 'right' : 'center') : 'center'}}
                      >
                        {r.date}
                      </span>
                    );
                  })}
                </div>
              </div>
            </div>

            {/* Verification Status Chart */}
            <div className="flex flex-col gap-4 rounded-xl border border-slate-200 dark:border-slate-800 p-6 bg-white dark:bg-slate-900/50">
              <p className="text-slate-900 dark:text-white text-base font-medium">Tình trạng xác minh Luật sư</p>
              <div className="relative flex items-center justify-center h-48">
                <svg className="absolute w-full h-full" viewBox="0 0 36 36">
                  <path className="stroke-slate-200 dark:stroke-slate-700" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" strokeWidth="3"></path>
                  <path className="stroke-red-500" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" strokeDasharray={`${rejectedDash}, ${circumference - rejectedDash}`} strokeLinecap="round" strokeWidth="3"></path>
                  <path className="stroke-yellow-500" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" strokeDasharray={`${pendingDash}, ${circumference - pendingDash}`} strokeDashoffset={`-${rejectedDash}`} strokeLinecap="round" strokeWidth="3"></path>
                  <path className="stroke-green-500" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" strokeDasharray={`${verifiedDash}, ${circumference - verifiedDash}`} strokeDashoffset={`-${rejectedDash + pendingDash}`} strokeLinecap="round" strokeWidth="3"></path>
                </svg>
                <div className="text-center">
                  <p className="text-slate-500 dark:text-slate-400 text-xs">Tổng số</p>
                  <p className="text-slate-900 dark:text-white text-3xl font-bold">{verificationStatus.total}</p>
                </div>
              </div>
              <div className="flex flex-col gap-3 pt-4">
                <div className="flex justify-between items-center text-sm">
                  <div className="flex items-center gap-2">
                    <div className="w-2.5 h-2.5 rounded-full bg-green-500"></div>
                    <span className="text-slate-600 dark:text-slate-400">Đã xác minh ({verificationStatus.verified})</span>
                  </div>
                  <span className="font-medium text-slate-800 dark:text-slate-200">{verifiedPercent}%</span>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <div className="flex items-center gap-2">
                    <div className="w-2.5 h-2.5 rounded-full bg-yellow-500"></div>
                    <span className="text-slate-600 dark:text-slate-400">Chờ xác minh ({verificationStatus.pending})</span>
                  </div>
                  <span className="font-medium text-slate-800 dark:text-slate-200">{pendingPercent}%</span>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <div className="flex items-center gap-2">
                    <div className="w-2.5 h-2.5 rounded-full bg-red-500"></div>
                    <span className="text-slate-600 dark:text-slate-400">Bị từ chối ({verificationStatus.rejected})</span>
                  </div>
                  <span className="font-medium text-slate-800 dark:text-slate-200">{rejectedPercent}%</span>
                </div>
              </div>
            </div>
          </div>

          {/* Recent Activities */}
          <div className="rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900/50">
            <div className="p-6 border-b border-slate-200 dark:border-slate-800">
              <h3 className="text-base font-medium text-slate-900 dark:text-white">Hoạt động gần đây</h3>
            </div>
            <div className="divide-y divide-slate-200 dark:divide-slate-800">
              <div className="flex items-center justify-between p-4 hover:bg-slate-50 dark:hover:bg-slate-800/50">
                <div className="flex items-center gap-4">
                  <div className="p-2 rounded-full bg-blue-100 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400">
                    <span className="material-symbols-outlined text-xl">person_add</span>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-800 dark:text-slate-200">Luật sư Nguyễn Văn A vừa đăng ký.</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400">ID: #LS151</p>
                  </div>
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-400">2 phút trước</p>
              </div>
              <div className="flex items-center justify-between p-4 hover:bg-slate-50 dark:hover:bg-slate-800/50">
                <div className="flex items-center gap-4">
                  <div className="p-2 rounded-full bg-purple-100 dark:bg-purple-900/50 text-purple-600 dark:text-purple-400">
                    <span className="material-symbols-outlined text-xl">help</span>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-800 dark:text-slate-200">Người dùng Trần Thị B đã gửi một câu hỏi mới.</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400">Lĩnh vực: Dân sự</p>
                  </div>
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-400">15 phút trước</p>
              </div>
              <div className="flex items-center justify-between p-4 hover:bg-slate-50 dark:hover:bg-slate-800/50">
                <div className="flex items-center gap-4">
                  <div className="p-2 rounded-full bg-green-100 dark:bg-green-900/50 text-green-600 dark:text-green-400">
                    <span className="material-symbols-outlined text-xl">task_alt</span>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-800 dark:text-slate-200">Tài khoản luật sư Lê Văn C đã được xác minh.</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400">ID: #LS148</p>
                  </div>
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-400">1 giờ trước</p>
              </div>
              <div className="flex items-center justify-between p-4 hover:bg-slate-50 dark:hover:bg-slate-800/50">
                <div className="flex items-center gap-4">
                  <div className="p-2 rounded-full bg-yellow-100 dark:bg-yellow-900/50 text-yellow-600 dark:text-yellow-400">
                    <span className="material-symbols-outlined text-xl">article</span>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-800 dark:text-slate-200">Một bài tin tức mới đã được đăng.</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400">Tiêu đề: "Cập nhật luật lao động 2024"</p>
                  </div>
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-400">3 giờ trước</p>
              </div>
              <div className="p-4 text-center">
                <a className="text-sm font-medium text-primary hover:underline" href="#">Xem tất cả hoạt động</a>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
