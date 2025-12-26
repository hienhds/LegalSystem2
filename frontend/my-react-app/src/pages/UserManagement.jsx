import React, { useState, useEffect } from "react";
import axiosInstance from "../utils/axiosInstance";
import AdminSidebar from "../components/AdminSidebar";

export default function UserManagement() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  });
  const [filters, setFilters] = useState({
    search: "",
  });
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showLockModal, setShowLockModal] = useState(false);
  const [showUnlockModal, setShowUnlockModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  useEffect(() => {
    fetchUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pagination.page, pagination.size]);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const params = {
        page: pagination.page,
        size: pagination.size,
      };

      if (filters.search) params.search = filters.search;

      const response = await axiosInstance.get("/api/admin/users", { params });

      if (response.data.success) {
        const data = response.data.data;
        setUsers(data.content || []);
        setPagination((prev) => ({
          ...prev,
          totalElements: data.totalElements || 0,
          totalPages: data.totalPages || 0,
        }));
      }
    } catch (error) {
      console.error("Error fetching users:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPagination((prev) => ({ ...prev, page: 0 }));
    fetchUsers();
  };

  const handleLockUser = async () => {
    try {
      await axiosInstance.put(`/api/admin/users/${selectedUser.userId}/lock`);
      setShowLockModal(false);
      fetchUsers();
    } catch (error) {
      console.error("Error locking user:", error);
      alert("Không thể khóa tài khoản. Vui lòng thử lại.");
    }
  };

  const handleUnlockUser = async () => {
    try {
      await axiosInstance.put(`/api/admin/users/${selectedUser.userId}/unlock`);
      setShowUnlockModal(false);
      fetchUsers();
    } catch (error) {
      console.error("Error unlocking user:", error);
      alert("Không thể mở khóa tài khoản. Vui lòng thử lại.");
    }
  };

  const handleDeleteUser = async () => {
    try {
      await axiosInstance.delete(`/api/admin/users/${selectedUser.userId}`);
      setShowDeleteModal(false);
      fetchUsers();
    } catch (error) {
      console.error("Error deleting user:", error);
      alert("Không thể xóa tài khoản. Vui lòng thử lại.");
    }
  };

  const getRoleBadgeColor = (role) => {
    switch (role) {
      case "ADMIN":
        return "bg-amber-100 text-amber-800 dark:bg-amber-900/50 dark:text-amber-300";
      case "LAWYER":
        return "bg-purple-100 text-purple-800 dark:bg-purple-900/50 dark:text-purple-300";
      default:
        return "bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300";
    }
  };

  const getAvatarUrl = (user) => {
    if (user.avatarUrl) {
      return user.avatarUrl.startsWith("http")
        ? user.avatarUrl
        : `http://localhost:8080${user.avatarUrl}`;
    }
    const initials = user.fullName?.split(" ").map(n => n[0]).join("") || "U";
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(initials)}&background=137fec&color=fff&size=128`;
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleDateString("vi-VN");
  };

  const currentUserEmail = localStorage.getItem("userEmail");

  if (loading && users.length === 0) {
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

      <main className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 overflow-y-auto">
        <div className="flex-1 flex flex-col max-w-7xl mx-auto w-full">
          {/* Header */}
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
            <h1 className="text-slate-900 dark:text-white text-2xl lg:text-3xl font-bold tracking-tight">
              Quản lý người dùng
            </h1>
            <div className="flex items-center gap-2 w-full sm:w-auto">
              <button
                onClick={fetchUsers}
                className="flex h-9 shrink-0 items-center justify-center gap-x-1.5 rounded-lg bg-white dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 px-3 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300 text-sm font-medium w-1/2 sm:w-auto"
              >
                <span className="material-symbols-outlined text-base">refresh</span>
                <span>Làm mới</span>
              </button>
            </div>
          </div>

          {/* Table Container */}
          <div className="bg-white dark:bg-slate-900/50 rounded-xl border border-slate-200 dark:border-slate-800 flex-1 flex flex-col">
            {/* Filters */}
            <div className="p-4 border-b border-slate-200 dark:border-slate-800 flex flex-col sm:flex-row items-center gap-3">
              <form onSubmit={handleSearch} className="relative w-full sm:max-w-xs">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 dark:text-slate-500 text-xl">
                  search
                </span>
                <input
                  className="w-full pl-10 pr-4 py-2 text-sm border-slate-200 dark:border-slate-700 rounded-lg bg-background-light dark:bg-background-dark focus:ring-primary focus:border-primary"
                  placeholder="Tìm kiếm theo email, tên..."
                  type="text"
                  value={filters.search}
                  onChange={(e) => setFilters({ ...filters, search: e.target.value })}
                />
              </form>
            </div>

            {/* Desktop Table */}
            <div className="flex-1 overflow-x-auto">
              <div className="hidden lg:block">
                <table className="w-full text-sm text-left text-slate-500 dark:text-slate-400">
                  <thead className="text-xs text-slate-700 dark:text-slate-300 uppercase bg-slate-50 dark:bg-slate-800/50">
                    <tr>
                      <th className="p-4" scope="col">STT</th>
                      <th className="p-4 min-w-[200px]" scope="col">Tên đầy đủ</th>
                      <th className="p-4 min-w-[150px]" scope="col">Số điện thoại</th>
                      <th className="p-4" scope="col">Vai trò</th>
                      <th className="p-4 min-w-[150px]" scope="col">Ngày đăng ký</th>
                      <th className="p-4" scope="col">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((user, index) => {
                      const isCurrentUser = user.email === currentUserEmail || user.role === "ADMIN";
                      return (
                        <tr
                          key={user.userId}
                          className="bg-white dark:bg-slate-900/50 border-b dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/50"
                        >
                          <td className="p-4 font-medium text-slate-900 dark:text-white">
                            {pagination.page * pagination.size + index + 1}
                          </td>
                          <td className="p-4">
                            <div className="flex items-center gap-3">
                              <img
                                alt={user.fullName}
                                className="size-8 rounded-full object-cover"
                                src={getAvatarUrl(user)}
                              />
                              <div>
                                <div className="font-medium text-slate-900 dark:text-white">
                                  {user.fullName}
                                </div>
                                <div className="text-xs text-slate-500 dark:text-slate-400">
                                  {user.email}
                                </div>
                              </div>
                            </div>
                          </td>
                          <td className="p-4">{user.phoneNumber || "N/A"}</td>
                          <td className="p-4">
                            <span className={`px-2 py-1 text-xs font-medium rounded ${getRoleBadgeColor(user.role)}`}>
                              {user.role}
                            </span>
                          </td>
                          <td className="p-4">{formatDate(user.createdAt)}</td>
                          <td className="p-4">
                            <div className="flex items-center gap-1">
                              {user.isActive ? (
                                <button
                                  onClick={() => {
                                    setSelectedUser(user);
                                    setShowLockModal(true);
                                  }}
                                  disabled={isCurrentUser}
                                  className={`p-1.5 rounded-md ${
                                    isCurrentUser
                                      ? "text-slate-300 dark:text-slate-600 cursor-not-allowed"
                                      : "hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-400 hover:text-yellow-600 dark:hover:text-yellow-400"
                                  }`}
                                  title={isCurrentUser ? "Không thể khóa admin" : "Khóa tài khoản"}
                                >
                                  <span className="material-symbols-outlined text-lg">lock</span>
                                </button>
                              ) : (
                                <button
                                  onClick={() => {
                                    setSelectedUser(user);
                                    setShowUnlockModal(true);
                                  }}
                                  className="p-1.5 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-400 hover:text-green-600 dark:hover:text-green-400"
                                  title="Mở khóa tài khoản"
                                >
                                  <span className="material-symbols-outlined text-lg">lock_open</span>
                                </button>
                              )}
                              <button
                                onClick={() => {
                                  setSelectedUser(user);
                                  setShowDeleteModal(true);
                                }}
                                disabled={isCurrentUser}
                                className={`p-1.5 rounded-md ${
                                  isCurrentUser
                                    ? "text-slate-300 dark:text-slate-600 cursor-not-allowed"
                                    : "hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-400 hover:text-red-600 dark:hover:text-red-400"
                                }`}
                                title={isCurrentUser ? "Không thể xóa admin" : "Xóa tài khoản"}
                              >
                                <span className="material-symbols-outlined text-lg">delete</span>
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              {/* Mobile Cards */}
              <div className="lg:hidden p-4 space-y-4">
                {users.map((user) => {
                  const isCurrentUser = user.email === currentUserEmail || user.role === "ADMIN";
                  return (
                    <div
                      key={user.userId}
                      className="bg-slate-50 dark:bg-slate-800/50 rounded-lg p-4 border border-slate-200 dark:border-slate-700"
                    >
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex items-center gap-3">
                          <img
                            alt={user.fullName}
                            className="size-10 rounded-full object-cover"
                            src={getAvatarUrl(user)}
                          />
                          <div>
                            <p className="font-semibold text-slate-900 dark:text-white">{user.fullName}</p>
                            <p className="text-sm text-slate-500 dark:text-slate-400">{user.email}</p>
                          </div>
                        </div>
                      </div>
                      <div className="mt-4 grid grid-cols-2 gap-4 text-sm">
                        <div>
                          <p className="text-slate-500 dark:text-slate-400">Số điện thoại</p>
                          <p className="text-slate-900 dark:text-white font-medium mt-1">{user.phoneNumber || "N/A"}</p>
                        </div>
                        <div>
                          <p className="text-slate-500 dark:text-slate-400">Vai trò</p>
                          <span className={`inline-block px-2 py-1 text-xs font-medium rounded mt-1 ${getRoleBadgeColor(user.role)}`}>
                            {user.role}
                          </span>
                        </div>
                        <div>
                          <p className="text-slate-500 dark:text-slate-400">Ngày đăng ký</p>
                          <p className="font-medium text-slate-800 dark:text-slate-200">{formatDate(user.createdAt)}</p>
                        </div>
                      </div>
                      <div className="mt-4 pt-3 border-t border-slate-200 dark:border-slate-700 flex justify-end gap-2">
                        {user.isActive ? (
                          <button
                            onClick={() => {
                              setSelectedUser(user);
                              setShowLockModal(true);
                            }}
                            disabled={isCurrentUser}
                            className="p-2 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300 disabled:opacity-50"
                          >
                            <span className="material-symbols-outlined">lock</span>
                          </button>
                        ) : (
                          <button
                            onClick={() => {
                              setSelectedUser(user);
                              setShowUnlockModal(true);
                            }}
                            className="p-2 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300"
                          >
                            <span className="material-symbols-outlined">lock_open</span>
                          </button>
                        )}
                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setShowDeleteModal(true);
                          }}
                          disabled={isCurrentUser}
                          className="p-2 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300 disabled:opacity-50"
                        >
                          <span className="material-symbols-outlined">delete</span>
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Pagination */}
            <div className="p-4 border-t border-slate-200 dark:border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-4 text-sm text-slate-600 dark:text-slate-400">
              <span className="font-medium">
                Hiển thị {pagination.page * pagination.size + 1}-
                {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} của{" "}
                {pagination.totalElements} người dùng
              </span>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setPagination((prev) => ({ ...prev, page: prev.page - 1 }))}
                  disabled={pagination.page === 0}
                  className="flex items-center justify-center size-8 rounded-md border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <span className="material-symbols-outlined text-base">chevron_left</span>
                </button>
                <div className="flex items-center gap-1">
                  {[...Array(Math.min(pagination.totalPages, 5))].map((_, i) => {
                    const pageNum = i;
                    return (
                      <button
                        key={pageNum}
                        onClick={() => setPagination((prev) => ({ ...prev, page: pageNum }))}
                        className={`flex items-center justify-center size-8 rounded-md ${
                          pagination.page === pageNum
                            ? "bg-primary text-white font-semibold"
                            : "hover:bg-slate-100 dark:hover:bg-slate-800"
                        }`}
                      >
                        {pageNum + 1}
                      </button>
                    );
                  })}
                  {pagination.totalPages > 5 && <span>...</span>}
                </div>
                <button
                  onClick={() => setPagination((prev) => ({ ...prev, page: prev.page + 1 }))}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className="flex items-center justify-center size-8 rounded-md border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <span className="material-symbols-outlined text-base">chevron_right</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Lock Modal */}
      {showLockModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-slate-900 rounded-xl p-6 max-w-md w-full">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">
              Xác nhận khóa tài khoản
            </h3>
            <p className="text-slate-600 dark:text-slate-400 mb-6">
              Bạn có chắc muốn khóa tài khoản <strong>{selectedUser?.fullName}</strong>? Người dùng sẽ không thể đăng nhập.
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setShowLockModal(false)}
                className="px-4 py-2 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300"
              >
                Hủy
              </button>
              <button
                onClick={handleLockUser}
                className="px-4 py-2 rounded-lg bg-yellow-500 hover:bg-yellow-600 text-white"
              >
                Khóa
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Unlock Modal */}
      {showUnlockModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-slate-900 rounded-xl p-6 max-w-md w-full">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">
              Xác nhận mở khóa tài khoản
            </h3>
            <p className="text-slate-600 dark:text-slate-400 mb-6">
              Bạn có chắc muốn mở khóa tài khoản <strong>{selectedUser?.fullName}</strong>?
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setShowUnlockModal(false)}
                className="px-4 py-2 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300"
              >
                Hủy
              </button>
              <button
                onClick={handleUnlockUser}
                className="px-4 py-2 rounded-lg bg-green-500 hover:bg-green-600 text-white"
              >
                Mở khóa
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-slate-900 rounded-xl p-6 max-w-md w-full">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">
              Xác nhận xóa tài khoản
            </h3>
            <p className="text-slate-600 dark:text-slate-400 mb-6">
              ⚠️ Hành động này không thể hoàn tác! Bạn có chắc muốn xóa tài khoản{" "}
              <strong>{selectedUser?.fullName}</strong>?
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setShowDeleteModal(false)}
                className="px-4 py-2 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300"
              >
                Hủy
              </button>
              <button
                onClick={handleDeleteUser}
                className="px-4 py-2 rounded-lg bg-red-500 hover:bg-red-600 text-white"
              >
                Xóa
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
