import React, { useState, useEffect } from "react";
import axiosInstance from "../utils/axiosInstance";
import AdminSidebar from "../components/AdminSidebar";

export default function DocumentManagement() {
  const [selectedIds, setSelectedIds] = useState([]);
  const [filters, setFilters] = useState({
    search: "",
    category: "",
    status: "",
    sortBy: "newest"
  });
  const [documents, setDocuments] = useState([]);
  const [stats, setStats] = useState({ total: 0, active: 0, inactive: 0 });
  const [categories, setCategories] = useState([]);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });
  const [loading, setLoading] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [showXmlModal, setShowXmlModal] = useState(false);
  const [xmlContent, setXmlContent] = useState("");
  const [loadingXml, setLoadingXml] = useState(false);
  const [selectedDocument, setSelectedDocument] = useState(null);
  const [formData, setFormData] = useState({
    title: "",
    category: "",
    status: "ACTIVE",
    file: null
  });

  // Fetch stats
  useEffect(() => {
    fetchStats();
    fetchCategories();
  }, []);

  // Fetch documents when filters or pagination change
  useEffect(() => {
    fetchDocuments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters, pagination.page]);

  const fetchStats = async () => {
    try {
      const response = await axiosInstance.get("/api/admin/documents/stats");
      setStats(response.data);
    } catch (error) {
      console.error("Error fetching stats:", error);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await axiosInstance.get("/api/admin/documents/categories");
      setCategories(response.data.data || []);
    } catch (error) {
      console.error("Error fetching categories:", error);
    }
  };

  const fetchDocuments = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.page,
        size: pagination.size,
        sort: getSortParam()
      };

      if (filters.search) params.search = filters.search;
      if (filters.category) params.category = filters.category;
      if (filters.status) params.status = filters.status;

      const response = await axiosInstance.get("/api/admin/documents", { params });
      setDocuments(response.data.content || []);
      setPagination(prev => ({
        ...prev,
        totalElements: response.data.totalElements || 0,
        totalPages: response.data.totalPages || 0
      }));
    } catch (error) {
      console.error("Error fetching documents:", error);
    } finally {
      setLoading(false);
    }
  };

  const getSortParam = () => {
    switch (filters.sortBy) {
      case "newest":
        return "createdAt,desc";
      case "oldest":
        return "createdAt,asc";
      case "views":
        return "viewCount,desc";
      default:
        return "createdAt,desc";
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN");
  };

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedIds(documents.map(doc => doc.documentId));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (id) => {
    setSelectedIds(prev => 
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  const handleRefresh = () => {
    fetchStats();
    fetchDocuments();
  };

  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  const parseVietnamLegalXml = (xmlString) => {
    try {
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(xmlString, "text/xml");
      
      // Check if parsing error
      if (xmlDoc.querySelector("parsererror")) {
        return { isLegalXml: false, rawXml: xmlString };
      }

      // Tìm các thẻ văn bản pháp luật Việt Nam
      const vanBan = xmlDoc.querySelector("VanBan, VANBAN, vanban");
      
      if (!vanBan) {
        return { isLegalXml: false, rawXml: xmlString };
      }

      // Parse các thành phần
      const result = {
        isLegalXml: true,
        ten: vanBan.querySelector("Ten, TEN, ten")?.textContent || "Không có tiêu đề",
        loai: vanBan.querySelector("Loai, LOAI, loai")?.textContent || "",
        so: vanBan.querySelector("So, SO, so")?.textContent || "",
        ngayBanHanh: vanBan.querySelector("NgayBanHanh, NGAYBANHANH, ngaybanhanh")?.textContent || "",
        ngayHieuLuc: vanBan.querySelector("NgayHieuLuc, NGAYHIEULUC, ngayhieuluc")?.textContent || "",
        coQuanBanHanh: vanBan.querySelector("CoQuanBanHanh, COQUANBANHANH, coquanbanhanh")?.textContent || "",
        nguoiKy: vanBan.querySelector("NguoiKy, NGUOIKY, nguoiky")?.textContent || "",
        chuong: [],
        dieu: []
      };

      // Parse các chương
      const chuongNodes = vanBan.querySelectorAll("Chuong, CHUONG, chuong");
      chuongNodes.forEach(chuong => {
        result.chuong.push({
          so: chuong.querySelector("So, SO, so")?.textContent || "",
          ten: chuong.querySelector("Ten, TEN, ten")?.textContent || "",
          dieu: []
        });
      });

      // Parse các điều
      const dieuNodes = vanBan.querySelectorAll("Dieu, DIEU, dieu");
      dieuNodes.forEach(dieu => {
        const dieuObj = {
          so: dieu.querySelector("So, SO, so")?.textContent || dieu.getAttribute("so") || "",
          tieu_de: dieu.querySelector("TieuDe, TIEUDE, tieude")?.textContent || "",
          khoan: [],
          noi_dung: ""
        };

        // Lấy nội dung trực tiếp của điều (không phải trong khoản)
        const noiDungNode = dieu.querySelector("NoiDung, NOIDUNG, noidung");
        if (noiDungNode) {
          dieuObj.noi_dung = noiDungNode.textContent;
        }

        // Parse các khoản
        const khoanNodes = dieu.querySelectorAll("Khoan, KHOAN, khoan");
        khoanNodes.forEach(khoan => {
          dieuObj.khoan.push({
            so: khoan.querySelector("So, SO, so")?.textContent || khoan.getAttribute("so") || "",
            noi_dung: khoan.querySelector("NoiDung, NOIDUNG, noidung")?.textContent || khoan.textContent
          });
        });

        result.dieu.push(dieuObj);
      });

      return result;
    } catch {
      return { isLegalXml: false, rawXml: xmlString };
    }
  };

  const handleViewXml = async (documentId) => {
    setLoadingXml(true);
    setShowXmlModal(true);
    try {
      const response = await axiosInstance.get(`/api/admin/documents/${documentId}/content`);
      const xmlText = response.data.data || response.data;
      const parsed = parseVietnamLegalXml(xmlText);
      setXmlContent(parsed);
    } catch (error) {
      console.error("Error loading XML content:", error);
      setXmlContent({ isLegalXml: false, rawXml: "Không thể tải nội dung file XML" });
    } finally {
      setLoadingXml(false);
    }
  };

  const handleDelete = async (documentId) => {
    try {
      await axiosInstance.delete(`/api/admin/documents/${documentId}`);
      setShowDeleteModal(false);
      fetchDocuments();
      fetchStats();
      alert("Đã xóa văn bản thành công!");
    } catch (error) {
      console.error("Error deleting document:", error);
      alert("Không thể xóa văn bản. Vui lòng thử lại.");
    }
  };

  const handleBulkDelete = async () => {
    if (selectedIds.length === 0) {
      alert("Vui lòng chọn ít nhất một văn bản để xóa");
      return;
    }
    
    if (!window.confirm(`Bạn có chắc muốn xóa ${selectedIds.length} văn bản đã chọn?`)) {
      return;
    }

    try {
      await axiosInstance.delete("/api/admin/documents/bulk", {
        data: { ids: selectedIds }
      });
      setSelectedIds([]);
      fetchDocuments();
      fetchStats();
      alert("Đã xóa các văn bản thành công!");
    } catch (error) {
      console.error("Error bulk deleting:", error);
      alert("Không thể xóa văn bản. Vui lòng thử lại.");
    }
  };

  const handleSubmitAdd = async (e) => {
    e.preventDefault();
    
    if (!formData.title || !formData.category || !formData.file) {
      alert("Vui lòng điền đầy đủ thông tin");
      return;
    }

    try {
      const token = localStorage.getItem("accessToken");
      const formDataToSend = new FormData();
      formDataToSend.append("title", formData.title);
      formDataToSend.append("category", formData.category);
      formDataToSend.append("status", formData.status);
      formDataToSend.append("file", formData.file);

      await axiosInstance.post("/api/admin/documents", formDataToSend, {
        headers: { 
          Authorization: `Bearer ${token}`,
          "Content-Type": "multipart/form-data"
        }
      });

      setShowAddModal(false);
      setFormData({ title: "", category: "", status: "ACTIVE", file: null });
      fetchDocuments();
      fetchStats();
      fetchCategories();
      alert("Đã thêm văn bản thành công!");
    } catch (error) {
      console.error("Error adding document:", error);
      alert(error.response?.data?.message || "Không thể thêm văn bản. Vui lòng thử lại.");
    }
  };

  const handleSubmitEdit = async (e) => {
    e.preventDefault();
    
    if (!formData.title || !formData.category) {
      alert("Vui lòng điền đầy đủ thông tin");
      return;
    }

    try {
      const token = localStorage.getItem("accessToken");
      const formDataToSend = new FormData();
      formDataToSend.append("title", formData.title);
      formDataToSend.append("category", formData.category);
      formDataToSend.append("status", formData.status);
      if (formData.file) {
        formDataToSend.append("file", formData.file);
      }

      await axiosInstance.put(`/api/admin/documents/${selectedDocument.documentId}`, formDataToSend, {
        headers: { 
          Authorization: `Bearer ${token}`,
          "Content-Type": "multipart/form-data"
        }
      });

      setShowEditModal(false);
      setFormData({ title: "", category: "", status: "ACTIVE", file: null });
      setSelectedDocument(null);
      fetchDocuments();
      fetchStats();
      fetchCategories();
      alert("Đã cập nhật văn bản thành công!");
    } catch (error) {
      console.error("Error updating document:", error);
      alert(error.response?.data?.message || "Không thể cập nhật văn bản. Vui lòng thử lại.");
    }
  };

  return (
    <div className="flex min-h-screen bg-background-light dark:bg-background-dark">
      <AdminSidebar />
      
      <main className="flex-1 flex flex-col overflow-y-auto">
        {/* Header */}
        <div className="p-6 lg:p-8 flex-shrink-0">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-slate-900 dark:text-white text-3xl font-bold tracking-tight">
              Quản Lý Văn Bản Pháp Luật
            </h1>
            <div className="flex items-center gap-2">
              <button onClick={handleRefresh} className="flex h-9 shrink-0 items-center justify-center gap-x-2 rounded-lg bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-200 px-3 hover:bg-slate-50 dark:hover:bg-slate-700/50">
                <span className="material-symbols-outlined text-base">refresh</span>
                <p className="text-sm font-medium">Làm mới</p>
              </button>
              <button onClick={() => setShowAddModal(true)} className="flex h-9 shrink-0 items-center justify-center gap-x-2 rounded-lg bg-primary text-white pl-3 pr-4 hover:bg-primary/90">
                <span className="material-symbols-outlined text-base">add</span>
                <p className="text-sm font-medium">Thêm văn bản mới</p>
              </button>
              {selectedIds.length > 0 && (
                <button onClick={handleBulkDelete} className="flex h-9 shrink-0 items-center justify-center gap-x-2 rounded-lg bg-red-600 text-white pl-3 pr-4 hover:bg-red-700">
                  <span className="material-symbols-outlined text-base">delete</span>
                  <p className="text-sm font-medium">Xóa ({selectedIds.length})</p>
                </button>
              )}
            </div>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
            <div className="bg-white dark:bg-slate-900/50 rounded-xl p-4 border border-slate-200 dark:border-slate-800 flex items-center gap-4">
              <div className="p-3 rounded-full bg-primary/10 text-primary dark:bg-primary/20 dark:text-sky-300">
                <span className="material-symbols-outlined">description</span>
              </div>
              <div>
                <p className="text-sm text-slate-500 dark:text-slate-400">Tổng số văn bản</p>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.total.toLocaleString()}</p>
              </div>
            </div>
            <div className="bg-white dark:bg-slate-900/50 rounded-xl p-4 border border-slate-200 dark:border-slate-800 flex items-center gap-4">
              <div className="p-3 rounded-full bg-green-500/10 text-green-600 dark:bg-green-500/20 dark:text-green-400">
                <span className="material-symbols-outlined">task_alt</span>
              </div>
              <div>
                <p className="text-sm text-slate-500 dark:text-slate-400">Đang hoạt động</p>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.active.toLocaleString()}</p>
              </div>
            </div>
            <div className="bg-white dark:bg-slate-900/50 rounded-xl p-4 border border-slate-200 dark:border-slate-800 flex items-center gap-4">
              <div className="p-3 rounded-full bg-red-500/10 text-red-600 dark:bg-red-500/20 dark:text-red-400">
                <span className="material-symbols-outlined">cancel</span>
              </div>
              <div>
                <p className="text-sm text-slate-500 dark:text-slate-400">Ngừng hoạt động</p>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">{stats.inactive.toLocaleString()}</p>
              </div>
            </div>
            <div className="bg-white dark:bg-slate-900/50 rounded-xl p-4 border border-slate-200 dark:border-slate-800 flex items-center gap-4">
              <div className="p-3 rounded-full bg-yellow-500/10 text-yellow-600 dark:bg-yellow-500/20 dark:text-yellow-400">
                <span className="material-symbols-outlined">trending_up</span>
              </div>
              <div>
                <p className="text-sm text-slate-500 dark:text-slate-400">Xem nhiều nhất</p>
                <p className="text-2xl font-bold text-slate-900 dark:text-white">Top 5</p>
              </div>
            </div>
          </div>

          {/* Filters */}
          <div className="bg-white dark:bg-slate-900/50 rounded-xl p-4 border border-slate-200 dark:border-slate-800">
            <div className="grid grid-cols-1 lg:grid-cols-5 gap-4">
              <div className="relative lg:col-span-2">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 dark:text-slate-500">
                  search
                </span>
                <input
                  className="w-full h-10 bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700 rounded-lg pl-10 pr-4 text-sm focus:ring-primary focus:border-primary"
                  placeholder="Tìm kiếm theo tiêu đề văn bản..."
                  type="text"
                  value={filters.search}
                  onChange={(e) => setFilters(prev => ({ ...prev, search: e.target.value }))}
                />
              </div>
              <div className="relative lg:col-span-1">
                <select
                  className="w-full h-10 bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700 rounded-lg px-3 text-sm focus:ring-primary focus:border-primary"
                  value={filters.category}
                  onChange={(e) => setFilters(prev => ({ ...prev, category: e.target.value }))}
                >
                  <option value="">Tất cả danh mục</option>
                  {categories.map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
              </div>
              <div className="relative lg:col-span-1">
                <select
                  className="w-full h-10 bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700 rounded-lg px-3 text-sm focus:ring-primary focus:border-primary"
                  value={filters.status}
                  onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))}
                >
                  <option value="">Tất cả trạng thái</option>
                  <option value="ACTIVE">Đang hoạt động</option>
                  <option value="INACTIVE">Ngừng hoạt động</option>
                </select>
              </div>
              <div className="relative lg:col-span-1">
                <select
                  className="w-full h-10 bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700 rounded-lg px-3 text-sm focus:ring-primary focus:border-primary"
                  value={filters.sortBy}
                  onChange={(e) => setFilters(prev => ({ ...prev, sortBy: e.target.value }))}
                >
                  <option value="newest">Sắp xếp theo: Mới nhất</option>
                  <option value="oldest">Sắp xếp theo: Cũ nhất</option>
                  <option value="views">Sắp xếp theo: Lượt xem</option>
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
                        checked={documents.length > 0 && selectedIds.length === documents.length}
                        onChange={handleSelectAll}
                      />
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      STT
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      Tiêu đề văn bản
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      Danh mục
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      Trạng thái
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      Lượt xem
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      Ngày tạo
                    </th>
                    <th className="px-4 py-3 text-right text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider" scope="col">
                      Thao tác
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
                  {loading ? (
                    <tr>
                      <td colSpan="8" className="px-4 py-8 text-center text-slate-500 dark:text-slate-400">
                        Đang tải dữ liệu...
                      </td>
                    </tr>
                  ) : documents.length === 0 ? (
                    <tr>
                      <td colSpan="8" className="px-4 py-8 text-center text-slate-500 dark:text-slate-400">
                        Không có văn bản nào
                      </td>
                    </tr>
                  ) : documents.map((doc, index) => (
                    <tr key={doc.documentId}>
                      <td className="p-4">
                        <input
                          className="h-4 w-4 rounded border-slate-300 dark:border-slate-600 text-primary focus:ring-primary"
                          type="checkbox"
                          checked={selectedIds.includes(doc.documentId)}
                          onChange={() => handleSelectOne(doc.documentId)}
                        />
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-500 dark:text-slate-400">
                        {pagination.page * pagination.size + index + 1}
                      </td>
                      <td className="px-4 py-3 text-sm font-medium text-slate-900 dark:text-white max-w-xs truncate">
                        {doc.title}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-600 dark:text-slate-300">
                        {doc.category}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          doc.status === "ACTIVE"
                            ? "bg-green-100 text-green-800 dark:bg-green-900/50 dark:text-green-300"
                            : "bg-red-100 text-red-800 dark:bg-red-900/50 dark:text-red-300"
                        }`}>
                          {doc.status === "ACTIVE" ? "Đang hoạt động" : "Ngừng hoạt động"}
                        </span>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-600 dark:text-slate-300">
                        {doc.viewCount.toLocaleString()}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-600 dark:text-slate-300">
                        {formatDate(doc.createdAt)}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-right">
                        <div className="flex items-center justify-end gap-1">
                          <button 
                            onClick={() => {
                              setSelectedDocument(doc);
                              setShowViewModal(true);
                            }}
                            className="p-2 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400" 
                            title="Xem chi tiết"
                          >
                            <span className="material-symbols-outlined text-lg">visibility</span>
                          </button>
                          <button 
                            onClick={() => {
                              setSelectedDocument(doc);
                              setFormData({
                                title: doc.title,
                                category: doc.category,
                                status: doc.status,
                                file: null
                              });
                              setShowEditModal(true);
                            }}
                            className="p-2 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400" 
                            title="Chỉnh sửa"
                          >
                            <span className="material-symbols-outlined text-lg">edit</span>
                          </button>
                          {doc.fileUrl && (
                            <button
                              onClick={() => handleViewXml(doc.documentId)}
                              className="p-2 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400" 
                              title="Xem nội dung XML"
                            >
                              <span className="material-symbols-outlined text-lg">description</span>
                            </button>
                          )}
                          <button 
                            onClick={() => {
                              setSelectedDocument(doc);
                              setShowDeleteModal(true);
                            }}
                            className="p-2 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 text-red-600 dark:text-red-500" 
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
                {pagination.totalElements > 0 ? pagination.page * pagination.size + 1 : 0}-{Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)}
              </span>{" "}
              trên{" "}
              <span className="font-semibold text-slate-900 dark:text-white">{pagination.totalElements.toLocaleString()}</span>{" "}
              văn bản
            </p>
            <div className="flex items-center gap-2">
              <button
                disabled={pagination.page === 0}
                onClick={() => handlePageChange(pagination.page - 1)}
                className="inline-flex items-center justify-center h-9 w-9 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800/50 text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span className="material-symbols-outlined text-lg">chevron_left</span>
              </button>
              <span className="text-sm text-slate-600 dark:text-slate-400">Trang</span>
              <input
                className="h-9 w-16 text-center rounded-lg border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800/50 text-sm"
                type="number"
                value={pagination.page + 1}
                onChange={(e) => {
                  const page = parseInt(e.target.value) - 1;
                  if (page >= 0 && page < pagination.totalPages) {
                    handlePageChange(page);
                  }
                }}
                min="1"
                max={pagination.totalPages}
              />
              <span className="text-sm text-slate-600 dark:text-slate-400">trên {pagination.totalPages}</span>
              <button 
                disabled={pagination.page >= pagination.totalPages - 1}
                onClick={() => handlePageChange(pagination.page + 1)}
                className="inline-flex items-center justify-center h-9 w-9 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800/50 text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span className="material-symbols-outlined text-lg">chevron_right</span>
              </button>
            </div>
          </div>
        </div>
      </main>

      {/* Add Document Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-md w-full p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Thêm văn bản mới</h3>
            <form onSubmit={handleSubmitAdd} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">Tiêu đề</label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData({...formData, title: e.target.value})}
                  className="w-full h-10 rounded-lg border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:ring-primary focus:border-primary"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">Danh mục</label>
                <input
                  type="text"
                  value={formData.category}
                  onChange={(e) => setFormData({...formData, category: e.target.value})}
                  className="w-full h-10 rounded-lg border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:ring-primary focus:border-primary"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">File XML</label>
                <input
                  type="file"
                  accept=".xml"
                  onChange={(e) => setFormData({...formData, file: e.target.files[0]})}
                  className="w-full rounded-lg border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:ring-primary focus:border-primary text-sm"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">Trạng thái</label>
                <select
                  value={formData.status}
                  onChange={(e) => setFormData({...formData, status: e.target.value})}
                  className="w-full h-10 rounded-lg border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:ring-primary focus:border-primary"
                >
                  <option value="ACTIVE">Đang hoạt động</option>
                  <option value="INACTIVE">Ngừng hoạt động</option>
                </select>
              </div>
              <div className="flex justify-end gap-3 mt-6">
                <button
                  type="button"
                  onClick={() => {
                    setShowAddModal(false);
                    setFormData({ title: "", category: "", status: "ACTIVE", file: null });
                  }}
                  className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-primary text-white hover:bg-primary/90"
                >
                  Thêm
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Document Modal */}
      {showEditModal && selectedDocument && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-md w-full p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Chỉnh sửa văn bản</h3>
            <form onSubmit={handleSubmitEdit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">Tiêu đề</label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData({...formData, title: e.target.value})}
                  className="w-full h-10 rounded-lg border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:ring-primary focus:border-primary"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">Danh mục</label>
                <input
                  type="text"
                  value={formData.category}
                  onChange={(e) => setFormData({...formData, category: e.target.value})}
                  className="w-full h-10 rounded-lg border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:ring-primary focus:border-primary"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">File XML mới (tùy chọn)</label>
                <input
                  type="file"
                  accept=".xml"
                  onChange={(e) => setFormData({...formData, file: e.target.files[0]})}
                  className="w-full rounded-lg border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:ring-primary focus:border-primary text-sm"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">Trạng thái</label>
                <select
                  value={formData.status}
                  onChange={(e) => setFormData({...formData, status: e.target.value})}
                  className="w-full h-10 rounded-lg border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:ring-primary focus:border-primary"
                >
                  <option value="ACTIVE">Đang hoạt động</option>
                  <option value="INACTIVE">Ngừng hoạt động</option>
                </select>
              </div>
              <div className="flex justify-end gap-3 mt-6">
                <button
                  type="button"
                  onClick={() => {
                    setShowEditModal(false);
                    setFormData({ title: "", category: "", status: "ACTIVE", file: null });
                    setSelectedDocument(null);
                  }}
                  className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-primary text-white hover:bg-primary/90"
                >
                  Cập nhật
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && selectedDocument && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-md w-full p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">🗑️ Xóa văn bản?</h3>
            <p className="text-slate-600 dark:text-slate-400 mb-6">
              <strong className="text-red-600">Hành động này không thể hoàn tác!</strong><br/>
              Bạn có chắc muốn xóa văn bản <strong>"{selectedDocument.title}"</strong>?
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => {
                  setShowDeleteModal(false);
                  setSelectedDocument(null);
                }}
                className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
              >
                Hủy
              </button>
              <button
                onClick={() => handleDelete(selectedDocument.documentId)}
                className="px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700"
              >
                Xóa
              </button>
            </div>
          </div>
        </div>
      )}

      {/* XML Content Modal */}
      {showXmlModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-6xl w-full max-h-[90vh] flex flex-col">
            <div className="p-6 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center flex-shrink-0">
              <h2 className="text-xl font-bold text-slate-900 dark:text-white">Nội dung văn bản XML</h2>
              <button
                onClick={() => {
                  setShowXmlModal(false);
                  setXmlContent("");
                }}
                className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-300"
              >
                <span className="material-symbols-outlined text-2xl">close</span>
              </button>
            </div>
            <div className="flex-1 overflow-y-auto p-6">
              {loadingXml ? (
                <div className="flex items-center justify-center py-12">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                </div>
              ) : xmlContent?.isLegalXml ? (
                <div className="bg-white dark:bg-slate-800 rounded-lg p-8 max-w-4xl mx-auto">
                  {/* Tiêu đề văn bản */}
                  <div className="text-center mb-8 border-b-2 border-slate-200 dark:border-slate-700 pb-6">
                    <h1 className="text-2xl font-bold text-slate-900 dark:text-white mb-4">
                      {xmlContent.ten}
                    </h1>
                    {xmlContent.loai && (
                      <p className="text-lg text-slate-600 dark:text-slate-400 mb-2">
                        {xmlContent.loai} {xmlContent.so && `số ${xmlContent.so}`}
                      </p>
                    )}
                    {xmlContent.coQuanBanHanh && (
                      <p className="text-sm text-slate-500 dark:text-slate-400 italic">
                        {xmlContent.coQuanBanHanh}
                      </p>
                    )}
                    <div className="flex justify-center gap-6 mt-4 text-sm text-slate-600 dark:text-slate-400">
                      {xmlContent.ngayBanHanh && (
                        <p>Ngày ban hành: <span className="font-semibold">{xmlContent.ngayBanHanh}</span></p>
                      )}
                      {xmlContent.ngayHieuLuc && (
                        <p>Ngày hiệu lực: <span className="font-semibold">{xmlContent.ngayHieuLuc}</span></p>
                      )}
                    </div>
                  </div>

                  {/* Các chương */}
                  {xmlContent.chuong?.length > 0 && (
                    <div className="mb-8">
                      {xmlContent.chuong.map((chuong, idx) => (
                        <div key={idx} className="mb-6">
                          <h2 className="text-xl font-bold text-center text-slate-900 dark:text-white mb-4">
                            Chương {chuong.so}: {chuong.ten}
                          </h2>
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Các điều */}
                  <div className="space-y-6">
                    {xmlContent.dieu?.map((dieu, idx) => (
                      <div key={idx} className="border-l-4 border-primary pl-6 py-2">
                        <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-3">
                          Điều {dieu.so}{dieu.tieu_de && `. ${dieu.tieu_de}`}
                        </h3>
                        
                        {dieu.noi_dung && (
                          <p className="text-slate-700 dark:text-slate-300 mb-3 leading-relaxed">
                            {dieu.noi_dung}
                          </p>
                        )}

                        {dieu.khoan?.length > 0 && (
                          <div className="space-y-3 ml-4">
                            {dieu.khoan.map((khoan, kIdx) => (
                              <div key={kIdx} className="flex gap-3">
                                <span className="font-semibold text-slate-600 dark:text-slate-400 min-w-[2rem]">
                                  {khoan.so}.
                                </span>
                                <p className="text-slate-700 dark:text-slate-300 leading-relaxed flex-1">
                                  {khoan.noi_dung}
                                </p>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>

                  {/* Người ký */}
                  {xmlContent.nguoiKy && (
                    <div className="mt-12 text-right border-t-2 border-slate-200 dark:border-slate-700 pt-6">
                      <p className="text-sm text-slate-500 dark:text-slate-400 mb-2">Nơi nhận:</p>
                      <p className="text-sm text-slate-600 dark:text-slate-300 mb-4">
                        - Như trên;<br/>
                        - Lưu: VT, PC.
                      </p>
                      <p className="font-bold text-slate-900 dark:text-white text-lg">
                        {xmlContent.nguoiKy}
                      </p>
                    </div>
                  )}
                </div>
              ) : (
                <div className="bg-slate-900 rounded-lg p-6 overflow-x-auto shadow-inner">
                  <pre className="text-xs text-green-400 font-mono leading-relaxed whitespace-pre">
                    {xmlContent?.rawXml || "Không có nội dung"}
                  </pre>
                </div>
              )}
            </div>
            <div className="p-6 border-t border-slate-200 dark:border-slate-800 flex justify-end flex-shrink-0">
              <button
                onClick={() => {
                  setShowXmlModal(false);
                  setXmlContent("");
                }}
                className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {/* View Document Modal */}
      {showViewModal && selectedDocument && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-slate-200 dark:border-slate-800">
              <h2 className="text-xl font-bold text-slate-900 dark:text-white">Chi tiết văn bản</h2>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <p className="text-sm text-slate-500 dark:text-slate-400">Tiêu đề</p>
                <p className="text-lg font-semibold text-slate-900 dark:text-white">{selectedDocument.title}</p>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">Danh mục</p>
                  <p className="text-slate-900 dark:text-white font-medium">{selectedDocument.category}</p>
                </div>
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">Trạng thái</p>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                    selectedDocument.status === "ACTIVE"
                      ? "bg-green-100 text-green-800 dark:bg-green-900/50 dark:text-green-300"
                      : "bg-red-100 text-red-800 dark:bg-red-900/50 dark:text-red-300"
                  }`}>
                    {selectedDocument.status === "ACTIVE" ? "Đang hoạt động" : "Ngừng hoạt động"}
                  </span>
                </div>
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">Lượt xem</p>
                  <p className="text-slate-900 dark:text-white font-medium">{selectedDocument.viewCount?.toLocaleString() || 0}</p>
                </div>
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">Ngày tạo</p>
                  <p className="text-slate-900 dark:text-white font-medium">{formatDate(selectedDocument.createdAt)}</p>
                </div>
              </div>
              {selectedDocument.fileUrl && (
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-400 mb-2">File đính kèm</p>
                  <a 
                    href={selectedDocument.fileUrl.startsWith('http') ? selectedDocument.fileUrl : `http://localhost:8080${selectedDocument.fileUrl}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                  >
                    <span className="material-symbols-outlined text-lg">description</span>
                    Xem file XML
                  </a>
                </div>
              )}
            </div>
            <div className="p-6 border-t border-slate-200 dark:border-slate-800 flex justify-end">
              <button
                onClick={() => {
                  setShowViewModal(false);
                  setSelectedDocument(null);
                }}
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
