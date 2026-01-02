import React, { useState, useEffect } from "react";
import axiosInstance from "../utils/axiosInstance";
import AdminSidebar from "../components/AdminSidebar";

export default function LawyerManagement() {
  const [lawyers, setLawyers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
  });
  const [filters, setFilters] = useState({
    search: "",
    status: "",
  });
  const [selectedLawyers, setSelectedLawyers] = useState([]);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showVerifyModal, setShowVerifyModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showCertificateModal, setShowCertificateModal] = useState(false);
  const [selectedLawyer, setSelectedLawyer] = useState(null);
  const [rejectReason, setRejectReason] = useState("");

  // Debounce search
  useEffect(() => {
    const timer = setTimeout(() => {
      fetchLawyers();
    }, 500); // Wait 500ms after user stops typing
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.search]);

  // Fetch immediately when page/size/status changes
  useEffect(() => {
    fetchLawyers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pagination.page, pagination.size, filters.status]);

  const fetchLawyers = async () => {
    try {
      setLoading(true);
      const params = {
        page: pagination.page,
        size: pagination.size,
      };

      if (filters.status) params.status = filters.status;
      if (filters.search) params.keyword = filters.search;

      const response = await axiosInstance.get("/api/admin/lawyers", { params });

      if (response.data.success) {
        const data = response.data.data;
        setLawyers(data.content || []);
        setPagination((prev) => ({
          ...prev,
          totalElements: data.totalElements || 0,
          totalPages: data.totalPages || 0,
        }));
      }
    } catch (error) {
      console.error("Error fetching lawyers:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyLawyer = async () => {
    try {
      await axiosInstance.put(`/api/admin/lawyers/${selectedLawyer.lawyerId}/approve`);
      setShowVerifyModal(false);
      setShowDetailModal(false);
      fetchLawyers();
      alert("Đã xác minh luật sư thành công!");
    } catch (error) {
      console.error("Error verifying lawyer:", error);
      alert("Không thể xác minh luật sư. Vui lòng thử lại.");
    }
  };

  const handleRejectLawyer = async () => {
    try {
      await axiosInstance.put(`/api/admin/lawyers/${selectedLawyer.lawyerId}/reject`, { reason: rejectReason });
      setShowRejectModal(false);
      setShowDetailModal(false);
      setRejectReason("");
      fetchLawyers();
      alert("Đã từ chối luật sư thành công!");
    } catch (error) {
      console.error("Error rejecting lawyer:", error);
      alert("Không thể từ chối luật sư. Vui lòng thử lại.");
    }
  };

  const handleDeleteLawyer = async () => {
    try {
      await axiosInstance.delete(`/api/admin/lawyers/${selectedLawyer.lawyerId}`);
      setShowDeleteModal(false);
      setShowDetailModal(false);
      fetchLawyers();
      alert("Đã xóa luật sư thành công!");
    } catch (error) {
      console.error("Error deleting lawyer:", error);
      alert("Không thể xóa luật sư. Vui lòng thử lại.");
    }
  };

  const getStatusBadgeColor = (status) => {
    switch (status) {
      case "APPROVED":
      case "VERIFIED":
        return "bg-green-100 text-green-800 dark:bg-green-900/50 dark:text-green-300";
      case "PENDING":
        return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/50 dark:text-yellow-300";
      case "REJECTED":
        return "bg-red-100 text-red-800 dark:bg-red-900/50 dark:text-red-300";
      default:
        return "bg-slate-100 text-slate-800 dark:bg-slate-900/50 dark:text-slate-300";
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case "APPROVED":
      case "VERIFIED":
        return "Đã xác minh";
      case "PENDING":
        return "Đang chờ";
      case "REJECTED":
        return "Bị từ chối";
      default:
        return status;
    }
  };

  const getAvatarUrl = (lawyer) => {
    if (lawyer.avatarUrl) {
      return lawyer.avatarUrl.startsWith("http")
        ? lawyer.avatarUrl
        : `http://localhost:8080${lawyer.avatarUrl}`;
    }
    const initials = lawyer.fullName?.split(" ").map(n => n[0]).join("") || "L";
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(initials)}&background=137fec&color=fff&size=128`;
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleDateString("vi-VN");
  };

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedLawyers(lawyers.map(l => l.lawyerId));
    } else {
      setSelectedLawyers([]);
    }
  };

  const handleSelectLawyer = (lawyerId) => {
    setSelectedLawyers(prev => 
      prev.includes(lawyerId) 
        ? prev.filter(id => id !== lawyerId)
        : [...prev, lawyerId]
    );
  };

  if (loading && lawyers.length === 0) {
    return (
      <div className="bg-background-light dark:bg-background-dark min-h-screen flex">
        <AdminSidebar />
        <main className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-slate-600 dark:text-slate-400">Đang tải dữ liệu...</p>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="bg-background-light dark:bg-background-dark min-h-screen flex">
      <AdminSidebar />

      <main className="flex-1 flex flex-col overflow-y-auto">
        {/* Header */}
        <div className="p-6 lg:p-8 flex-shrink-0">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-slate-900 dark:text-white text-3xl font-bold tracking-tight">
              Quản lý luật sư
            </h1>
            <button
              onClick={fetchLawyers}
              className="flex h-9 shrink-0 items-center justify-center gap-x-2 rounded-lg bg-primary text-white pl-3 pr-4 hover:bg-primary/90"
            >
              <span className="material-symbols-outlined text-base">refresh</span>
              <p className="text-sm font-medium">Làm mới</p>
            </button>
          </div>

          {/* Filters */}
          <div className="bg-white dark:bg-slate-900/50 rounded-xl p-4 border border-slate-200 dark:border-slate-800">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="relative md:col-span-1">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 dark:text-slate-500">
                  search
                </span>
                <input
                  className="w-full h-10 bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700 rounded-lg pl-10 pr-4 text-sm focus:ring-primary focus:border-primary"
                  placeholder="Tìm kiếm theo email hoặc tên..."
                  type="text"
                  value={filters.search}
                  onChange={(e) => setFilters({ ...filters, search: e.target.value })}
                />
              </div>
              <div className="relative md:col-span-1">
                <select
                  className="w-full h-10 bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700 rounded-lg px-3 text-sm focus:ring-primary focus:border-primary"
                  value={filters.status}
                  onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                >
                  <option value="">Tất cả trạng thái</option>
                  <option value="PENDING">Đang chờ</option>
                  <option value="APPROVED">Đã xác minh</option>
                  <option value="REJECTED">Bị từ chối</option>
                </select>
              </div>
              <div className="flex items-center gap-2 justify-end md:col-span-1">
                <label className="text-sm text-slate-600 dark:text-slate-400" htmlFor="page-size">
                  Hiển thị:
                </label>
                <select
                  className="h-10 w-20 bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700 rounded-lg text-sm focus:ring-primary focus:border-primary"
                  id="page-size"
                  value={pagination.size}
                  onChange={(e) => setPagination({ ...pagination, size: Number(e.target.value), page: 0 })}
                >
                  <option value="10">10</option>
                  <option value="25">25</option>
                  <option value="50">50</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* Table */}
        <div className="flex-1 overflow-x-auto px-6 lg:px-8 pb-6 lg:pb-8">
          <div className="w-full align-middle inline-block">
            <div className="bg-white dark:bg-slate-900/50 rounded-xl border border-slate-200 dark:border-slate-800 overflow-hidden">
              <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-800">
                <thead className="bg-slate-50 dark:bg-slate-800/50">
                  <tr>
                    <th className="p-4" scope="col">
                      <input
                        className="h-4 w-4 rounded border-slate-300 dark:border-slate-600 text-primary focus:ring-primary"
                        type="checkbox"
                        checked={selectedLawyers.length === lawyers.length && lawyers.length > 0}
                        onChange={handleSelectAll}
                      />
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      STT
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      Luật sư
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      Trạng thái
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      Ngày đăng ký
                    </th>
                    <th className="px-4 py-3 text-right text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      Thao tác
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
                  {lawyers.map((lawyer, index) => (
                    <tr key={lawyer.lawyerId}>
                      <td className="p-4">
                        <input
                          className="h-4 w-4 rounded border-slate-300 dark:border-slate-600 text-primary focus:ring-primary"
                          type="checkbox"
                          checked={selectedLawyers.includes(lawyer.lawyerId)}
                          onChange={() => handleSelectLawyer(lawyer.lawyerId)}
                        />
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-500 dark:text-slate-400">
                        {pagination.page * pagination.size + index + 1}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap">
                        <div className="flex items-center gap-3">
                          <img
                            alt={lawyer.fullName}
                            className="h-10 w-10 rounded-full object-cover"
                            src={getAvatarUrl(lawyer)}
                          />
                          <div>
                            <p className="text-sm font-medium text-slate-900 dark:text-white">
                              {lawyer.fullName}
                            </p>
                            <p className="text-xs text-slate-500 dark:text-slate-400">
                              {lawyer.email}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusBadgeColor(lawyer.verificationStatus)}`}>
                          {getStatusText(lawyer.verificationStatus)}
                        </span>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-600 dark:text-slate-300">
                        {formatDate(lawyer.createdAt)}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-right">
                        <div className="flex items-center justify-end gap-1">
                          {lawyer.verificationStatus === "PENDING" && (
                            <>
                              <button
                                onClick={() => {
                                  setSelectedLawyer(lawyer);
                                  setShowVerifyModal(true);
                                }}
                                className="p-2 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 text-green-600 dark:text-green-500"
                                title="Xác minh"
                              >
                                <span className="material-symbols-outlined text-lg">check_circle</span>
                              </button>
                              <button
                                onClick={() => {
                                  setSelectedLawyer(lawyer);
                                  setShowRejectModal(true);
                                }}
                                className="p-2 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 text-red-600 dark:text-red-500"
                                title="Từ chối"
                              >
                                <span className="material-symbols-outlined text-lg">cancel</span>
                              </button>
                            </>
                          )}
                          <button
                            onClick={() => {
                              setSelectedLawyer(lawyer);
                              setShowDetailModal(true);
                            }}
                            className="p-2 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400"
                            title="Xem chi tiết"
                          >
                            <span className="material-symbols-outlined text-lg">visibility</span>
                          </button>
                          <button
                            onClick={() => {
                              setSelectedLawyer(lawyer);
                              setShowDeleteModal(true);
                            }}
                            className="p-2 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400"
                            title="Xóa"
                          >
                            <span className="material-symbols-outlined text-lg">delete</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Pagination */}
        <div className="px-6 lg:px-8 pb-4 flex-shrink-0">
          <div className="flex flex-wrap justify-between items-center gap-4 bg-white dark:bg-slate-900/50 rounded-xl p-4 border border-slate-200 dark:border-slate-800">
            <p className="text-sm text-slate-600 dark:text-slate-400">
              Hiển thị{" "}
              <span className="font-semibold text-slate-900 dark:text-white">
                {pagination.page * pagination.size + 1}-
                {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)}
              </span>{" "}
              trên{" "}
              <span className="font-semibold text-slate-900 dark:text-white">
                {pagination.totalElements}
              </span>{" "}
              luật sư
            </p>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setPagination({ ...pagination, page: pagination.page - 1 })}
                disabled={pagination.page === 0}
                className="inline-flex items-center justify-center h-9 w-9 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800/50 text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50"
              >
                <span className="material-symbols-outlined text-lg">chevron_left</span>
              </button>
              <span className="text-sm text-slate-600 dark:text-slate-400">Trang</span>
              <input
                className="h-9 w-12 text-center rounded-lg border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800/50 text-sm"
                type="number"
                value={pagination.page + 1}
                onChange={(e) => {
                  const newPage = Math.max(0, Math.min(Number(e.target.value) - 1, pagination.totalPages - 1));
                  setPagination({ ...pagination, page: newPage });
                }}
              />
              <span className="text-sm text-slate-600 dark:text-slate-400">
                trên {pagination.totalPages}
              </span>
              <button
                onClick={() => setPagination({ ...pagination, page: pagination.page + 1 })}
                disabled={pagination.page >= pagination.totalPages - 1}
                className="inline-flex items-center justify-center h-9 w-9 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800/50 text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50"
              >
                <span className="material-symbols-outlined text-lg">chevron_right</span>
              </button>
            </div>
          </div>
        </div>
      </main>

      {/* Detail Modal */}
      {showDetailModal && selectedLawyer && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-slate-200 dark:border-slate-800">
              <div className="flex justify-between items-center">
                <h2 className="text-xl font-bold text-slate-900 dark:text-white">
                  Thông tin luật sư
                </h2>
                {selectedLawyer.certificateUrl && (
                  <button
                    onClick={() => setShowCertificateModal(true)}
                    className="flex items-center gap-2 px-3 py-1.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
                  >
                    <span className="material-symbols-outlined text-lg">description</span>
                    Xem chứng chỉ
                  </button>
                )}
              </div>
            </div>
            <div className="p-6 space-y-4">
              <div className="flex items-center gap-4">
                <img
                  src={getAvatarUrl(selectedLawyer)}
                  alt={selectedLawyer.fullName}
                  className="w-20 h-20 rounded-full object-cover"
                />
                <div>
                  <h3 className="text-lg font-semibold text-slate-900 dark:text-white">
                    {selectedLawyer.fullName}
                  </h3>
                  <p className="text-sm text-slate-500 dark:text-slate-400">
                    {selectedLawyer.email}
                  </p>
                </div>
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">Số điện thoại</p>
                  <p className="text-slate-900 dark:text-white font-medium">
                    {selectedLawyer.phoneNumber || "N/A"}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">Số giấy phép</p>
                  <p className="text-slate-900 dark:text-white font-medium">
                    {selectedLawyer.barLicenseId || "N/A"}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">Liên đoàn</p>
                  <p className="text-slate-900 dark:text-white font-medium">
                    {selectedLawyer.barAssociationName || "N/A"}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">Trạng thái</p>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusBadgeColor(selectedLawyer.verificationStatus)}`}>
                    {getStatusText(selectedLawyer.verificationStatus)}
                  </span>
                </div>
              </div>

              <div>
                <p className="text-sm text-slate-500 dark:text-slate-400 mb-2 text-center">Chuyên môn</p>
                <div className="flex flex-wrap gap-2 justify-center">
                  {selectedLawyer.specializations && selectedLawyer.specializations.length > 0 ? (
                    selectedLawyer.specializations.map((spec, idx) => (
                      <span key={idx} className="px-3 py-1 bg-primary/10 text-primary rounded-full text-sm">
                        {spec}
                      </span>
                    ))
                  ) : (
                    <p className="text-slate-500">Không có</p>
                  )}
                </div>
              </div>

              <div>
                <p className="text-sm text-slate-500 dark:text-slate-400 mb-2">Mô tả</p>
                <p className="text-slate-900 dark:text-white">
                  {selectedLawyer.bio || "Không có mô tả"}
                </p>
              </div>

              <div>
                <p className="text-sm text-slate-500 dark:text-slate-400">Ngày đăng ký</p>
                <p className="text-slate-900 dark:text-white font-medium">
                  {formatDate(selectedLawyer.createdAt)}
                </p>
              </div>
            </div>
            <div className="p-6 border-t border-slate-200 dark:border-slate-800 flex justify-end gap-3">
              <button
                onClick={() => setShowDetailModal(false)}
                className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
              >
                Đóng
              </button>
              {selectedLawyer.verificationStatus === "PENDING" && (
                <>
                  <button
                    onClick={() => {
                      setShowDetailModal(false);
                      setShowVerifyModal(true);
                    }}
                    className="px-4 py-2 rounded-lg bg-green-600 text-white hover:bg-green-700"
                  >
                    ✓ Xác minh
                  </button>
                  <button
                    onClick={() => {
                      setShowDetailModal(false);
                      setShowRejectModal(true);
                    }}
                    className="px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700"
                  >
                    ✗ Từ chối
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Verify Modal */}
      {showVerifyModal && selectedLawyer && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-md w-full p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">
              ⚠️ Xác nhận xác minh luật sư?
            </h3>
            <p className="text-slate-600 dark:text-slate-400 mb-6">
              Bạn có chắc muốn xác minh luật sư <strong>"{selectedLawyer.fullName}"</strong>?<br/>
              Sau khi xác minh, luật sư có thể hoạt động trên hệ thống.
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setShowVerifyModal(false)}
                className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
              >
                Hủy
              </button>
              <button
                onClick={handleVerifyLawyer}
                className="px-4 py-2 rounded-lg bg-green-600 text-white hover:bg-green-700"
              >
                Xác minh
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reject Modal */}
      {showRejectModal && selectedLawyer && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-md w-full p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">
              ❌ Từ chối xác minh luật sư
            </h3>
            <p className="text-slate-600 dark:text-slate-400 mb-4">
              Luật sư: <strong>{selectedLawyer.fullName}</strong>
            </p>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
              Lý do từ chối:
            </label>
            <textarea
              className="w-full h-24 rounded-lg border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:ring-primary focus:border-primary"
              placeholder="Nhập lý do..."
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
            />
            <div className="flex justify-end gap-3 mt-6">
              <button
                onClick={() => {
                  setShowRejectModal(false);
                  setRejectReason("");
                }}
                className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
              >
                Hủy
              </button>
              <button
                onClick={handleRejectLawyer}
                className="px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700"
              >
                Từ chối
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {showDeleteModal && selectedLawyer && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-md w-full p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">
              🗑️ Xóa luật sư vĩnh viễn?
            </h3>
            <p className="text-slate-600 dark:text-slate-400 mb-6">
              <strong className="text-red-600">Hành động này không thể hoàn tác!</strong><br/>
              Bạn có chắc muốn xóa luật sư <strong>"{selectedLawyer.fullName}"</strong>?
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setShowDeleteModal(false)}
                className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
              >
                Hủy
              </button>
              <button
                onClick={handleDeleteLawyer}
                className="px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700"
              >
                Xóa
              </button>
            </div>
          </div>
        </div>
      )}
      {/* Certificate Modal */}
      {showCertificateModal && selectedLawyer && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-slate-200 dark:border-slate-800">
              <div className="flex justify-between items-center">
                <h2 className="text-xl font-bold text-slate-900 dark:text-white">
                  Chứng chỉ hành nghề - {selectedLawyer.fullName}
                </h2>
                <button
                  onClick={() => setShowCertificateModal(false)}
                  className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-300"
                >
                  <span className="material-symbols-outlined text-2xl">close</span>
                </button>
              </div>
            </div>
            <div className="p-6">
              <div className="flex justify-center">
                <img
                  src={selectedLawyer.certificateUrl.startsWith("http") 
                    ? selectedLawyer.certificateUrl 
                    : `http://localhost:8080${selectedLawyer.certificateUrl}`}
                  alt={`Chứng chỉ hành nghề - ${selectedLawyer.fullName}`}
                  className="max-w-full max-h-[70vh] object-contain rounded-lg shadow-lg"
                  onError={(e) => {
                    e.target.style.display = 'none';
                    e.target.nextSibling.style.display = 'block';
                  }}
                />
                <div className="hidden text-center text-slate-500 dark:text-slate-400">
                  <span className="material-symbols-outlined text-6xl mb-4">broken_image</span>
                  <p>Không thể tải ảnh chứng chỉ</p>
                </div>
              </div>
            </div>
            <div className="p-6 border-t border-slate-200 dark:border-slate-800 flex justify-end">
              <button
                onClick={() => setShowCertificateModal(false)}
                className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
