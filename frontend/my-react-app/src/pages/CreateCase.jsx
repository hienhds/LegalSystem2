import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { caseService } from "../services/caseService";
import Layout from "../components/Layout";
import axiosInstance from "../utils/axiosInstance";
import useUserProfile from "../hooks/useUserProfile";

export default function CreateCase() {
  const navigate = useNavigate();
  const { user, loading: userLoading } = useUserProfile();

  const [formData, setFormData] = useState({
    title: "",
    description: "",
    clientId: "",
  });

  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [selectedClient, setSelectedClient] = useState(null);
  const [showDropdown, setShowDropdown] = useState(false);
  const [loading, setLoading] = useState(false);
  const [searching, setSearching] = useState(false);
  const searchTimeoutRef = useRef(null);

  useEffect(() => {
    if (!userLoading) {
      if (!user || user.role !== "LAWYER") {
        alert("Bạn không có quyền truy cập trang này.");
        navigate("/");
      }
    }
  }, [user, userLoading, navigate]);

  useEffect(() => {
    if (!searchTerm.trim()) {
      setSearchResults([]);
      return;
    }

    if (searchTimeoutRef.current) clearTimeout(searchTimeoutRef.current);

    searchTimeoutRef.current = setTimeout(async () => {
      setSearching(true);
      try {
        // 🔥 ĐỔI ĐƯỜNG DẪN TẠI ĐÂY: Dùng /api/users/search
        const res = await axiosInstance.get(`/api/users/search`, {
          params: { 
            keyword: searchTerm, // Backend mới dùng 'keyword'
            page: 0, 
            size: 5 
          }
        });
        
        if (res.data.success) {
          const clients = res.data.data.content.filter(u => u.role !== 'ADMIN' && u.role !== 'LAWYER');
          setSearchResults(clients);
          setShowDropdown(true);
        }
      } catch (error) {
        console.error("Lỗi tìm kiếm khách hàng:", error);
      } finally {
        setSearching(false);
      }
    }, 500);

    return () => clearTimeout(searchTimeoutRef.current);
  }, [searchTerm]);

  const handleSelectClient = (client) => {
    setFormData({ ...formData, clientId: client.userId });
    setSelectedClient(client);
    setShowDropdown(false);
    setSearchTerm("");
  };

  const handleRemoveClient = () => {
    setFormData({ ...formData, clientId: "" });
    setSelectedClient(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.clientId) {
      alert("Vui lòng chọn khách hàng!");
      return;
    }

    setLoading(true);
    try {
      const res = await caseService.createCase(formData);
      if (res.data.success) {
        alert(`Tạo hồ sơ thành công cho khách hàng: ${selectedClient.fullName}`);
        navigate(`/cases/${res.data.data.caseId}`);
      }
    } catch (error) {
      console.error(error);
      alert("Lỗi: " + (error.response?.data?.message || "Không thể tạo vụ án"));
    } finally {
      setLoading(false);
    }
  };

  const getAvatar = (client) => {
    if (client.avatarUrl?.startsWith("http")) return client.avatarUrl;
    if (client.avatarUrl) return `http://localhost:8080${client.avatarUrl}`;
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(client.fullName)}&background=random`;
  };

  if (userLoading) return <Layout><div>Đang tải...</div></Layout>;

  return (
    <Layout>
      <div className="max-w-3xl mx-auto p-6 bg-white dark:bg-slate-900 rounded-xl shadow-md mt-10 border border-slate-200 dark:border-slate-800">
        <h1 className="text-2xl font-bold mb-6 text-slate-900 dark:text-white border-b pb-4 dark:border-slate-800">
          Tạo Hồ Sơ Vụ Án Mới
        </h1>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-semibold mb-2 dark:text-slate-300">
              Tiêu đề vụ việc <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              className="w-full p-3 border border-slate-300 dark:border-slate-700 rounded-lg dark:bg-slate-800 focus:ring-2 focus:ring-blue-500 outline-none"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              placeholder="Vd: Tranh chấp đất đai tại xã X..."
            />
          </div>
          
          <div className="relative">
            <label className="block text-sm font-semibold mb-2 dark:text-slate-300">
              Khách hàng (Người dân) <span className="text-red-500">*</span>
            </label>

            {selectedClient ? (
              <div className="flex items-center justify-between p-3 border border-green-500 bg-green-50 dark:bg-green-900/20 rounded-lg">
                <div className="flex items-center gap-3">
                  <img 
                    src={getAvatar(selectedClient)} 
                    alt="Avatar" 
                    className="w-12 h-12 rounded-full object-cover border border-slate-300"
                  />
                  <div>
                    <p className="font-bold text-slate-800 dark:text-slate-200 text-base">
                      {selectedClient.fullName} 
                      <span className="text-sm font-normal text-green-600 ml-2">(ID: #{selectedClient.userId})</span>
                    </p>
                    <p className="text-xs text-slate-500">
                      {selectedClient.email} • {selectedClient.phoneNumber}
                    </p>
                  </div>
                </div>
                <button 
                  type="button"
                  onClick={handleRemoveClient}
                  className="text-red-500 hover:text-red-700 text-sm font-medium px-3 py-1 hover:bg-red-50 rounded"
                >
                  ✕ Bỏ chọn
                </button>
              </div>
            ) : (
              <div className="relative">
                <div className="flex items-center border border-slate-300 dark:border-slate-700 rounded-lg dark:bg-slate-800 overflow-hidden focus-within:ring-2 focus-within:ring-blue-500">
                  <span className="material-symbols-outlined px-3 text-slate-400">person_search</span>
                  <input
                    type="text"
                    className="w-full p-3 pl-0 outline-none bg-transparent"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onFocus={() => { if(searchTerm) setShowDropdown(true); }}
                    placeholder="Nhập tên, email hoặc SĐT khách hàng..."
                  />
                  {searching && (
                    <div className="px-3">
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                    </div>
                  )}
                </div>

                {showDropdown && searchResults.length > 0 && (
                  <ul className="absolute z-10 w-full mt-1 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg shadow-lg max-h-60 overflow-y-auto">
                    {searchResults.map((client) => (
                      <li 
                        key={client.userId}
                        onClick={() => handleSelectClient(client)}
                        className="flex items-center gap-3 p-3 hover:bg-slate-100 dark:hover:bg-slate-700 cursor-pointer border-b last:border-0 dark:border-slate-700 transition-colors"
                      >
                        <img 
                          src={getAvatar(client)} 
                          alt={client.fullName} 
                          className="w-10 h-10 rounded-full object-cover"
                        />
                        <div>
                          <p className="text-sm font-medium text-slate-800 dark:text-slate-200">
                            {client.fullName}
                          </p>
                          <p className="text-xs text-slate-500 truncate max-w-[200px]">{client.email}</p>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
                
                {showDropdown && !searching && searchTerm && searchResults.length === 0 && (
                   <div className="absolute z-10 w-full mt-1 bg-white dark:bg-slate-800 p-3 text-center text-slate-500 text-sm border rounded-lg shadow-lg">
                     Không tìm thấy khách hàng phù hợp.
                   </div>
                )}
              </div>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-2 dark:text-slate-300">
              Mô tả chi tiết <span className="text-red-500">*</span>
            </label>
            <textarea
              required
              rows="6"
              className="w-full p-3 border border-slate-300 dark:border-slate-700 rounded-lg dark:bg-slate-800 focus:ring-2 focus:ring-blue-500 outline-none"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Mô tả nội dung vụ việc..."
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex justify-center items-center gap-2"
          >
            {loading ? "Đang xử lý..." : "Tạo Hồ Sơ Vụ Án"}
          </button>
        </form>
      </div>
    </Layout>
  );
}