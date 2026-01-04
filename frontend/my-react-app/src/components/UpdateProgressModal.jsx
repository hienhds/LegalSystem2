import React, { useState } from "react";

export default function UpdateProgressModal({ isOpen, onClose, caseId, onSuccess }) {
  const [description, setDescription] = useState("");
  const [status, setStatus] = useState("IN_PROGRESS");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const statusOptions = [
    { value: "PENDING", label: "Chờ tiếp nhận", color: "text-gray-600" },
    { value: "IN_PROGRESS", label: "Đang thực hiện", color: "text-blue-600" },
    { value: "RESOLVED", label: "Đã giải quyết", color: "text-green-600" },
    { value: "CLOSED", label: "Đã đóng", color: "text-gray-600" },
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!description.trim()) {
      setError("Vui lòng nhập mô tả cập nhật");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem("accessToken");
      const userId = localStorage.getItem("userId");

      const response = await fetch(`http://localhost:8080/api/cases/${caseId}/progress`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
          "X-User-Id": userId || "",
          "X-User-Role": "LAWYER",
        },
        body: JSON.stringify({
          description: description.trim(),
          status: status,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Không thể cập nhật tiến độ");
      }

      const data = await response.json();
      
      if (data.code === 200) {
        // Reset form
        setDescription("");
        setStatus("IN_PROGRESS");
        // Call success callback
        if (onSuccess) onSuccess();
        // Close modal
        onClose();
      } else {
        throw new Error(data.message || "Không thể cập nhật tiến độ");
      }
    } catch (err) {
      console.error("Error updating progress:", err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setDescription("");
      setStatus("IN_PROGRESS");
      setError(null);
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
        onClick={handleClose}
      ></div>

      {/* Modal */}
      <div className="flex min-h-full items-center justify-center p-4">
        <div className="relative bg-white dark:bg-slate-800 rounded-2xl shadow-2xl max-w-2xl w-full p-6 transform transition-all">
          {/* Header */}
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-blue-50 dark:bg-blue-900/30 rounded-lg">
                <span className="material-symbols-outlined text-blue-600 dark:text-blue-400 text-2xl">
                  update
                </span>
              </div>
              <div>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white">
                  Cập nhật tiến độ
                </h3>
                <p className="text-sm text-slate-600 dark:text-slate-400">
                  Ghi chú mới về tiến trình vụ án #{caseId}
                </p>
              </div>
            </div>
            <button
              onClick={handleClose}
              disabled={loading}
              className="p-2 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg transition-colors disabled:opacity-50"
            >
              <span className="material-symbols-outlined text-slate-400">close</span>
            </button>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Status Selection */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                Trạng thái vụ án
              </label>
              <div className="grid grid-cols-2 gap-3">
                {statusOptions.map((option) => (
                  <button
                    key={option.value}
                    type="button"
                    onClick={() => setStatus(option.value)}
                    className={`p-4 rounded-xl border-2 transition-all text-left ${
                      status === option.value
                        ? "border-blue-600 bg-blue-50 dark:bg-blue-900/20"
                        : "border-slate-200 dark:border-slate-700 hover:border-blue-300 dark:hover:border-blue-700"
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      <div
                        className={`w-3 h-3 rounded-full ${
                          status === option.value
                            ? "bg-blue-600"
                            : "bg-slate-300 dark:bg-slate-600"
                        }`}
                      ></div>
                      <span
                        className={`font-medium ${
                          status === option.value
                            ? option.color
                            : "text-slate-600 dark:text-slate-400"
                        }`}
                      >
                        {option.label}
                      </span>
                    </div>
                  </button>
                ))}
              </div>
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                Mô tả chi tiết <span className="text-red-500">*</span>
              </label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Ví dụ: Đã hoàn thành việc thu thập tài liệu. Tiến hành soạn đơn kiện..."
                rows={6}
                className="w-full px-4 py-3 border border-slate-300 dark:border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder-slate-400 transition-all resize-none"
                disabled={loading}
              />
              <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">
                Ghi chú về tiến trình, kết quả, hoặc các hành động đã thực hiện
              </p>
            </div>

            {/* Error Message */}
            {error && (
              <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl flex items-start gap-3">
                <span className="material-symbols-outlined text-red-500 text-xl">error</span>
                <p className="text-sm text-red-800 dark:text-red-300">{error}</p>
              </div>
            )}

            {/* Actions */}
            <div className="flex gap-3 pt-4">
              <button
                type="button"
                onClick={handleClose}
                disabled={loading}
                className="flex-1 px-6 py-3 border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-300 font-medium rounded-xl hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Hủy
              </button>
              <button
                type="submit"
                disabled={loading || !description.trim()}
                className="flex-1 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl shadow-lg shadow-blue-500/30 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    <span>Đang xử lý...</span>
                  </>
                ) : (
                  <>
                    <span className="material-symbols-outlined text-xl">check_circle</span>
                    <span>Lưu cập nhật</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
