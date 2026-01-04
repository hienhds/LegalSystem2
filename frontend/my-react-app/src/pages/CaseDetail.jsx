import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "../components/Header";
import Footer from "../components/Footer";
import UpdateProgressModal from "../components/UpdateProgressModal";

const API_BASE_URL = "http://localhost:8080/api";

export default function CaseDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [caseData, setCaseData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
  const [uploadingDocument, setUploadingDocument] = useState(false);
  const [uploadError, setUploadError] = useState(null);

  useEffect(() => {
    fetchCaseDetail();
  }, [id]);

  const handleUpdateSuccess = () => {
    // Refresh case data after successful update
    fetchCaseDetail();
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Reset error
    setUploadError(null);
    setUploadingDocument(true);

    try {
      const token = localStorage.getItem("accessToken");
      const userId = localStorage.getItem("userId");

      const formData = new FormData();
      formData.append("file", file);

      const response = await fetch(`${API_BASE_URL}/cases/${id}/documents`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "X-User-Id": userId || "",
          "X-User-Role": "LAWYER",
        },
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Không thể tải lên tài liệu");
      }

      const data = await response.json();
      
      if (data.code === 200) {
        // Refresh case data to show new document
        await fetchCaseDetail();
        alert("Tải lên tài liệu thành công!");
      } else {
        throw new Error(data.message || "Không thể tải lên tài liệu");
      }
    } catch (err) {
      console.error("Error uploading document:", err);
      setUploadError(err.message);
      alert("Lỗi: " + err.message);
    } finally {
      setUploadingDocument(false);
      // Reset file input
      event.target.value = "";
    }
  };

  const handleDownloadDocument = async (documentId, fileName) => {
    try {
      const token = localStorage.getItem("accessToken");
      const userId = localStorage.getItem("userId");

      // Download file directly
      const response = await fetch(`${API_BASE_URL}/cases/${id}/documents/${documentId}/download`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "X-User-Id": userId || "",
        },
      });

      if (!response.ok) {
        throw new Error("Không thể tải xuống tài liệu");
      }

      // Get the blob from response
      const blob = await response.blob();
      
      // Create download link
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = fileName || `document_${documentId}`;
      document.body.appendChild(a);
      a.click();
      
      // Cleanup
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      console.error("Error downloading document:", err);
      alert("Lỗi khi tải xuống: " + err.message);
    }
  };

  const handleDeleteDocument = async (documentId, fileName) => {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa tài liệu "${fileName}"?`)) {
      return;
    }

    try {
      const token = localStorage.getItem("accessToken");
      const userId = localStorage.getItem("userId");

      const response = await fetch(`${API_BASE_URL}/cases/${id}/documents/${documentId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
          "X-User-Id": userId || "",
          "X-User-Role": "LAWYER",
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Không thể xóa tài liệu");
      }

      const data = await response.json();
      
      if (data.code === 200) {
        // Refresh case data
        await fetchCaseDetail();
        alert("Đã xóa tài liệu thành công!");
      } else {
        throw new Error(data.message || "Không thể xóa tài liệu");
      }
    } catch (err) {
      console.error("Error deleting document:", err);
      alert("Lỗi: " + err.message);
    }
  };

  const handleDeleteCase = async () => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa vụ án này? Hành động này không thể hoàn tác.")) {
      return;
    }

    try {
      setLoading(true);
      const token = localStorage.getItem("accessToken");
      const userId = localStorage.getItem("userId");

      const response = await fetch(`${API_BASE_URL}/cases/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
          "X-User-Id": userId || "",
          "X-User-Role": "LAWYER",
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Không thể xóa vụ án");
      }

      const data = await response.json();
      
      if (data.code === 200) {
        // Navigate back to cases list
        alert("Đã xóa vụ án thành công");
        navigate("/cases");
      } else {
        throw new Error(data.message || "Không thể xóa vụ án");
      }
    } catch (err) {
      console.error("Error deleting case:", err);
      alert(err.message);
      setLoading(false);
    }
  };

  const fetchCaseDetail = async () => {
    try {
      setLoading(true);
      setError(null);

      const token = localStorage.getItem("accessToken");
      if (!token) {
        setError("Vui lòng đăng nhập để xem chi tiết vụ án");
        setLoading(false);
        return;
      }

      const response = await fetch(`${API_BASE_URL}/cases/${id}`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        throw new Error(`Lỗi ${response.status}: Không thể tải thông tin vụ án`);
      }

      const data = await response.json();
      console.log("Case detail:", data);

      if (data.code === 200 && data.result) {
        setCaseData(data.result);
      } else {
        throw new Error(data.message || "Không thể tải thông tin vụ án");
      }
    } catch (err) {
      console.error("Error fetching case detail:", err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  const getStatusInfo = (status) => {
    const statusMap = {
      PENDING: { label: "Chờ xử lý", color: "gray" },
      IN_PROGRESS: { label: "Đang thực hiện", color: "blue" },
      RESOLVED: { label: "Đã giải quyết", color: "green" },
      CLOSED: { label: "Đã đóng", color: "green" },
    };
    return statusMap[status] || { label: status, color: "gray" };
  };

  const getStatusBadgeClass = (color) => {
    const colors = {
      blue: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200",
      gray: "bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200",
      green: "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200",
    };
    return colors[color] || colors.gray;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-900">
        <Header />
        <main className="flex-grow flex items-center justify-center">
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-custom-blue-dark mx-auto mb-4"></div>
            <p className="text-slate-600 dark:text-slate-400">Đang tải thông tin vụ án...</p>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-900">
        <Header />
        <main className="flex-grow flex items-center justify-center">
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl p-8 text-center max-w-md">
            <span className="material-symbols-outlined text-red-500 text-5xl mb-4">error</span>
            <p className="text-red-800 dark:text-red-300 font-medium mb-4">{error}</p>
            <button
              onClick={() => navigate("/cases")}
              className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm transition-colors"
            >
              Quay lại danh sách
            </button>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  if (!caseData) return null;

  const statusInfo = getStatusInfo(caseData.status);

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-900 transition-colors duration-300">
      <Header />

      <main className="flex-grow py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto">
          {/* Breadcrumb */}
          <nav aria-label="Breadcrumb" className="flex mb-6">
            <ol className="inline-flex items-center space-x-1 md:space-x-3">
              <li className="inline-flex items-center">
                <button
                  onClick={() => navigate("/home")}
                  className="inline-flex items-center text-sm font-medium text-slate-500 dark:text-slate-400 hover:text-custom-blue-dark dark:hover:text-custom-blue-dark"
                >
                  <span className="material-symbols-outlined mr-2 text-lg">home</span>
                  Trang chủ
                </button>
              </li>
              <li>
                <div className="flex items-center">
                  <span className="material-symbols-outlined text-slate-400 text-lg">chevron_right</span>
                  <button
                    onClick={() => navigate("/cases")}
                    className="ml-1 text-sm font-medium text-slate-500 dark:text-slate-400 hover:text-custom-blue-dark dark:hover:text-custom-blue-dark md:ml-2"
                  >
                    Danh sách vụ án
                  </button>
                </div>
              </li>
              <li aria-current="page">
                <div className="flex items-center">
                  <span className="material-symbols-outlined text-slate-400 text-lg">chevron_right</span>
                  <span className="ml-1 text-sm font-medium text-slate-700 dark:text-slate-200 md:ml-2">
                    Chi tiết vụ án
                  </span>
                </div>
              </li>
            </ol>
          </nav>

          {/* Header Section */}
          <div className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-slate-200 dark:border-slate-700 p-6 mb-6">
            <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
              <div>
                <div className="flex items-center gap-3 mb-2">
                  <h1 className="text-3xl font-bold text-slate-900 dark:text-white tracking-tight">
                    {caseData.title}
                  </h1>
                  <span
                    className={`${getStatusBadgeClass(statusInfo.color)} text-xs font-semibold px-2.5 py-0.5 rounded-full uppercase tracking-wide`}
                  >
                    {statusInfo.label}
                  </span>
                </div>
                <p className="text-slate-600 dark:text-slate-400 text-sm">
                  Quản lý các thông tin chi tiết liên quan đến hồ sơ pháp lý này.
                </p>
              </div>
              <div className="flex flex-col items-end gap-1 text-sm text-slate-600 dark:text-slate-400">
                <div className="flex items-center gap-1">
                  <span className="material-symbols-outlined text-base">calendar_today</span>
                  <span>
                    Ngày tạo: <span className="font-medium text-slate-900 dark:text-white">{formatDate(caseData.createdAt)}</span>
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Client and Lawyer Info */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-slate-200 dark:border-slate-700 p-6 flex items-start gap-4 transition-transform hover:scale-[1.01] duration-200">
              <div className="p-3 bg-purple-50 dark:bg-purple-900/30 rounded-lg">
                <span className="material-symbols-outlined text-purple-600 dark:text-purple-400 text-2xl">person</span>
              </div>
              <div>
                <h3 className="text-sm font-medium text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                  Khách hàng
                </h3>
                <p className="text-lg font-semibold text-slate-900 dark:text-white">{caseData.clientName || "N/A"}</p>
              </div>
            </div>
            <div className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-slate-200 dark:border-slate-700 p-6 flex items-start gap-4 transition-transform hover:scale-[1.01] duration-200">
              <div className="p-3 bg-teal-50 dark:bg-teal-900/30 rounded-lg">
                <span className="material-symbols-outlined text-teal-600 dark:text-teal-400 text-2xl">gavel</span>
              </div>
              <div>
                <h3 className="text-sm font-medium text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                  Luật sư phụ trách
                </h3>
                <p className="text-lg font-semibold text-slate-900 dark:text-white">{caseData.lawyerName || "N/A"}</p>
              </div>
            </div>
          </div>

          {/* Description */}
          <div className="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-slate-200 dark:border-slate-700 p-6 mb-6">
            <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-3 flex items-center gap-2">
              <span className="material-symbols-outlined text-custom-blue-dark">description</span>
              Mô tả vụ việc
            </h3>
            <div className="bg-slate-50 dark:bg-slate-800/50 p-4 rounded-lg border border-slate-100 dark:border-slate-700/50">
              <p className="text-slate-700 dark:text-slate-300 leading-relaxed whitespace-pre-wrap">
                {caseData.description || "Chưa có mô tả"}
              </p>
            </div>
          </div>

          {/* Progress and Documents Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Progress Timeline */}
            <div className="lg:col-span-2 bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-slate-200 dark:border-slate-700 p-6 flex flex-col h-full">
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
                  <span className="material-symbols-outlined text-custom-blue-dark">timeline</span>
                  Tiến độ xử lý
                </h3>
                <div className="flex gap-2">
                  <button 
                    onClick={handleDeleteCase}
                    disabled={loading}
                    className="flex items-center gap-1 bg-red-50 hover:bg-red-100 text-red-600 dark:bg-red-900/20 dark:hover:bg-red-900/30 dark:text-red-400 px-3 py-2 rounded-lg text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <span className="material-symbols-outlined text-base">delete</span>
                    Xóa hồ sơ
                  </button>
                  <button 
                    onClick={() => setIsUpdateModalOpen(true)}
                    className="flex items-center gap-1 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium shadow-sm transition-colors"
                  >
                    <span className="material-symbols-outlined text-base">edit</span>
                    Cập nhật
                  </button>
                </div>
              </div>

              {caseData.progressUpdates && caseData.progressUpdates.length > 0 ? (
                <div className="flex-grow space-y-4">
                  {caseData.progressUpdates.map((update, index) => (
                    <div key={update.id} className="flex gap-4">
                      <div className="flex flex-col items-center">
                        <div className="w-3 h-3 rounded-full bg-custom-blue-dark"></div>
                        {index < caseData.progressUpdates.length - 1 && (
                          <div className="w-0.5 flex-grow bg-slate-200 dark:bg-slate-700 mt-1"></div>
                        )}
                      </div>
                      <div className="flex-grow pb-6">
                        <div className="bg-slate-50 dark:bg-slate-800/50 p-4 rounded-lg border border-slate-100 dark:border-slate-700/50">
                          <div className="flex justify-between items-start mb-2">
                            <p className="text-sm text-slate-600 dark:text-slate-400">
                              {formatDate(update.updateDate)}
                            </p>
                          </div>
                          <p className="text-slate-700 dark:text-slate-300">{update.updateDescription}</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="flex-grow flex flex-col items-center justify-center py-12">
                  <div className="bg-slate-50 dark:bg-slate-800 rounded-full p-4 mb-3">
                    <span className="material-symbols-outlined text-slate-400 text-3xl">history</span>
                  </div>
                  <p className="text-slate-500 dark:text-slate-400 text-center italic">
                    Chưa có cập nhật nào được ghi nhận.
                  </p>
                  <p className="text-xs text-slate-400 dark:text-slate-500 mt-2 text-center max-w-xs">
                    Nhấn nút "Cập nhật" để thêm ghi chú mới về tiến trình vụ án.
                  </p>
                </div>
              )}
            </div>

            {/* Documents */}
            <div className="lg:col-span-1 bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-slate-200 dark:border-slate-700 p-6 flex flex-col h-full">
              <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-6 flex items-center gap-2">
                <span className="material-symbols-outlined text-custom-blue-dark">folder_open</span>
                Tài liệu hồ sơ
              </h3>

              {caseData.documents && caseData.documents.length > 0 ? (
                <div className="flex-grow space-y-3 mb-4">
                  {caseData.documents.map((doc) => (
                    <div
                      key={doc.id}
                      className="p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg border border-slate-200 dark:border-slate-700 hover:border-custom-blue-dark transition-colors group"
                    >
                      <div className="flex items-center gap-3">
                        <span className="material-symbols-outlined text-custom-blue-dark">description</span>
                        <div className="flex-grow min-w-0">
                          <p className="text-sm font-medium text-slate-900 dark:text-white truncate">
                            {doc.fileName || `Tài liệu #${doc.id}`}
                          </p>
                          <p className="text-xs text-slate-500">{formatDate(doc.uploadedAt)}</p>
                        </div>
                        <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                          <button
                            onClick={() => handleDownloadDocument(doc.id, doc.fileName)}
                            className="p-1.5 hover:bg-green-100 dark:hover:bg-green-900/30 rounded text-green-600 dark:text-green-400"
                            title="Tải xuống"
                          >
                            <span className="material-symbols-outlined text-base">download</span>
                          </button>
                          <button
                            onClick={() => handleDeleteDocument(doc.id, doc.fileName)}
                            className="p-1.5 hover:bg-red-100 dark:hover:bg-red-900/30 rounded text-red-600 dark:text-red-400"
                            title="Xóa"
                          >
                            <span className="material-symbols-outlined text-base">delete</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="flex-grow flex flex-col items-center justify-center py-8">
                  <span className="material-symbols-outlined text-slate-300 dark:text-slate-600 text-6xl mb-4">
                    folder_open
                  </span>
                  <p className="text-slate-500 dark:text-slate-400 text-sm mb-6 text-center italic">
                    Chưa có tài liệu nào.
                  </p>
                </div>
              )}

              <div className="mt-auto">
                <input
                  type="file"
                  id="document-upload"
                  className="hidden"
                  onChange={handleFileUpload}
                  disabled={uploadingDocument}
                  accept=".pdf,.doc,.docx,.xls,.xlsx,.txt,.jpg,.jpeg,.png"
                />
                <label
                  htmlFor="document-upload"
                  className={`w-full flex items-center justify-center gap-2 border-2 border-dashed border-custom-blue-dark text-custom-blue-dark hover:bg-blue-50 dark:hover:bg-blue-900/20 px-4 py-3 rounded-lg text-sm font-medium transition-all duration-200 group cursor-pointer ${
                    uploadingDocument ? "opacity-50 cursor-not-allowed" : ""
                  }`}
                >
                  {uploadingDocument ? (
                    <>
                      <div className="w-5 h-5 border-2 border-custom-blue-dark border-t-transparent rounded-full animate-spin"></div>
                      <span>Đang tải lên...</span>
                    </>
                  ) : (
                    <>
                      <span className="material-symbols-outlined group-hover:scale-110 transition-transform">
                        cloud_upload
                      </span>
                      Thêm tài liệu mới
                    </>
                  )}
                </label>
              </div>
            </div>
          </div>
        </div>
      </main>

      <Footer />

      {/* Update Progress Modal */}
      <UpdateProgressModal
        isOpen={isUpdateModalOpen}
        onClose={() => setIsUpdateModalOpen(false)}
        caseId={id}
        onSuccess={handleUpdateSuccess}
      />
    </div>
  );
}
