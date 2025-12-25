import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Layout from "../components/Layout";
import axiosInstance from "../utils/axiosInstance";

export default function LegalDocuments() {
  const [showAIChat, setShowAIChat] = useState(false);
  
  // Data states
  const [documents, setDocuments] = useState([]);
  const [categories, setCategories] = useState([]);
  const [stats, setStats] = useState({
    totalDocuments: 0,
    newDocuments: 0,
    mostViewed: '',
    totalCategories: 0
  });
  
  // Filter states
  const [filters, setFilters] = useState({
    keyword: '',
    category: '',
    sortBy: 'createdAt',
    status: ''
  });
  
  // Pagination states
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0
  });
  
  // Loading states
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch categories on mount
  useEffect(() => {
    fetchCategories();
    fetchStats();
  }, []);

  // Fetch documents when filters or pagination change
  useEffect(() => {
    fetchDocuments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.category, filters.sortBy, filters.status, pagination.page]);

  // Debounce search
  useEffect(() => {
    const timer = setTimeout(() => {
      if (filters.keyword !== undefined) {
        setPagination(prev => ({ ...prev, page: 0 }));
        fetchDocuments();
      }
    }, 500);
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.keyword]);

  const fetchCategories = async () => {
    try {
      const response = await axiosInstance.get('/api/documents/categories');
      if (response.data.success) {
        setCategories(response.data.data.categories || []);
      }
    } catch (err) {
      console.error('Error fetching categories:', err);
    }
  };

  const fetchStats = async () => {
    try {
      const [statsRes, trendingRes] = await Promise.all([
        axiosInstance.get('/api/documents/categories/stats'),
        axiosInstance.get('/api/documents/trending', { params: { page: 0, size: 1 } })
      ]);
      
      if (statsRes.data.success) {
        const categoryStats = statsRes.data.data.categoryStats || [];
        const total = categoryStats.reduce((sum, cat) => sum + cat.documentCount, 0);
        setStats(prev => ({
          ...prev,
          totalDocuments: total,
          totalCategories: categoryStats.length
        }));
      }
      
      if (trendingRes.data.success && trendingRes.data.data.content?.length > 0) {
        setStats(prev => ({
          ...prev,
          mostViewed: trendingRes.data.data.content[0].title
        }));
      }
    } catch (err) {
      console.error('Error fetching stats:', err);
    }
  };

  const fetchDocuments = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const params = {
        page: pagination.page,
        size: pagination.size,
        sortBy: filters.sortBy,
        sortDirection: 'desc'
      };
      
      if (filters.keyword) params.keyword = filters.keyword;
      if (filters.category) params.category = filters.category;
      if (filters.status) params.status = filters.status;
      
      const response = await axiosInstance.get('/api/documents/search', { params });
      
      if (response.data.success) {
        const pageData = response.data.data;
        setDocuments(pageData.content || []);
        setPagination(prev => ({
          ...prev,
          totalPages: pageData.totalPages || 0,
          totalElements: pageData.totalElements || 0
        }));
      }
    } catch (err) {
      console.error('Error fetching documents:', err);
      setError('Không thể tải danh sách văn bản. Vui lòng thử lại.');
      setDocuments([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchChange = (e) => {
    setFilters(prev => ({ ...prev, keyword: e.target.value }));
  };

  const handleCategoryChange = (e) => {
    setFilters(prev => ({ ...prev, category: e.target.value }));
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  const handleSortChange = (e) => {
    const value = e.target.value;
    setFilters(prev => ({ ...prev, sortBy: value }));
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  const handleStatusChange = (e) => {
    setFilters(prev => ({ ...prev, status: e.target.value }));
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
  };

  return (
    <>
      <link rel="preconnect" href="https://fonts.googleapis.com" />
      <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
      <link href="https://fonts.googleapis.com/css2?family=Lora:wght@400;500;600;700&family=Noto+Sans:wght@400;500;600;700&display=swap" rel="stylesheet" />
    <Layout>
      <div style={{ fontFamily: "'Noto Sans', sans-serif" }}>
      {/* Search Section */}
      <div className="bg-card-bg dark:bg-card-bg-dark border-b border-border-color dark:border-border-color-dark">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 py-10">
          <div className="flex flex-col gap-6">
            <h1 className="text-3xl font-bold text-center text-slate-900 dark:text-white mb-2" style={{ fontFamily: "'Lora', serif" }}>
              Tra Cứu Văn Bản Pháp Luật
            </h1>

            {/* Search Input */}
            <div className="relative">
              <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4">
                <span className="material-symbols-outlined text-slate-500">search</span>
              </div>
              <input
                className="block w-full rounded-md border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 py-3 pl-12 pr-4 text-slate-900 dark:text-white shadow-sm placeholder:text-slate-500 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-base"
                placeholder="Tìm kiếm theo số hiệu, trích yếu, nội dung..."
                type="search"
                value={filters.keyword}
                onChange={handleSearchChange}
              />
            </div>

            {/* Filters */}
            <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4">
              <div className="grid grid-cols-2 sm:flex sm:items-center gap-4 flex-shrink-0">
                <select 
                  className="rounded-md border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 dark:text-white shadow-sm focus:border-blue-600 focus:ring-blue-600 text-sm"
                  value={filters.category}
                  onChange={handleCategoryChange}
                >
                  <option value="">Tất cả danh mục</option>
                  {categories.map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
                <select 
                  className="rounded-md border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 dark:text-white shadow-sm focus:border-blue-600 focus:ring-blue-600 text-sm"
                  value={filters.sortBy}
                  onChange={handleSortChange}
                >
                  <option value="createdAt">Ngày ban hành mới nhất</option>
                  <option value="viewCount">Xem nhiều nhất</option>
                  <option value="title">Theo tên</option>
                </select>
                <select 
                  className="rounded-md border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 dark:text-white shadow-sm focus:border-blue-600 focus:ring-blue-600 text-sm"
                  value={filters.status}
                  onChange={handleStatusChange}
                >
                  <option value="">Mọi trạng thái</option>
                  <option value="ACTIVE">Còn hiệu lực</option>
                  <option value="INACTIVE">Hết hiệu lực</option>
                </select>
              </div>

              {/* Popular Tags */}
              <div className="flex items-center gap-2 overflow-x-auto">
                <span className="text-sm font-medium text-slate-600 dark:text-slate-400 flex-shrink-0">
                  Phổ biến:
                </span>
                <div className="flex items-center gap-2">
                  <button className="px-3 py-1 text-sm bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-400 rounded-md hover:bg-slate-200 dark:hover:bg-slate-600">
                    #Lao động
                  </button>
                  <button className="px-3 py-1 text-sm bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-400 rounded-md hover:bg-slate-200 dark:hover:bg-slate-600">
                    #Dân sự
                  </button>
                  <button className="px-3 py-1 text-sm bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-400 rounded-md hover:bg-slate-200 dark:hover:bg-slate-600">
                    #Hình sự
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="mx-auto max-w-7xl px-4 sm:px-6 py-8">
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <div className="flex items-center gap-4 p-4 bg-white dark:bg-slate-900 rounded-md border border-slate-200 dark:border-slate-800 shadow-sm">
            <div className="flex-shrink-0 size-10 flex items-center justify-center rounded-md bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300">
              <span className="material-symbols-outlined">description</span>
            </div>
            <div>
              <p className="text-sm text-slate-600 dark:text-slate-400">Tổng văn bản</p>
              <p className="text-xl font-semibold text-slate-900 dark:text-white">{stats.totalDocuments.toLocaleString()}</p>
            </div>
          </div>

          <div className="flex items-center gap-4 p-4 bg-white dark:bg-slate-900 rounded-md border border-slate-200 dark:border-slate-800 shadow-sm">
            <div className="flex-shrink-0 size-10 flex items-center justify-center rounded-md bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300">
              <span className="material-symbols-outlined">new_releases</span>
            </div>
            <div>
              <p className="text-sm text-slate-600 dark:text-slate-400">Văn bản mới</p>
              <p className="text-xl font-semibold text-slate-900 dark:text-white">{pagination.totalElements}</p>
            </div>
          </div>

          <div className="flex items-center gap-4 p-4 bg-white dark:bg-slate-900 rounded-md border border-slate-200 dark:border-slate-800 shadow-sm">
            <div className="flex-shrink-0 size-10 flex items-center justify-center rounded-md bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-300">
              <span className="material-symbols-outlined">visibility</span>
            </div>
            <div>
              <p className="text-sm text-slate-600 dark:text-slate-400">Xem nhiều</p>
              <p className="text-xl font-semibold text-slate-900 dark:text-white truncate">
                {stats.mostViewed || 'Đang cập nhật...'}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-4 p-4 bg-white dark:bg-slate-900 rounded-md border border-slate-200 dark:border-slate-800 shadow-sm">
            <div className="flex-shrink-0 size-10 flex items-center justify-center rounded-md bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300">
              <span className="material-symbols-outlined">category</span>
            </div>
            <div>
              <p className="text-sm text-slate-600 dark:text-slate-400">Danh mục</p>
              <p className="text-xl font-semibold text-slate-900 dark:text-white">{stats.totalCategories}</p>
            </div>
          </div>
        </div>

        {/* Document Cards Grid */}
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <div className="text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-4 text-slate-600 dark:text-slate-400">Đang tải văn bản...</p>
            </div>
          </div>
        ) : error ? (
          <div className="text-center py-20">
            <p className="text-red-600 dark:text-red-400">{error}</p>
            <button 
              onClick={fetchDocuments}
              className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              Thử lại
            </button>
          </div>
        ) : documents.length === 0 ? (
          <div className="text-center py-20">
            <span className="material-symbols-outlined text-6xl text-slate-400">description</span>
            <p className="mt-4 text-slate-600 dark:text-slate-400">Không tìm thấy văn bản nào</p>
          </div>
        ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {documents.map((doc) => (
            <div
              key={doc.documentId}
              className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 shadow-sm p-6 flex flex-col hover:shadow-md hover:border-blue-300 dark:hover:border-blue-700 transition-all"
            >
              <div className="flex items-center justify-between mb-3">
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-medium bg-slate-100 text-slate-700 dark:bg-slate-700 dark:text-slate-300">
                  {doc.category || 'Chưa phân loại'}
                </span>
                <div
                  className={`flex items-center gap-1 ${
                    doc.status === "ACTIVE"
                      ? "text-green-600 dark:text-green-400"
                      : "text-red-600 dark:text-red-400"
                  }`}
                  title={doc.status === "ACTIVE" ? "Còn hiệu lực" : "Hết hiệu lực"}
                >
                  <span className="material-symbols-outlined text-base" style={{ fontVariationSettings: "'FILL' 1" }}>
                    {doc.status === "ACTIVE" ? "check_circle" : "cancel"}
                  </span>
                  <span className="text-xs font-medium">
                    {doc.status === "ACTIVE" ? "Còn hiệu lực" : "Hết hiệu lực"}
                  </span>
                </div>
              </div>

              <h3 className="text-lg font-semibold text-blue-700 dark:text-blue-400 flex-grow leading-snug" style={{ fontFamily: "'Lora', serif" }}>
                {doc.title}
              </h3>

              <p className="text-sm text-slate-600 dark:text-slate-400 mt-2">
                Ngày ban hành: {formatDate(doc.createdAt)}
              </p>

              <div className="mt-4 pt-4 border-t border-slate-200 dark:border-slate-800 flex items-center justify-between">
                <div className="flex items-center gap-1.5 text-sm text-slate-600 dark:text-slate-400">
                  <span className="material-symbols-outlined text-base">visibility</span>
                  <span>{(doc.viewCount || 0).toLocaleString()} lượt xem</span>
                </div>
                <Link
                  to={`/legal-documents/${doc.documentId}`}
                  className="inline-flex items-center justify-center rounded-md px-4 py-2 text-sm font-semibold bg-blue-600 text-white hover:bg-blue-700"
                >
                  Xem chi tiết
                </Link>
              </div>
            </div>
          ))}
        </div>
        )}

        {/* Pagination */}
        {pagination.totalPages > 1 && (
        <nav aria-label="Pagination" className="mt-8 flex items-center justify-center">
          <ul className="flex items-center -space-x-px h-10 text-base">
            <li>
              <button 
                onClick={() => handlePageChange(pagination.page - 1)}
                disabled={pagination.page === 0}
                className="flex items-center justify-center px-4 h-10 ms-0 leading-tight text-slate-600 bg-white border border-e-0 border-slate-300 rounded-s-md hover:bg-slate-100 hover:text-slate-900 dark:bg-slate-900 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-white disabled:opacity-50 disabled:cursor-not-allowed">
                <span className="sr-only">Previous</span>
                <span className="material-symbols-outlined">chevron_left</span>
              </button>
            </li>
            {[...Array(Math.min(pagination.totalPages, 5))].map((_, idx) => {
              let pageNum;
              if (pagination.totalPages <= 5) {
                pageNum = idx;
              } else if (pagination.page < 3) {
                pageNum = idx;
              } else if (pagination.page > pagination.totalPages - 3) {
                pageNum = pagination.totalPages - 5 + idx;
              } else {
                pageNum = pagination.page - 2 + idx;
              }
              return (
                <li key={pageNum}>
                  <button 
                    onClick={() => handlePageChange(pageNum)}
                    className={`flex items-center justify-center px-4 h-10 leading-tight border ${
                      pagination.page === pageNum
                        ? 'z-10 text-white border-blue-600 bg-blue-600 hover:bg-blue-700'
                        : 'text-slate-600 bg-white border-slate-300 hover:bg-slate-100 hover:text-slate-900 dark:bg-slate-900 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-white'
                    }`}
                  >
                    {pageNum + 1}
                  </button>
                </li>
              );
            })}
            <li>
              <button 
                onClick={() => handlePageChange(pagination.page + 1)}
                disabled={pagination.page >= pagination.totalPages - 1}
                className="flex items-center justify-center px-4 h-10 leading-tight text-slate-600 bg-white border border-s-0 border-slate-300 rounded-e-md hover:bg-slate-100 hover:text-slate-900 dark:bg-slate-900 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-white disabled:opacity-50 disabled:cursor-not-allowed">
                <span className="sr-only">Next</span>
                <span className="material-symbols-outlined">chevron_right</span>
              </button>
            </li>
          </ul>
        </nav>
        )}
      </div>

      {/* AI Chat Popup */}
      <div className="fixed bottom-6 right-6 z-[100]">
        {showAIChat && (
          <div
            className="absolute bottom-[calc(100%+1rem)] right-0 w-80 sm:w-96 bg-white dark:bg-slate-900 rounded-xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col"
            style={{ height: "60vh", maxHeight: "500px" }}
          >
            <div className="flex-shrink-0 flex items-center justify-between p-4 bg-blue-600 text-white border-b border-slate-200 dark:border-slate-800">
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined">smart_toy</span>
                <h3 className="font-semibold text-lg" style={{ fontFamily: "'Lora', serif" }}>Trợ lý AI Pháp lý</h3>
              </div>
              <button onClick={() => setShowAIChat(false)} className="cursor-pointer">
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
            <div className="flex-1 p-4 overflow-y-auto space-y-4">
              {/* Chat messages will go here */}
            </div>
            <div className="flex-shrink-0 p-4 border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900">
              <div className="relative">
                <input
                  className="w-full rounded-full border-slate-300 dark:border-slate-600 bg-slate-100 dark:bg-slate-800 pl-4 pr-12 py-2.5 text-sm text-slate-900 dark:text-white focus:ring-blue-600 focus:border-blue-600"
                  placeholder="Nhập câu hỏi của bạn..."
                  type="text"
                />
                <button className="absolute inset-y-0 right-0 flex items-center justify-center h-full w-12 text-blue-600 hover:text-blue-700">
                  <span className="material-symbols-outlined">send</span>
                </button>
              </div>
            </div>
          </div>
        )}
        <button
          onClick={() => setShowAIChat(!showAIChat)}
          className="cursor-pointer w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center text-white shadow-lg hover:bg-blue-700 transition-transform hover:scale-105"
        >
          <span className="material-symbols-outlined text-3xl">smart_toy</span>
        </button>
      </div>
      </div>
    </Layout>
    </>
  );
}
