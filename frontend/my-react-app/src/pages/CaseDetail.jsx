import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { caseService } from "../services/caseService";
import Layout from "../components/Layout";
import useUserProfile from "../hooks/useUserProfile";

export default function CaseDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useUserProfile();
  const [caseData, setCaseData] = useState(null);
  const [loading, setLoading] = useState(true);
  
  const [updateForm, setUpdateForm] = useState({ title: "", description: "", status: "" });
  const [showUpdateModal, setShowUpdateModal] = useState(false);

  const fetchCaseDetail = async () => {
    try {
      const res = await caseService.getCaseDetail(id);
      if (res.data.success) {
        setCaseData(res.data.data);
      }
    } catch (error) {
      console.error("Failed to load case:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCaseDetail();
  }, [id]);

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    try {
      await caseService.uploadDocument(id, file);
      alert("Upload tài liệu thành công!");
      fetchCaseDetail();
    } catch (error) {
      alert("Lỗi upload: " + (error.response?.data?.message || error.message));
    }
  };

  const handleDeleteDocument = async (docId, fileName) => {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa tài liệu "${fileName}" không?`)) {
      return;
    }

    try {
      await caseService.deleteDocument(id, docId);
      alert("Xóa tài liệu thành công!");
      fetchCaseDetail();
    } catch (error) {
      alert("Lỗi khi xóa: " + (error.response?.data?.message || error.message));
    }
  };

  // HÀM XỬ LÝ XÓA VỤ ÁN (MỚI)
  const handleDeleteCase = async () => {
    if (!window.confirm("CẢNH BÁO: Hành động này không thể hoàn tác.\nBạn có chắc chắn muốn xóa toàn bộ hồ sơ vụ án này không?")) {
      return;
    }

    try {
      await caseService.deleteCase(id);
      alert("Đã xóa vụ án thành công!");
      navigate("/cases"); // Chuyển hướng về danh sách sau khi xóa
    } catch (error) {
      alert("Lỗi khi xóa vụ án: " + (error.response?.data?.message || error.message));
    }
  };

  const handleUpdateProgress = async (e) => {
    e.preventDefault();
    try {
      const payload = { ...updateForm };
      if (!payload.status) delete payload.status;
      await caseService.updateProgress(id, payload);
      alert("Cập nhật tiến độ thành công!");
      setShowUpdateModal(false);
      setUpdateForm({ title: "", description: "", status: "" });
      fetchCaseDetail();
    } catch (error) {
      alert("Lỗi cập nhật: " + (error.response?.data?.message || error.message));
    }
  };

  const handleDownload = async (docId, fileName) => {
    try {
      const response = await caseService.downloadDocument(id, docId);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      alert("Không thể tải tài liệu.");
    }
  };

  const handleView = async (docId, fileName) => {
    try {
      const response = await caseService.viewDocument(id, docId);
      const contentType = response.headers['content-type'] || "";
      const file = new Blob([response.data], { type: contentType });
      const fileURL = URL.createObjectURL(file);

      const isViewable = contentType.includes("pdf") || 
                         contentType.includes("image") || 
                         contentType.includes("text");

      if (isViewable) {
          window.open(fileURL, "_blank");
      } else {
          const confirmDownload = window.confirm(
              `Trình duyệt không hỗ trợ xem trước file "${fileName}". Bạn có muốn tải về không?`
          );
          if (confirmDownload) {
              const link = document.createElement('a');
              link.href = fileURL;
              link.setAttribute('download', fileName);
              document.body.appendChild(link);
              link.click();
              link.parentNode.removeChild(link);
          }
      }
    } catch (error) {
      alert("Không thể xem tài liệu. Lỗi server hoặc file không tồn tại.");
      console.error("View error:", error);
    }
  };

  const isLawyer = user?.role === "LAWYER";

  if (loading) return <Layout><div>Đang tải...</div></Layout>;
  if (!caseData) return <Layout><div>Không tìm thấy vụ án</div></Layout>;

  return (
    <Layout>
      <div className="max-w-6xl mx-auto p-4 space-y-6">
        {/* Header */}
        <div className="bg-white dark:bg-slate-900 p-6 rounded-xl shadow-sm border dark:border-slate-800">
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-3xl font-bold text-slate-800 dark:text-white mb-2">{caseData.title}</h1>
              <span className={`px-3 py-1 rounded-full text-sm font-semibold ${
                caseData.status === 'COMPLETED' ? 'bg-green-100 text-green-700' : 
                caseData.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-700'
              }`}>
                {caseData.status === 'IN_PROGRESS' ? 'Đang thực hiện' : 
                 caseData.status === 'COMPLETED' ? 'Hoàn thành' : 
                 caseData.status === 'CANCELLED' ? 'Đã hủy' : caseData.status}
              </span>
            </div>
            <div className="text-right text-sm text-slate-500">
              <p>Ngày tạo: {new Date(caseData.createdAt).toLocaleDateString()}</p>
              <p>Mã hồ sơ: #{caseData.caseId}</p>
            </div>
          </div>
          
          <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="p-4 bg-slate-50 dark:bg-slate-800 rounded-lg">
              <p className="text-sm text-slate-500">Khách hàng</p>
              <p className="font-medium text-lg">{caseData.clientName}</p>
            </div>
            <div className="p-4 bg-slate-50 dark:bg-slate-800 rounded-lg">
              <p className="text-sm text-slate-500">Luật sư phụ trách</p>
              <p className="font-medium text-lg">{caseData.lawyerName}</p>
            </div>
          </div>
          
          <div className="mt-6">
            <h3 className="font-semibold mb-2">Mô tả vụ việc:</h3>
            <p className="text-slate-700 dark:text-slate-300 leading-relaxed bg-slate-50 dark:bg-slate-800 p-4 rounded-lg">
              {caseData.description}
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Cột trái: Timeline */}
          <div className="lg:col-span-2 bg-white dark:bg-slate-900 p-6 rounded-xl shadow-sm border dark:border-slate-800">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-bold">Tiến độ xử lý</h2>
              {isLawyer && (
                <div className="flex gap-2">
                  <button 
                    onClick={() => setShowUpdateModal(true)}
                    className="bg-primary text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-600 transition flex items-center gap-1"
                  >
                    <span className="material-symbols-outlined text-sm">edit_note</span> Cập nhật
                  </button>
                  <button 
                    onClick={handleDeleteCase}
                    className="bg-red-100 text-red-600 px-4 py-2 rounded-lg text-sm hover:bg-red-200 transition flex items-center gap-1 border border-red-200"
                    title="Xóa hoàn toàn vụ án này"
                  >
                    <span className="material-symbols-outlined text-sm">delete_forever</span> Xóa hồ sơ
                  </button>
                </div>
              )}
            </div>

            <div className="space-y-6 relative border-l-2 border-slate-200 dark:border-slate-700 ml-3 pl-6">
              {caseData.updates && caseData.updates.map((update) => (
                <div key={update.updateId} className="relative">
                  <div className="absolute -left-[31px] top-0 w-4 h-4 rounded-full bg-blue-500 border-4 border-white dark:border-slate-900"></div>
                  <div className="mb-1 flex justify-between items-center">
                    <h3 className="font-semibold text-lg">{update.title}</h3>
                    <span className="text-xs text-slate-500">{new Date(update.createdAt).toLocaleDateString()}</span>
                  </div>
                  <p className="text-slate-600 dark:text-slate-400 text-sm mb-2">{update.description}</p>
                  <p className="text-xs text-slate-400 italic">Cập nhật bởi: {update.createdByName}</p>
                </div>
              ))}
              {(!caseData.updates || caseData.updates.length === 0) && (
                <p className="text-slate-500 italic">Chưa có cập nhật nào.</p>
              )}
            </div>
          </div>

          {/* Cột phải: Tài liệu */}
          <div className="bg-white dark:bg-slate-900 p-6 rounded-xl shadow-sm border dark:border-slate-800 h-fit">
            <h2 className="text-xl font-bold mb-4">Tài liệu hồ sơ</h2>
            
            <div className="space-y-3 mb-6">
              {caseData.documents && caseData.documents.map((doc) => (
                <div 
                  key={doc.docId} 
                  className="flex items-center justify-between p-3 rounded-lg border hover:bg-slate-50 dark:hover:bg-slate-800 transition"
                >
                  <div className="flex items-center overflow-hidden mr-2">
                    <span className="material-symbols-outlined text-red-500 mr-3">description</span>
                    <div className="overflow-hidden">
                      <button 
                        onClick={() => handleView(doc.docId, doc.fileName)}
                        className="text-sm font-medium truncate text-blue-600 hover:underline block text-left"
                        title="Xem tài liệu"
                      >
                          {doc.fileName}
                      </button>
                      <p className="text-xs text-slate-500">
                        {new Date(doc.uploadedAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                  
                  <div className="flex gap-1">
                    <button
                        onClick={() => handleView(doc.docId, doc.fileName)}
                        className="p-2 text-slate-400 hover:text-green-600 rounded-full hover:bg-green-50 transition"
                        title="Xem trực tiếp"
                    >
                        <span className="material-symbols-outlined text-xl">visibility</span>
                    </button>

                    <button
                        onClick={() => handleDownload(doc.docId, doc.fileName)}
                        className="p-2 text-slate-400 hover:text-blue-600 rounded-full hover:bg-blue-50 transition"
                        title="Tải về máy"
                    >
                        <span className="material-symbols-outlined text-xl">download</span>
                    </button>

                    {isLawyer && (
                        <button
                            onClick={() => handleDeleteDocument(doc.docId, doc.fileName)}
                            className="p-2 text-slate-400 hover:text-red-600 rounded-full hover:bg-red-50 transition"
                            title="Xóa tài liệu"
                        >
                            <span className="material-symbols-outlined text-xl">delete</span>
                        </button>
                    )}
                  </div>
                </div>
              ))}
              {(!caseData.documents || caseData.documents.length === 0) && (
                <p className="text-sm text-slate-500 italic text-center py-4">Chưa có tài liệu nào.</p>
              )}
            </div>

            {isLawyer && (
              <div className="border-t pt-4">
                <label className="block w-full cursor-pointer group">
                  <span className="sr-only">Chọn tài liệu</span>
                  <div className="flex items-center justify-center w-full px-4 py-2 border-2 border-dashed border-blue-300 rounded-lg hover:bg-blue-50 transition cursor-pointer text-blue-600">
                    <span className="material-symbols-outlined mr-2">upload_file</span>
                    <span className="text-sm font-semibold">Thêm tài liệu mới</span>
                  </div>
                  <input 
                    type="file" 
                    className="hidden"
                    onChange={handleFileUpload}
                  />
                </label>
              </div>
            )}
          </div>
        </div>
      </div>

      {showUpdateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 animate-fade-in">
          <div className="bg-white dark:bg-slate-900 rounded-xl p-6 w-full max-w-md shadow-2xl">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-xl font-bold">Cập nhật tiến độ</h3>
              <button onClick={() => setShowUpdateModal(false)} className="text-slate-400 hover:text-slate-600">
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
            
            <form onSubmit={handleUpdateProgress} className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">Tiêu đề cập nhật</label>
                <input
                  type="text"
                  placeholder="VD: Đã nộp đơn lên tòa"
                  className="w-full p-2 border border-slate-300 rounded-lg dark:bg-slate-800 dark:border-slate-700 focus:ring-2 focus:ring-blue-500 outline-none"
                  value={updateForm.title}
                  onChange={e => setUpdateForm({...updateForm, title: e.target.value})}
                  required
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-1">Chi tiết</label>
                <textarea
                  placeholder="Mô tả chi tiết công việc đã thực hiện..."
                  className="w-full p-2 border border-slate-300 rounded-lg dark:bg-slate-800 dark:border-slate-700 focus:ring-2 focus:ring-blue-500 outline-none"
                  rows="3"
                  value={updateForm.description}
                  onChange={e => setUpdateForm({...updateForm, description: e.target.value})}
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Trạng thái vụ án</label>
                <select
                  className="w-full p-2 border border-slate-300 rounded-lg dark:bg-slate-800 dark:border-slate-700 focus:ring-2 focus:ring-blue-500 outline-none"
                  value={updateForm.status}
                  onChange={e => setUpdateForm({...updateForm, status: e.target.value})}
                >
                  <option value="">-- Giữ nguyên trạng thái cũ --</option>
                  <option value="IN_PROGRESS">Đang xử lý</option>
                  <option value="COMPLETED">Hoàn thành</option>
                  <option value="CANCELLED">Đã hủy / Tạm dừng</option>
                </select>
              </div>

              <div className="flex justify-end gap-2 pt-4">
                <button 
                  type="button" 
                  onClick={() => setShowUpdateModal(false)} 
                  className="px-4 py-2 text-slate-600 hover:bg-slate-100 rounded-lg transition"
                >
                  Hủy
                </button>
                <button 
                  type="submit" 
                  className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-blue-700 transition shadow-md"
                >
                  Lưu cập nhật
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </Layout>
  );
}