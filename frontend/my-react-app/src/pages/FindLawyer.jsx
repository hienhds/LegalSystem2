import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Layout from "../components/Layout";
import axiosInstance from "../utils/axiosInstance";

export default function FindLawyer() {
  // State for filters
  const [filters, setFilters] = useState({
    keyword: "",
    specialties: [],
    experience: "",
    location: "",
    rating: 0,
  });

  // State for lawyers data
  const [lawyers, setLawyers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0,
  });

  // State for filter options
  const [specializations, setSpecializations] = useState([]);
  const [barAssociations, setBarAssociations] = useState([]);
  const [stats, setStats] = useState({
    totalLawyers: 0,
    averageRating: 0,
    satisfactionRate: 0,
  });

  // Fetch lawyers from API
  const fetchLawyers = async () => {
    try {
      setLoading(true);
      const experienceRange = getExperienceRange(filters.experience);
      const params = {
        keyword: filters.keyword || undefined,
        specializationIds: filters.specialties.length > 0 ? filters.specialties.join(',') : undefined,
        barAssociationId: filters.location || undefined,
        minYearsOfExp: experienceRange.min,
        maxYearsOfExp: experienceRange.max,
        minRating: filters.rating > 0 ? filters.rating : undefined,
        page: pagination.page,
        size: pagination.size,
        sortBy: 'lawyerId',
        sortDir: 'DESC',
      };

      // Remove undefined params
      Object.keys(params).forEach(key => params[key] === undefined && delete params[key]);
      
      const response = await axiosInstance.get('/api/search/lawyers', { params });
      
      if (response.data.success) {
        const data = response.data.data;
        
        setLawyers(data.content || []);
        setPagination(prev => ({
          ...prev,
          totalPages: data.totalPages || 0,
          totalElements: data.totalElements || 0,
        }));
      }
    } catch (error) {
      console.error('Error fetching lawyers:', error);
      setLawyers([]);
    } finally {
      setLoading(false);
    }
  };

  // Fetch specializations
  const fetchSpecializations = async () => {
    try {
      const response = await axiosInstance.get('/api/specialization');
      if (response.data.success) {
        setSpecializations(response.data.data || []);
      }
    } catch (error) {
      console.error('Error fetching specializations:', error);
    }
  };

  // Fetch bar associations (locations)
  const fetchBarAssociations = async () => {
    try {
      const response = await axiosInstance.get('/api/bar-association');
      if (response.data.success) {
        setBarAssociations(response.data.data || []);
      }
    } catch (error) {
      console.error('Error fetching bar associations:', error);
    }
  };

  // Fetch stats
  const fetchStats = async () => {
    try {
      const response = await axiosInstance.get('/api/lawyers/stats');
      if (response.data.success) {
        setStats(response.data.data || {});
      }
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  };

  // Helper function to get full avatar URL
  const getAvatarUrl = (avatarUrl, fullName) => {
    if (!avatarUrl) {
      return `https://ui-avatars.com/api/?name=${encodeURIComponent(fullName || 'Lawyer')}&size=120&background=3b82f6&color=fff&bold=true`;
    }
    // If avatarUrl is relative path, prepend backend URL
    if (avatarUrl.startsWith('/uploads') || avatarUrl.startsWith('uploads')) {
      return `http://localhost:8080${avatarUrl.startsWith('/') ? '' : '/'}${avatarUrl}`;
    }
    // If already full URL, return as is
    return avatarUrl;
  };

  // Helper function to convert experience filter to min and max years
  const getExperienceRange = (experienceFilter) => {
    switch (experienceFilter) {
      case '0-3':
        return { min: 0, max: 2 };
      case '3-5':
        return { min: 3, max: 4 };
      case '5-10':
        return { min: 5, max: 9 };
      case '10+':
        return { min: 10, max: null }; // null means no upper limit
      default:
        return { min: undefined, max: undefined };
    }
  };

  // Load initial data
  useEffect(() => {
    fetchSpecializations();
    fetchBarAssociations();
    fetchStats();
  }, []);

  // Fetch lawyers when filters or page changes
  useEffect(() => {
    fetchLawyers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(filters), pagination.page]);

  const handleSearch = (e) => {
    e.preventDefault();
    // Reset to page 0 will trigger useEffect to fetch
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  const handleSpecialtyChange = (specialty) => {
    setFilters((prev) => ({
      ...prev,
      specialties: prev.specialties.includes(specialty)
        ? prev.specialties.filter((s) => s !== specialty)
        : [...prev.specialties, specialty],
    }));
  };

  const handleApplyFilters = () => {
    setPagination(prev => ({ ...prev, page: 0 }));
    // fetchLawyers will be triggered by useEffect when pagination.page changes
  };

  const handleResetFilters = () => {
    setFilters({
      keyword: "",
      specialties: [],
      experience: "",
      location: "",
      rating: 0,
    });
  };

  const renderStars = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;

    for (let i = 0; i < fullStars; i++) {
      stars.push(
        <span key={`full-${i}`} className="material-symbols-outlined !text-sm" style={{ fontVariationSettings: "'FILL' 1" }}>
          star
        </span>
      );
    }
    if (hasHalfStar) {
      stars.push(
        <span key="half" className="material-symbols-outlined !text-sm">
          star_half
        </span>
      );
    }
    const remainingStars = 5 - Math.ceil(rating);
    for (let i = 0; i < remainingStars; i++) {
      stars.push(
        <span key={`empty-${i}`} className="material-symbols-outlined !text-sm">
          star
        </span>
      );
    }
    return stars;
  };

  return (
    <Layout showFooter={true}>
      {/* Hero Section */}
      <section
        className="relative flex min-h-[200px] flex-col items-center justify-center p-4"
        style={{ backgroundImage: "linear-gradient(to right, #1e40af, #3b82f6)" }}
      >
        <div className="flex flex-col gap-4 text-center">
          <h1 className="font-serif text-white text-1xl sm:text-2xl md:text-5xl font-bold">Tìm Luật Sư Uy Tín</h1>
          <h2 className="text-white/90 text-sm font-normal leading-normal md:text-base">
            {stats.totalLawyers || 0} Luật sư | ⭐ {stats.averageRating?.toFixed(1) || '0.0'}/5 | ✅ {Math.round(stats.satisfactionRate || 0)}% Hài lòng
          </h2>
        </div>
        <form onSubmit={handleSearch} className="mt-8 w-full max-w-[600px] shadow-lg rounded-full">
          <label className="flex h-14 w-full items-stretch">
            <div className="flex items-center justify-center bg-white dark:bg-slate-800 pl-5 rounded-l-full">
              <span className="material-symbols-outlined text-slate-400">search</span>
            </div>
            <input
              className="form-input w-full flex-1 border-0 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 placeholder:text-slate-400 focus:ring-0 text-base font-normal leading-normal"
              placeholder="Tìm kiếm theo tên, chuyên ngành, công ty..."
              value={filters.keyword}
              onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            />
            <div className="flex items-center justify-center bg-white dark:bg-slate-800 pr-2 rounded-r-full">
              <button
                type="submit"
                className="flex min-w-[84px] cursor-pointer items-center justify-center overflow-hidden rounded-full h-11 px-5 bg-blue-600 text-white text-base font-bold hover:bg-blue-700"
              >
                <span className="truncate">Tìm kiếm</span>
              </button>
            </div>
          </label>
        </form>
      </section>

      {/* Main Content */}
      <div className="container mx-auto px-4 py-8 lg:py-12">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Sidebar Filters */}
          <aside className="lg:col-span-4 xl:col-span-3">
            <div className="sticky top-24">
              <div className="bg-white dark:bg-slate-900 p-6 rounded-xl border border-slate-200 dark:border-slate-800">
                <h2 className="text-xl font-bold font-serif mb-6">Bộ lọc</h2>

                {/* Lĩnh vực */}
                <div className="border-b border-slate-200 dark:border-slate-800 pb-6">
                  <h3 className="text-base font-bold mb-3">Lĩnh vực</h3>
                  <div className="space-y-3">
                    {specializations.map((spec) => (
                      <label key={spec.specId} className="flex items-center gap-x-3 cursor-pointer">
                        <input
                          className="h-5 w-5 rounded border-slate-300 dark:border-slate-600 bg-transparent text-blue-600 focus:ring-blue-600/50"
                          type="checkbox"
                          checked={filters.specialties.includes(spec.specId)}
                          onChange={() => handleSpecialtyChange(spec.specId)}
                        />
                        <p>{spec.specName}</p>
                      </label>
                    ))}
                  </div>
                </div>

                {/* Kinh nghiệm */}
                <div className="border-b border-slate-200 dark:border-slate-800 py-6">
                  <h3 className="text-base font-bold mb-3">Kinh nghiệm</h3>
                  <div className="space-y-3">
                    {[
                      { label: "Dưới 3 năm", value: "0-3" },
                      { label: "3 - 5 năm", value: "3-5" },
                      { label: "5 - 10 năm", value: "5-10" },
                      { label: "Trên 10 năm", value: "10+" },
                    ].map((exp) => (
                      <label key={exp.value} className="flex items-center gap-x-3 cursor-pointer">
                        <input
                          className="h-5 w-5 border-slate-300 dark:border-slate-600 bg-transparent text-blue-600 focus:ring-blue-600/50"
                          name="experience"
                          type="radio"
                          checked={filters.experience === exp.value}
                          onChange={() => setFilters({ ...filters, experience: exp.value })}
                        />
                        <p>{exp.label}</p>
                      </label>
                    ))}
                  </div>
                </div>

                {/* Khu vực */}
                <div className="border-b border-slate-200 dark:border-slate-800 py-6">
                  <h3 className="text-base font-bold mb-3">Khu vực</h3>
                  <select
                    className="form-select w-full rounded-lg border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 focus:border-blue-600 focus:ring-blue-600/50"
                    value={filters.location}
                    onChange={(e) => setFilters({ ...filters, location: e.target.value })}
                  >
                    <option value="">Tất cả</option>
                    {barAssociations.map((bar) => (
                      <option key={bar.barAssociationId} value={bar.barAssociationId}>
                        {bar.barAssociationName}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Đánh giá */}
                <div className="py-6 border-b border-slate-200 dark:border-slate-800">
                  <h3 className="text-base font-bold mb-3">Đánh giá</h3>
                  <div className="flex items-center gap-1 text-2xl text-slate-300">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <span
                        key={star}
                        className={`material-symbols-outlined cursor-pointer ${
                          star <= filters.rating ? "!text-yellow-400" : ""
                        }`}
                        onClick={() => setFilters({ ...filters, rating: star })}
                      >
                        star
                      </span>
                    ))}
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="flex flex-col gap-3 pt-6">
                  <button
                    onClick={handleApplyFilters}
                    className="w-full flex cursor-pointer items-center justify-center overflow-hidden rounded-lg h-11 px-4 bg-blue-600 text-white text-base font-bold hover:bg-blue-700"
                  >
                    Áp dụng
                  </button>
                  <button
                    onClick={handleResetFilters}
                    className="w-full flex cursor-pointer items-center justify-center overflow-hidden rounded-lg h-11 px-4 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 text-base font-bold"
                  >
                    Xóa bộ lọc
                  </button>
                </div>
              </div>
            </div>
          </aside>

          {/* Lawyer Cards */}
          <main className="lg:col-span-8 xl:col-span-9">
            {loading ? (
              <div className="flex justify-center items-center py-20">
                <div className="text-center">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                  <p className="mt-4 text-slate-600 dark:text-slate-400">Đang tải...</p>
                </div>
              </div>
            ) : lawyers.length === 0 ? (
              <div className="text-center py-20">
                <span className="material-symbols-outlined text-6xl text-slate-400">search_off</span>
                <p className="mt-4 text-slate-600 dark:text-slate-400 text-lg">Không tìm thấy luật sư nào</p>
                <button
                  onClick={handleResetFilters}
                  className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  Xóa bộ lọc
                </button>
              </div>
            ) : (
              <>
                <div className="mb-6 flex items-center justify-between">
                  <p className="text-slate-600 dark:text-slate-400">
                    Tìm thấy <span className="font-bold text-blue-600">{pagination.totalElements}</span> luật sư
                  </p>
                  <p className="text-sm text-slate-500">
                    Trang {pagination.page + 1} / {pagination.totalPages}
                  </p>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {lawyers.map((lawyer) => (
                    <div
                      key={lawyer.lawyerId}
                      className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 flex flex-col sm:flex-row gap-6 shadow-sm hover:shadow-lg hover:border-blue-600/50 transition-all duration-300"
                    >
                      {/* Avatar */}
                      <div className="flex-shrink-0 text-center">
                        <div className="relative inline-block">
                          <img
                            className="size-[120px] rounded-full object-cover border-2 border-slate-200"
                            src={getAvatarUrl(lawyer.avatarUrl, lawyer.fullName)}
                            alt={lawyer.fullName}
                            onError={(e) => {
                              e.target.onerror = null;
                              // Fallback to ui-avatars
                              e.target.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(lawyer.fullName || 'L')}&size=120&background=3b82f6&color=fff&bold=true`;
                            }}
                          />
                          {lawyer.verificationStatus === 'APPROVED' && (
                            <span
                              className="material-symbols-outlined absolute bottom-1 right-1 text-white bg-blue-600 rounded-full p-1 text-lg"
                              style={{ fontVariationSettings: "'FILL' 1" }}
                            >
                              verified
                            </span>
                          )}
                        </div>
                      </div>

                      {/* Content */}
                      <div className="flex-1">
                        <div className="flex justify-between items-start">
                          <div>
                            <h3 className="text-xl font-bold text-blue-600 font-serif">Luật sư {lawyer.fullName}</h3>
                            <div className="flex items-center gap-1 text-yellow-400 mt-1">
                              {renderStars(lawyer.averageRating || 0)}
                              <span className="text-slate-500 dark:text-slate-400 text-sm ml-1">
                                ({lawyer.reviewCount || 0} đánh giá)
                              </span>
                            </div>
                          </div>
                          <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400">
                            <button className="p-2 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800">
                              <span className="material-symbols-outlined">bookmark_border</span>
                            </button>
                            <button className="p-2 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800">
                              <span className="material-symbols-outlined">call</span>
                            </button>
                          </div>
                        </div>

                        <div className="text-sm text-slate-500 dark:text-slate-400 mt-3 space-y-1.5">
                          <p className="flex items-center gap-2">
                            <span className="material-symbols-outlined !text-base">location_on</span>
                            <span>{lawyer.barAssociationName || 'Không có thông tin'}</span>
                          </p>
                          <p className="flex items-center gap-2">
                            <span className="material-symbols-outlined !text-base">business_center</span>
                            <span>{lawyer.officeAddress || 'Chưa cập nhật'}</span>
                          </p>
                          <p className="flex items-center gap-2">
                            <span className="material-symbols-outlined !text-base">workspace_premium</span>
                            <span>{lawyer.yearsOfExp || 0} năm kinh nghiệm</span>
                          </p>
                        </div>

                        <div className="flex flex-wrap gap-2 mt-4">
                          {(lawyer.specializations || []).map((specialty, idx) => (
                            <span
                              key={idx}
                              className="text-xs font-semibold bg-blue-600/10 text-blue-600 px-2.5 py-1 rounded-full"
                            >
                              {specialty}
                            </span>
                          ))}
                        </div>

                        <p className="text-sm text-slate-600 dark:text-slate-300 mt-4 line-clamp-2">
                          {lawyer.bio || 'Chưa có thông tin'}
                        </p>

                        <div className="flex flex-col sm:flex-row gap-3 mt-6">
                          <Link
                            to={`/lawyers/${lawyer.lawyerId}`}
                            className="flex-1 flex min-w-[84px] cursor-pointer items-center justify-center overflow-hidden rounded-lg h-10 px-4 border border-blue-600 text-blue-600 text-sm font-bold hover:bg-blue-600/5 dark:hover:bg-blue-600/10"
                          >
                            Xem chi tiết
                          </Link>
                          <button className="flex-1 flex min-w-[84px] cursor-pointer items-center justify-center overflow-hidden rounded-lg h-10 px-4 bg-blue-600 text-white text-sm font-bold gap-2 hover:bg-blue-700">
                            <span className="material-symbols-outlined !text-base">calendar_month</span>
                            Đặt lịch hẹn
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Pagination */}
                {pagination.totalPages > 1 && (
                  <nav aria-label="Pagination" className="flex items-center justify-center mt-12">
                    <button
                      onClick={() => setPagination({ ...pagination, page: Math.max(0, pagination.page - 1) })}
                      disabled={pagination.page === 0}
                      className="inline-flex items-center justify-center size-10 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <span className="material-symbols-outlined">chevron_left</span>
                    </button>

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
                        <button
                          key={pageNum}
                          onClick={() => setPagination({ ...pagination, page: pageNum })}
                          className={`inline-flex items-center justify-center size-10 rounded-lg font-medium mx-1 ${
                            pagination.page === pageNum
                              ? "bg-blue-600 text-white"
                              : "bg-white dark:bg-slate-900 text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
                          }`}
                        >
                          {pageNum + 1}
                        </button>
                      );
                    })}

                    {pagination.totalPages > 5 && <span className="inline-flex items-center justify-center size-10 text-slate-500 mx-1">...</span>}

                    <button
                      onClick={() =>
                        setPagination({ ...pagination, page: Math.min(pagination.totalPages - 1, pagination.page + 1) })
                      }
                      disabled={pagination.page >= pagination.totalPages - 1}
                      className="inline-flex items-center justify-center size-10 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <span className="material-symbols-outlined">chevron_right</span>
                    </button>
                  </nav>
                )}
              </>
            )}
          </main>
        </div>
      </div>
    </Layout>
  );
}
