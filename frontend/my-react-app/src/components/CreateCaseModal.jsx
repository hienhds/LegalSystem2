import React, { useState, useEffect } from "react";

export default function CreateCaseModal({ isOpen, onClose, onSuccess }) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [clientId, setClientId] = useState("");
  const [clientEmail, setClientEmail] = useState("");
  const [selectedClient, setSelectedClient] = useState(null);
  const [clients, setClients] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [loading, setLoading] = useState(false);
  const [searchLoading, setSearchLoading] = useState(false);
  const [error, setError] = useState(null);

  // Search clients by name or email
  const searchClients = async (searchText) => {
    if (!searchText || searchText.length < 2) {
      setClients([]);
      setShowSuggestions(false);
      return;
    }

    try {
      setSearchLoading(true);
      const token = localStorage.getItem("accessToken");

      const response = await fetch(`http://localhost:8080/api/users/search-by-keyword?keyword=${encodeURIComponent(searchText)}`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (response.ok) {
        const data = await response.json();
        console.log("Search API response:", data);
        
        // Handle different response structures
        let clientList = [];
        if (data.success && data.data) {
          clientList = Array.isArray(data.data) ? data.data : [];
        } else if (Array.isArray(data)) {
          clientList = data;
        }
        
        // Show all users from search (không filter theo roles)
        console.log("All clients:", clientList);
        setClients(clientList);
        setShowSuggestions(clientList.length > 0);
      } else {
        console.error("Search failed with status:", response.status);
        setClients([]);
        setShowSuggestions(false);
      }
    } catch (err) {
      console.error("Error searching clients:", err);
      setClients([]);
      setShowSuggestions(false);
    } finally {
      setSearchLoading(false);
    }
  };

  // Handle email input change with debounce
  useEffect(() => {
    const timer = setTimeout(() => {
      searchClients(clientEmail);
    }, 300);

    return () => clearTimeout(timer);
  }, [clientEmail]);

  const handleSelectClient = (client) => {
    setSelectedClient(client);
    setClientId(client.userId);
    setClientEmail(client.email);
    setShowSuggestions(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!title.trim()) {
      setError("Vui lòng nhập tiêu đề vụ án");
      return;
    }
    if (!description.trim()) {
      setError("Vui lòng nhập mô tả vụ án");
      return;
    }
    if (!clientId) {
      setError("Vui lòng chọn khách hàng");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem("accessToken");
      const userId = localStorage.getItem("userId");

      const response = await fetch("http://localhost:8080/api/cases", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
          "X-User-Id": userId || "",
          "X-User-Role": "LAWYER",
        },
        body: JSON.stringify({
          title: title.trim(),
          description: description.trim(),
          clientId: parseInt(clientId),
        }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Không thể tạo vụ án");
      }

      const data = await response.json();

      if (data.code === 200) {
        // Reset form
        setTitle("");
        setDescription("");
        setClientId("");
        setClientEmail("");
        setSelectedClient(null);
        setClients([]);
        // Call success callback
        if (onSuccess) onSuccess();
        // Close modal
        onClose();
      } else {
        throw new Error(data.message || "Không thể tạo vụ án");
      }
    } catch (err) {
      console.error("Error creating case:", err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setTitle("");
      setDescription("");
      setClientId("");
      setClientEmail("");
      setSelectedClient(null);
      setClients([]);
      setShowSuggestions(false);
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
        <div className="relative bg-white dark:bg-slate-800 rounded-2xl shadow-2xl max-w-3xl w-full p-6 transform transition-all">
          {/* Header */}
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-blue-50 dark:bg-blue-900/30 rounded-lg">
                <span className="material-symbols-outlined text-blue-600 dark:text-blue-400 text-2xl">
                  add_circle
                </span>
              </div>
              <div>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white">Tạo vụ án mới</h3>
                <p className="text-sm text-slate-600 dark:text-slate-400">
                  Khởi tạo hồ sơ pháp lý mới cho khách hàng
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
            {/* Title */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                Tiêu đề vụ án <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Ví dụ: Tranh chấp hợp đồng bất động sản"
                className="w-full px-4 py-3 border border-slate-300 dark:border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder-slate-400 transition-all"
                disabled={loading}
              />
            </div>

            {/* Client Selection */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                Khách hàng <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <div className="relative">
                  <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none">
                    search
                  </span>
                  <input
                    type="text"
                    value={clientEmail}
                    onChange={(e) => {
                      setClientEmail(e.target.value);
                      setSelectedClient(null);
                      setClientId("");
                    }}
                    onFocus={() => clients.length > 0 && setShowSuggestions(true)}
                    onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
                    placeholder="Nhập tên hoặc email khách hàng để tìm kiếm..."
                    className="w-full pl-10 pr-10 py-3 border border-slate-300 dark:border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder-slate-400 transition-all"
                    disabled={loading}
                  />
                  {searchLoading && (
                    <div className="absolute right-3 top-1/2 -translate-y-1/2">
                      <div className="w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                  )}
                </div>
                
                {/* Selected Client Display */}
                {selectedClient && (
                  <div className="mt-2 p-3 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="material-symbols-outlined text-blue-600 dark:text-blue-400">person</span>
                      <div>
                        <p className="text-sm font-medium text-slate-900 dark:text-white">{selectedClient.fullName}</p>
                        <p className="text-xs text-slate-600 dark:text-slate-400">{selectedClient.email}</p>
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedClient(null);
                        setClientId("");
                        setClientEmail("");
                      }}
                      className="p-1 hover:bg-blue-100 dark:hover:bg-blue-800 rounded"
                    >
                      <span className="material-symbols-outlined text-sm text-slate-400">close</span>
                    </button>
                  </div>
                )}

                {/* Suggestions Dropdown */}
                {showSuggestions && clients.length > 0 && !selectedClient && (
                  <div className="absolute top-full left-0 right-0 mt-1 bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded-xl shadow-lg z-20 max-h-60 overflow-y-auto">
                    {clients.map((client) => (
                      <div
                        key={client.userId}
                        className="flex items-center gap-3 px-4 py-3 hover:bg-slate-100 dark:hover:bg-slate-700 cursor-pointer transition-colors border-b border-slate-100 dark:border-slate-700 last:border-0"
                        onMouseDown={(e) => {
                          e.preventDefault();
                          handleSelectClient(client);
                        }}
                      >
                        <span className="material-symbols-outlined text-slate-400 dark:text-slate-500">person</span>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium text-slate-900 dark:text-slate-50 truncate">
                            {client.fullName || "Chưa có tên"}
                          </p>
                          <p className="text-xs text-slate-500 dark:text-slate-400 truncate">{client.email}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">
                Chọn khách hàng liên quan đến vụ án này
              </p>
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                Mô tả chi tiết <span className="text-red-500">*</span>
              </label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Mô tả chi tiết về vụ án, bối cảnh, yêu cầu pháp lý..."
                rows={6}
                className="w-full px-4 py-3 border border-slate-300 dark:border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder-slate-400 transition-all resize-none"
                disabled={loading}
              />
              <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">
                Cung cấp thông tin đầy đủ về vụ việc, các bên liên quan, và yêu cầu pháp lý
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
                disabled={loading || !title.trim() || !description.trim() || !clientId}
                className="flex-1 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl shadow-lg shadow-blue-500/30 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    <span>Đang tạo...</span>
                  </>
                ) : (
                  <>
                    <span className="material-symbols-outlined text-xl">check_circle</span>
                    <span>Tạo vụ án</span>
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
