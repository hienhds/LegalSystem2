import axiosInstance from "../utils/axiosInstance";

export const caseService = {
  // 1. Tạo vụ án mới
  createCase: async (data) => {
    // Backend trả về ApiResponse<CaseResponse>
    return await axiosInstance.post("/api/cases", data);
  },

  // 2. Lấy chi tiết vụ án
  getCaseDetail: async (caseId) => {
    return await axiosInstance.get(`/api/cases/${caseId}`);
  },

  // 3. Cập nhật tiến độ
  updateProgress: async (caseId, data) => {
    return await axiosInstance.post(`/api/cases/${caseId}/updates`, data);
  },

  // 4. Upload tài liệu vụ án
  uploadDocument: async (caseId, file) => {
    const formData = new FormData();
    formData.append("file", file);
    return await axiosInstance.post(`/api/cases/${caseId}/documents`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },
  
  // 5. Lấy danh sách vụ án
  getMyCases: async (page = 0, size = 10, keyword = "") => {
    return await axiosInstance.get(`/api/cases`, {
      params: { page, size, keyword }
    });
  },

  // 6. Download tài liệu
  downloadDocument: async (caseId, docId) => {
    return await axiosInstance.get(`/api/cases/${caseId}/documents/${docId}/download`, {
        responseType: 'blob',
    });
  },

  // 7. Xem tài liệu trực tiếp
  viewDocument: async (caseId, docId) => {
    return await axiosInstance.get(`/api/cases/${caseId}/documents/${docId}/view`, {
        responseType: 'blob',
    });
  },

  // 8. Xóa tài liệu
  deleteDocument: async (caseId, docId) => {
    return await axiosInstance.delete(`/api/cases/${caseId}/documents/${docId}`);
  },

  // 9. Xóa vụ án
  deleteCase: async (caseId) => {
    return await axiosInstance.delete(`/api/cases/${caseId}`);
  }
};