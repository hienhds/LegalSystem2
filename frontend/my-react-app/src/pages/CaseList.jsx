import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { caseService } from "../services/caseService";
import Layout from "../components/Layout";
import useUserProfile from "../hooks/useUserProfile";

export default function CaseList() {
  const { user } = useUserProfile();
  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Thêm state page để fix lỗi "page is not defined"
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // State cho tìm kiếm
  const [searchTerm, setSearchTerm] = useState("");
  const [keyword, setKeyword] = useState("");

  const navigate = useNavigate();

  // Gọi API mỗi khi keyword hoặc page thay đổi
  useEffect(() => {
    const fetchCases = async () => {
      setLoading(true);
      setError(null); // Reset lỗi trước khi gọi mới
      try {
        // Gọi API với page và keyword chuẩn
        const res = await caseService.getMyCases(page, 10, keyword);
        
        if (res.data.success) {
          setCases(res.data.data.content || []);
          setTotalPages(res.data.data.totalPages || 0);
        }
      } catch (err) {
        console.error("Lỗi tải danh sách:", err);
        // Hiển thị thông báo lỗi thân thiện hơn
        if (err.response && err.response.status === 500) {
           setError("Lỗi Server (500): Vui lòng kiểm tra lại Backend (File CaseRepository.java)");
        } else {
           setError("Không thể tải danh sách vụ án. Vui lòng thử lại sau.");
        }
      } finally {
        setLoading(false);
      }
    };
    fetchCases();
  }, [keyword, page]); // Thêm page vào dependency

  const handleSearch = (e) => {
    e.preventDefault();
    setKeyword(searchTerm);
    setPage(0); // Reset về trang đầu khi tìm kiếm
  };

  const getStatusColor = (status) => {
      switch(status) {
        case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800';
        case 'COMPLETED': return 'bg-green-100 text-green-800';
        case 'CANCELLED': return 'bg-red-100 text-red-800';
        default: return 'bg-gray-100 text-gray-800';
      }
  };

  return (
    <Layout>
      <div className="max-w-6xl mx-auto p-6">
        <div className="flex flex-col md:flex-row justify-between items-center mb-8 gap-4">
          <h1 className="text-3xl font-bold text-slate-800 dark:text-white">Hồ Sơ Vụ Án Của Tôi</h1>
          
          {user?.role === "LAWYER" && (
            <Link to="/create-case" className="bg-primary text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-600 flex items-center gap-2">
              <span className="material-symbols-outlined">add</span>
              Tạo vụ án mới
            </Link>
          )}
        </div>

        {/* Phần tìm kiếm */}
        <div className="bg-white dark:bg-slate-900 p-4 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 mb-6">
          <form onSubmit={handleSearch} className="flex gap-2">
            <div className="relative flex-1">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
              <input
                type="text"
                className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all"
                placeholder="Tìm hồ sơ..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <button 
              type="submit"
              className="px-6 py-2.5 bg-primary text-white font-medium rounded-lg hover:bg-blue-600 transition-colors shadow-sm flex items-center gap-2"
            >
              Tìm kiếm
            </button>
          </form>
        </div>

        {error && (
          <div className="p-4 mb-4 text-red-700 bg-red-100 rounded-lg border border-red-400">
            {error}
          </div>
        )}

        {loading ? (
          <div className="text-center py-10">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary mx-auto"></div>
            <p className="mt-4 text-slate-500">Đang tải dữ liệu...</p>
          </div>
        ) : cases.length === 0 && !error ? (
          <div className="text-center py-20 bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800">
            <span className="material-symbols-outlined text-6xl text-slate-300">folder_off</span>
            <p className="mt-4 text-slate-500 text-lg">
              {keyword ? `Không tìm thấy kết quả cho "${keyword}"` : "Bạn chưa có hồ sơ vụ án nào."}
            </p>
          </div>
        ) : (
          <div className="grid gap-4">
            {cases.map((c) => (
              <div 
                key={c.caseId} 
                onClick={() => navigate(`/cases/${c.caseId}`)}
                className="bg-white dark:bg-slate-900 p-5 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 cursor-pointer hover:shadow-md transition-all flex flex-col md:flex-row justify-between items-start md:items-center gap-4 group"
              >
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100 group-hover:text-blue-600 transition-colors">
                      {c.title}
                    </h3>
                    <span className="text-xs text-slate-400 bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded">#{c.caseId}</span>
                  </div>
                  <p className="text-sm text-slate-500 line-clamp-1 mb-2">{c.description}</p>
                  <div className="flex flex-wrap gap-4 text-xs text-slate-500 dark:text-slate-400">
                    <span className="flex items-center gap-1 bg-slate-50 dark:bg-slate-800 px-2 py-1 rounded">
                      <span className="material-symbols-outlined text-[16px]">person</span> 
                      {c.clientName && c.lawyerName ? (
                        user?.role === 'LAWYER' ? c.clientName : c.lawyerName
                      ) : (c.lawyerName || c.clientName)}
                    </span>
                    <span className="flex items-center gap-1 bg-slate-50 dark:bg-slate-800 px-2 py-1 rounded">
                      <span className="material-symbols-outlined text-[16px]">calendar_today</span> 
                      {c.createdAt ? new Date(c.createdAt).toLocaleDateString("vi-VN") : "N/A"}
                    </span>
                  </div>
                </div>
                <div>
                  <span className={`px-3 py-1 rounded-full text-xs font-semibold whitespace-nowrap ${getStatusColor(c.status)}`}>
                    {c.status === 'IN_PROGRESS' ? 'Đang xử lý' : c.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}