import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import Footer from "../components/Footer";
import CreateCaseModal from "../components/CreateCaseModal";

const API_BASE_URL = "http://localhost:8080/api";

export default function Cases() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [userRole, setUserRole] = useState(null);

  // Fetch cases from API
  useEffect(() => {
    let isCancelled = false;
    
    const loadCases = async () => {
      if (isCancelled) return;
      await fetchCases();
    };
    
    loadCases();
    
    return () => {
      isCancelled = true;
    };
  }, [page, pageSize]);

  // Get user role from localStorage
  useEffect(() => {
    const role = localStorage.getItem("userRole");
    setUserRole(role);
  }, []);

  const fetchCases = async () => {
    try {
      setLoading(true);
      setError(null);

      // Get auth token from localStorage
      const token = localStorage.getItem("accessToken");

      if (!token) {
        setError("Vui lòng đăng nhập để xem danh sách vụ án");
        setLoading(false);
        return;
      }

      console.log("Fetching cases...");
      
      const response = await fetch(
        `${API_BASE_URL}/cases?page=${page}&size=${pageSize}${searchQuery ? `&keyword=${encodeURIComponent(searchQuery)}` : ""}`,
        {
          method: "GET",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      console.log("Response status:", response.status);

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        console.error("Error response:", errorData);
        throw new Error(errorData.message || `Lỗi ${response.status}: Không thể tải danh sách vụ án`);
      }

      const data = await response.json();
      console.log("Cases data:", data);
      console.log("Result:", data.result);
      console.log("Result type:", typeof data.result);
      console.log("Result keys:", data.result ? Object.keys(data.result) : 'null');
      
      if (data.code === 200 && data.result) {
        const casesData = data.result.content || data.result;
        console.log("Cases array:", casesData);
        setCases(Array.isArray(casesData) ? casesData : []);
        
        // Update pagination info if available
        if (data.result.totalPages !== undefined) {
          setTotalPages(data.result.totalPages);
          setTotalElements(data.result.totalElements);
        }
      } else {
        throw new Error(data.message || "Không thể tải danh sách vụ án");
      }
    } catch (err) {
      console.error("Error fetching cases:", err);
      setError(err.message);
      setCases([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setPage(0); // Reset to first page
    fetchCases();
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN");
  };

  const getLatestUpdateDate = (caseItem) => {
    // Lấy thời gian update gần nhất từ progressUpdates
    if (caseItem.progressUpdates && caseItem.progressUpdates.length > 0) {
      // progressUpdates đã được sort theo thời gian giảm dần, lấy phần tử đầu tiên
      return formatDate(caseItem.progressUpdates[0].updateDate);
    }
    // Nếu không có progressUpdates, dùng createdAt
    return formatDate(caseItem.createdAt);
  };

  const getStatusInfo = (status) => {
    const statusMap = {
      PENDING: { label: "Chờ xử lý", color: "gray" },
      IN_PROGRESS: { label: "Đang xử lý", color: "blue" },
      RESOLVED: { label: "Đã giải quyết", color: "green" },
      CLOSED: { label: "Đã đóng", color: "green" },
    };
    return statusMap[status] || { label: status, color: "gray" };
  };

  const getIconByStatus = (status) => {
    const iconMap = {
      PENDING: "schedule",
      IN_PROGRESS: "folder_shared",
      RESOLVED: "assignment_turned_in",
      CLOSED: "check_circle",
    };
    return iconMap[status] || "folder";
  };

  const getStatusBadgeClass = (color) => {
    const colors = {
      blue: "bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-200",
      gray: "bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200",
      green: "bg-green-100 text-green-800 dark:bg-green-900/50 dark:text-green-200",
    };
    return colors[color] || colors.gray;
  };

  const getIconBgClass = (color) => {
    const colors = {
      blue: "bg-blue-50 dark:bg-blue-900/20 text-blue-500",
      orange: "bg-orange-50 dark:bg-orange-900/20 text-orange-500",
      green: "bg-green-50 dark:bg-green-900/20 text-green-600",
    };
    return colors[color] || colors.blue;
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-900 transition-colors duration-300">
      <Header />
      
      <main className="flex-grow py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-5xl mx-auto space-y-6">
          {/* Page Header */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Hồ Sơ Vụ Án Của Tôi</h1>
              <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
                Quản lý và theo dõi tiến độ các vụ việc pháp lý của bạn.
              </p>
            </div>
            {userRole === "LAWYER" && (
              <button 
                onClick={() => setIsCreateModalOpen(true)}
                className="inline-flex items-center justify-center gap-2 px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg shadow-sm transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-600 dark:focus:ring-offset-slate-900"
              >
                <span className="material-symbols-outlined text-lg">add</span>
                Tạo vụ án mới
              </button>
            )}
          </div>

          {/* Search and Filter */}
          <div className="bg-white dark:bg-slate-800 p-4 rounded-xl shadow-sm border border-slate-200 dark:border-slate-700">
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="relative flex-grow">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span className="material-symbols-outlined text-slate-400">search</span>
                </div>
                <input
                  className="block w-full pl-10 pr-3 py-2.5 border border-slate-300 dark:border-slate-600 rounded-lg leading-5 bg-white dark:bg-slate-700 text-slate-900 dark:text-white placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-custom-blue-dark focus:border-custom-blue-dark sm:text-sm transition-colors"
                  placeholder="Tìm theo mã hồ sơ, tên vụ án..."
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  onKeyPress={handleKeyPress}
                />
              </div>
              <div className="flex gap-2">
                <select
                  className="block w-full pl-3 pr-10 py-2.5 text-base border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white focus:outline-none focus:ring-custom-blue-dark focus:border-custom-blue-dark sm:text-sm rounded-lg"
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <option value="all">Tất cả trạng thái</option>
                  <option value="processing">Đang xử lý</option>
                  <option value="completed">Đã hoàn thành</option>
                  <option value="pending">Chờ bổ sung</option>
                </select>
                <button 
                  onClick={handleSearch}
                  className="px-6 py-2.5 bg-custom-blue-dark hover:bg-blue-700 text-white font-medium rounded-lg shadow-sm text-sm transition-colors whitespace-nowrap"
                >
                  Tìm kiếm
                </button>
              </div>
            </div>
          </div>

          {/* Cases List */}
          <div className="space-y-4">
            {loading ? (
              <div className="flex items-center justify-center py-12">
                <div className="text-center">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-custom-blue-dark mx-auto mb-4"></div>
                  <p className="text-slate-600 dark:text-slate-400">Đang tải danh sách vụ án...</p>
                </div>
              </div>
            ) : error ? (
              <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl p-6 text-center">
                <span className="material-symbols-outlined text-red-500 text-4xl mb-2">error</span>
                <p className="text-red-800 dark:text-red-300 font-medium">{error}</p>
                <button 
                  onClick={fetchCases}
                  className="mt-4 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm transition-colors"
                >
                  Thử lại
                </button>
              </div>
            ) : cases.length === 0 ? (
              <div className="bg-slate-100 dark:bg-slate-800 rounded-xl p-12 text-center">
                <span className="material-symbols-outlined text-slate-400 text-6xl mb-4">folder_open</span>
                <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">Chưa có vụ án nào</h3>
                <p className="text-slate-600 dark:text-slate-400">Bắt đầu bằng cách tạo vụ án mới.</p>
              </div>
            ) : (
              cases.map((caseItem) => {
                const statusInfo = getStatusInfo(caseItem.status);
                const icon = getIconByStatus(caseItem.status);
                
                return (
                  <div
                    key={caseItem.id}
                    onClick={() => navigate(`/cases/${caseItem.id}`)}
                    className="group bg-white dark:bg-slate-800 rounded-xl p-5 border border-slate-200 dark:border-slate-700 hover:border-custom-blue-dark dark:hover:border-custom-blue-dark hover:shadow-md transition-all duration-200 relative overflow-hidden cursor-pointer"
                  >
                    <div className="absolute top-5 right-5">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusBadgeClass(statusInfo.color)}`}>
                        {statusInfo.label}
                      </span>
                    </div>
                    <div className="flex flex-col md:flex-row md:items-start gap-4">
                      <div className="flex-shrink-0">
                        <div className={`h-12 w-12 rounded-full flex items-center justify-center ${getIconBgClass(statusInfo.color)}`}>
                          <span className="material-symbols-outlined">{icon}</span>
                        </div>
                      </div>
                      <div className="flex-grow space-y-3">
                        <div>
                          <div className="flex items-center gap-3 mb-1">
                            <span className="text-xs font-mono text-slate-500 bg-slate-100 dark:bg-slate-700 px-2 py-0.5 rounded">
                              #{caseItem.id}
                            </span>
                            <span className="text-xs text-slate-600 dark:text-slate-400 flex items-center gap-1">
                              <span className="material-symbols-outlined text-[14px]">calendar_today</span>
                              {formatDate(caseItem.createdAt)}
                            </span>
                          </div>
                          <h3 className="text-lg font-semibold text-slate-900 dark:text-white group-hover:text-custom-blue-dark transition-colors">
                            {caseItem.title}
                          </h3>
                          <p className="text-sm text-slate-600 dark:text-slate-400 mt-1 line-clamp-2">
                            {caseItem.description}
                          </p>
                        </div>
                        <div className="flex flex-wrap items-center gap-y-2 gap-x-6 pt-2 border-t border-slate-100 dark:border-slate-700">
                          <div className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                            <span className="material-symbols-outlined text-slate-400 text-lg">person</span>
                            <span>LS. {caseItem.lawyerName || 'N/A'}</span>
                          </div>
                          <div className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                            <span className="material-symbols-outlined text-slate-400 text-lg">group</span>
                            <span>Khách hàng: {caseItem.clientName || 'N/A'}</span>
                          </div>
                          <div className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                            <span className="material-symbols-outlined text-slate-400 text-lg">schedule</span>
                            <span>Cập nhật: {getLatestUpdateDate(caseItem)}</span>
                          </div>
                        </div>
                      </div>
                      <div className="hidden md:flex flex-col justify-center self-center opacity-0 group-hover:opacity-100 transition-opacity transform translate-x-2 group-hover:translate-x-0">
                        <span className="material-symbols-outlined text-slate-400">chevron_right</span>
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* Pagination */}
          {!loading && !error && cases.length > 0 && (
            <div className="flex items-center justify-between border-t border-slate-200 dark:border-slate-700 px-4 py-3 sm:px-6 mt-6">
              <div className="flex flex-1 justify-between sm:hidden">
                <button 
                  onClick={() => setPage(Math.max(0, page - 1))}
                  disabled={page === 0}
                  className="relative inline-flex items-center rounded-md border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Trước
                </button>
                <button 
                  onClick={() => setPage(page + 1)}
                  disabled={page >= totalPages - 1}
                  className="relative ml-3 inline-flex items-center rounded-md border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Sau
                </button>
              </div>
              <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm text-slate-700 dark:text-slate-400">
                    Hiển thị <span className="font-medium">{page * pageSize + 1}</span> đến{" "}
                    <span className="font-medium">{Math.min((page + 1) * pageSize, totalElements)}</span> trong số{" "}
                    <span className="font-medium">{totalElements}</span> kết quả
                  </p>
                </div>
                <div>
                  <nav aria-label="Pagination" className="isolate inline-flex -space-x-px rounded-md shadow-sm">
                    <button 
                      onClick={() => setPage(Math.max(0, page - 1))}
                      disabled={page === 0}
                      className="relative inline-flex items-center rounded-l-md px-2 py-2 text-slate-400 ring-1 ring-inset ring-slate-300 dark:ring-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700 focus:z-20 focus:outline-offset-0 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <span className="sr-only">Previous</span>
                      <span className="material-symbols-outlined text-sm">chevron_left</span>
                    </button>
                    
                    {[...Array(Math.min(totalPages, 5))].map((_, idx) => {
                      const pageNumber = idx;
                      return (
                        <button
                          key={pageNumber}
                          onClick={() => setPage(pageNumber)}
                          className={`relative inline-flex items-center px-4 py-2 text-sm font-semibold ${
                            page === pageNumber
                              ? "z-10 bg-custom-blue-dark text-white focus:z-20 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-custom-blue-dark"
                              : "text-slate-900 dark:text-white ring-1 ring-inset ring-slate-300 dark:ring-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700 focus:z-20 focus:outline-offset-0"
                          }`}
                        >
                          {pageNumber + 1}
                        </button>
                      );
                    })}
                    
                    <button 
                      onClick={() => setPage(page + 1)}
                      disabled={page >= totalPages - 1}
                      className="relative inline-flex items-center rounded-r-md px-2 py-2 text-slate-400 ring-1 ring-inset ring-slate-300 dark:ring-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700 focus:z-20 focus:outline-offset-0 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <span className="sr-only">Next</span>
                      <span className="material-symbols-outlined text-sm">chevron_right</span>
                    </button>
                  </nav>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>

      <Footer />

      {/* Create Case Modal */}
      <CreateCaseModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={() => {
          setPage(0);
          fetchCases();
        }}
      />
    </div>
  );
}
