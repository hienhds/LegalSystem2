import React, { useState } from "react";
import axiosInstance from "../utils/axiosInstance";
import Header from "../components/Header";
import useUserProfile from "../hooks/useUserProfile";

export default function UserProfile() {
  const { user, loading, refreshProfile } = useUserProfile();
  const [showEditModal, setShowEditModal] = useState(false);
  const [formData, setFormData] = useState({
    fullName: "",
    address: "",
    phoneNumber: "",
  });
  const [saving, setSaving] = useState(false);
  const [showAvatarModal, setShowAvatarModal] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [showLawyerModal, setShowLawyerModal] = useState(false);
  const [lawyerFormData, setLawyerFormData] = useState({
    barLicenseId: "",
    bio: "",
    officeAddress: "",
    yearsOfExp: "",
    barAssociationId: "",
    specializationIds: []
  });
  const [certificateFile, setCertificateFile] = useState(null);
  const [certificatePreview, setCertificatePreview] = useState(null);
  const [specializations, setSpecializations] = useState([]);
  const [barAssociations, setBarAssociations] = useState([]);
  const [submittingLawyer, setSubmittingLawyer] = useState(false);

  const handleOpenEditModal = () => {
    setFormData({
      fullName: user?.fullName || "",
      address: user?.address || "",
      phoneNumber: user?.phoneNumber || "",
    });
    setShowEditModal(true);
  };

  const handleCloseModal = () => {
    setShowEditModal(false);
    setFormData({ fullName: "", address: "", phoneNumber: "" });
  };

  const handleSaveProfile = async () => {
    try {
      setSaving(true);
      const response = await axiosInstance.put("/api/users/profile", formData);

      if (response.data.success) {
        alert("Cập nhật thông tin thành công!");
        handleCloseModal();
        if (refreshProfile) refreshProfile();
        window.location.reload();
      }
    } catch (error) {
      console.error("Error updating profile:", error);
      alert("Không thể cập nhật thông tin. Vui lòng thử lại.");
    } finally {
      setSaving(false);
    }
  };

  const handleAvatarClick = () => {
    setShowAvatarModal(true);
  };

  const handleCloseAvatarModal = () => {
    setShowAvatarModal(false);
    setSelectedFile(null);
    setPreviewUrl(null);
  };

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (!file.type.startsWith('image/')) {
        alert('Vui lòng chọn file ảnh!');
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        alert('Kích thước ảnh không được vượt quá 5MB!');
        return;
      }
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleUploadAvatar = async () => {
    if (!selectedFile) {
      alert('Vui lòng chọn ảnh!');
      return;
    }

    try {
      setUploading(true);
      const formData = new FormData();
      formData.append('file', selectedFile);

      const response = await axiosInstance.post("/api/users/avatar", formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      if (response.data.success) {
        alert('Cập nhật ảnh đại diện thành công!');
        handleCloseAvatarModal();
        window.location.reload();
      }
    } catch (error) {
      console.error('Error uploading avatar:', error);
      alert('Không thể tải ảnh lên. Vui lòng thử lại.');
    } finally {
      setUploading(false);
    }
  };

  const handleOpenLawyerModal = async () => {
    setShowLawyerModal(true);
    await Promise.all([fetchSpecializations(), fetchBarAssociations()]);
  };

  const handleCloseLawyerModal = () => {
    setShowLawyerModal(false);
    setLawyerFormData({
      barLicenseId: "",
      bio: "",
      officeAddress: "",
      yearsOfExp: "",
      barAssociationId: "",
      specializationIds: []
    });
    setCertificateFile(null);
    setCertificatePreview(null);
  };

  const fetchSpecializations = async () => {
    try {
      const response = await axiosInstance.get("/api/specialization");
      if (response.data.success) {
        console.log('Specializations data:', response.data.data);
        setSpecializations(response.data.data);
      }
    } catch (error) {
      console.error("Error fetching specializations:", error);
    }
  };

  const fetchBarAssociations = async () => {
    try {
      const response = await axiosInstance.get("/api/bar-association");
      if (response.data.success) {
        setBarAssociations(response.data.data);
      }
    } catch (error) {
      console.error("Error fetching bar associations:", error);
    }
  };

  const handleCertificateSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (!file.type.startsWith('image/')) {
        alert('Vui lòng chọn file ảnh!');
        return;
      }
      if (file.size > 10 * 1024 * 1024) {
        alert('Kích thước ảnh không được vượt quá 10MB!');
        return;
      }
      setCertificateFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setCertificatePreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSpecializationChange = (specId) => {
    const numericSpecId = parseInt(specId);
    console.log('Changing spec ID:', numericSpecId, 'Current IDs:', lawyerFormData.specializationIds);
    
    if (isNaN(numericSpecId)) {
      console.error('Invalid spec ID:', specId);
      return;
    }
    
    setLawyerFormData(prev => {
      const updatedIds = prev.specializationIds.includes(numericSpecId)
        ? prev.specializationIds.filter(id => id !== numericSpecId)
        : [...prev.specializationIds, numericSpecId];
      console.log('Updated IDs:', updatedIds);
      return { ...prev, specializationIds: updatedIds };
    });
  };

  const handleSubmitLawyer = async () => {
    if (!certificateFile) {
      alert('Vui lòng tải lên chứng chỉ hành nghề!');
      return;
    }

    if (!lawyerFormData.barLicenseId || !lawyerFormData.bio || !lawyerFormData.officeAddress || 
        !lawyerFormData.yearsOfExp || !lawyerFormData.barAssociationId || 
        lawyerFormData.specializationIds.length === 0) {
      alert('Vui lòng điền đầy đủ thông tin!');
      return;
    }

    try {
      setSubmittingLawyer(true);
      const formData = new FormData();
      
      formData.append('data', JSON.stringify({
        barLicenseId: lawyerFormData.barLicenseId,
        bio: lawyerFormData.bio,
        officeAddress: lawyerFormData.officeAddress,
        yearsOfExp: parseInt(lawyerFormData.yearsOfExp),
        barAssociationId: parseInt(lawyerFormData.barAssociationId),
        specializationIds: lawyerFormData.specializationIds.map(id => parseInt(id))
      }));
      formData.append('certificate', certificateFile);

      const response = await axiosInstance.post("/api/users/register-lawyer", formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      if (response.data.success) {
        alert('Đăng ký luật sư thành công! Vui lòng chờ xác minh từ admin.');
        handleCloseLawyerModal();
      }
    } catch (error) {
      console.error('Error registering lawyer:', error);
      alert('Không thể đăng ký luật sư. Vui lòng thử lại.');
    } finally {
      setSubmittingLawyer(false);
    }
  };

  if (loading) {
    return <div className="text-center py-10">Đang tải dữ liệu...</div>;
  }

  if (!user) {
    return <div className="text-center py-10 text-red-500">Không tìm thấy thông tin người dùng.</div>;
  }

  const avatarUrl = user.avatarUrl
    ? user.avatarUrl.startsWith("http")
      ? user.avatarUrl
      : `http://localhost:8080${user.avatarUrl}`
    : "https://ui-avatars.com/api/?name=User&background=cccccc&color=222222&size=128";

  return (
    <div className="bg-background-light dark:bg-background-dark font-display min-h-screen flex flex-col">
      <Header />
      <main className="w-full max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 sm:py-12">
        <div className="mb-8">
          <h1 className="text-4xl font-display font-bold text-center">Hồ Sơ Của Tôi</h1>
          <p className="text-center mt-2 text-lg text-gray-500">Quản lý thông tin cá nhân của bạn để có trải nghiệm tốt nhất.</p>
        </div>
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-md p-6 sm:p-8">
          <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6 sm:gap-8 border-b border-gray-200 dark:border-gray-700 pb-8 mb-8">
            <div className="relative group cursor-pointer" onClick={handleAvatarClick}>
              <div
                className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-32 border-4 border-gray-300 dark:border-gray-600 shadow-md transition-opacity group-hover:opacity-75"
                style={{ backgroundImage: `url('${avatarUrl}')` }}
                data-alt="User avatar"
              ></div>
              <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/0 group-hover:bg-black/40 transition-all">
                <div className="opacity-0 group-hover:opacity-100 transition-opacity text-white text-center">
                  <span className="material-symbols-outlined text-4xl">photo_camera</span>
                  <p className="text-sm font-medium mt-1">Đổi ảnh</p>
                </div>
              </div>
            </div>
            <div className="flex-1 text-center sm:text-left">
              <h2 className="text-3xl font-display font-bold">{user.fullName || "Chưa cập nhật"}</h2>
              <p className="text-gray-500 mt-2">{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : "Chưa cập nhật"}</p>
            </div>
            <div className="flex flex-col gap-3 sm:flex-row">
              <button
                onClick={handleOpenEditModal}
                className="group relative flex min-w-[140px] cursor-pointer items-center justify-center gap-2.5 overflow-hidden rounded-xl h-12 px-6 bg-gradient-to-r from-primary to-blue-600 text-white text-sm font-semibold leading-normal tracking-wide shadow-lg shadow-primary/30 hover:shadow-xl hover:shadow-primary/40 hover:scale-[1.02] transition-all duration-300 border border-primary/20"
              >
                <div className="absolute inset-0 bg-gradient-to-r from-white/0 via-white/10 to-white/0 transform -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
                <span className="material-symbols-outlined text-xl relative z-10">edit</span>
                <span className="truncate relative z-10">Chỉnh sửa hồ sơ</span>
              </button>
              <button
                onClick={handleOpenLawyerModal}
                className="group relative flex min-w-[140px] cursor-pointer items-center justify-center gap-2.5 overflow-hidden rounded-xl h-12 px-6 bg-gradient-to-r from-green-600 to-emerald-600 text-white text-sm font-semibold leading-normal tracking-wide shadow-lg shadow-green-600/30 hover:shadow-xl hover:shadow-green-600/40 hover:scale-[1.02] transition-all duration-300 border border-green-600/20"
              >
                <div className="absolute inset-0 bg-gradient-to-r from-white/0 via-white/10 to-white/0 transform -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
                <span className="material-symbols-outlined text-xl relative z-10">gavel</span>
                <span className="truncate relative z-10">Đăng ký luật sư</span>
              </button>
            </div>
          </div>
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row">
              <p className="w-full sm:w-1/3 text-gray-500 font-semibold">Họ và tên</p>
              <p className="w-full sm:w-2/3 font-medium">{user.fullName || "Chưa cập nhật"}</p>
            </div>
            <div className="flex flex-col sm:flex-row">
              <p className="w-full sm:w-1/3 text-gray-500 font-semibold">Email</p>
              <p className="w-full sm:w-2/3 font-medium">{user.email || "Chưa cập nhật"}</p>
            </div>
            <div className="flex flex-col sm:flex-row">
              <p className="w-full sm:w-1/3 text-gray-500 font-semibold">Số điện thoại</p>
              <p className="w-full sm:w-2/3 font-medium">{user.phoneNumber || "Chưa cập nhật"}</p>
            </div>
            <div className="flex flex-col sm:flex-row">
              <p className="w-full sm:w-1/3 text-gray-500 font-semibold">Địa chỉ</p>
              <p className="w-full sm:w-2/3 font-medium">{user.address || "Chưa cập nhật"}</p>
            </div>
          </div>
        </div>
      </main>

      {/* Edit Profile Modal */}
      {showEditModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-md w-full p-6 shadow-2xl">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Sửa thông tin</h2>
              <button
                onClick={handleCloseModal}
                className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  Họ và tên
                </label>
                <input
                  type="text"
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                  placeholder="Nhập họ và tên"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  Số điện thoại
                </label>
                <input
                  type="tel"
                  value={formData.phoneNumber}
                  onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                  className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                  placeholder="Nhập số điện thoại"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  Địa chỉ
                </label>
                <textarea
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                  placeholder="Nhập địa chỉ"
                  rows="3"
                />
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={handleCloseModal}
                className="flex-1 px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 font-medium"
              >
                Hủy
              </button>
              <button
                onClick={handleSaveProfile}
                disabled={saving}
                className="flex-1 px-4 py-2 rounded-lg bg-primary text-white hover:bg-primary/90 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {saving ? "Đang lưu..." : "Lưu thay đổi"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Avatar Upload Modal */}
      {showAvatarModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-md w-full p-6 shadow-2xl">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Cập nhật ảnh đại diện</h2>
              <button
                onClick={handleCloseAvatarModal}
                className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <div className="flex flex-col items-center space-y-6">
              {/* Preview Avatar */}
              <div className="relative">
                <div
                  className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-40 border-4 border-primary shadow-lg"
                  style={{ backgroundImage: `url('${previewUrl || avatarUrl}')` }}
                ></div>
              </div>

              {/* File Input */}
              <div className="w-full">
                <input
                  type="file"
                  id="avatar-input"
                  accept="image/*"
                  onChange={handleFileSelect}
                  className="hidden"
                />
                <label
                  htmlFor="avatar-input"
                  className="flex items-center justify-center gap-2 w-full px-4 py-3 rounded-lg border-2 border-dashed border-slate-300 dark:border-slate-700 hover:border-primary dark:hover:border-primary cursor-pointer transition-colors bg-slate-50 dark:bg-slate-800/50"
                >
                  <span className="material-symbols-outlined text-primary">upload</span>
                  <span className="text-slate-700 dark:text-slate-300 font-medium">
                    {selectedFile ? selectedFile.name : 'Chọn ảnh từ máy tính'}
                  </span>
                </label>
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-2 text-center">
                  JPG, PNG hoặc GIF (tối đa 5MB)
                </p>
              </div>

              {/* Drag & Drop hint */}
              {!selectedFile && (
                <div className="w-full text-center py-4 border-t border-slate-200 dark:border-slate-700">
                  <p className="text-sm text-slate-500 dark:text-slate-400">
                    Hoặc kéo thả ảnh vào đây
                  </p>
                </div>
              )}
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={handleCloseAvatarModal}
                className="flex-1 px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 font-medium"
              >
                Hủy
              </button>
              <button
                onClick={handleUploadAvatar}
                disabled={!selectedFile || uploading}
                className="flex-1 px-4 py-2 rounded-lg bg-primary text-white hover:bg-primary/90 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {uploading ? 'Đang tải lên...' : 'Lưu ảnh'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Lawyer Registration Modal */}
      {showLawyerModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6 shadow-2xl">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Đăng ký luật sư</h2>
              <button
                onClick={handleCloseLawyerModal}
                className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    Số giấy phép luật sư *
                  </label>
                  <input
                    type="text"
                    value={lawyerFormData.barLicenseId}
                    onChange={(e) => setLawyerFormData({ ...lawyerFormData, barLicenseId: e.target.value })}
                    className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                    placeholder="VD: BAR123456"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    Số năm kinh nghiệm *
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={lawyerFormData.yearsOfExp}
                    onChange={(e) => setLawyerFormData({ ...lawyerFormData, yearsOfExp: e.target.value })}
                    className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                    placeholder="Nhập số năm kinh nghiệm"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  Liên đoàn luật sư *
                </label>
                <select
                  value={lawyerFormData.barAssociationId}
                  onChange={(e) => setLawyerFormData({ ...lawyerFormData, barAssociationId: e.target.value })}
                  className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                >
                  <option value="">Chọn liên đoàn luật sư</option>
                  {barAssociations.map((ba) => (
                    <option key={ba.barAssociationId || ba.id} value={ba.barAssociationId || ba.id}>
                      {ba.associationName || ba.name || ba.barAssociationName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  Địa chỉ văn phòng *
                </label>
                <textarea
                  value={lawyerFormData.officeAddress}
                  onChange={(e) => setLawyerFormData({ ...lawyerFormData, officeAddress: e.target.value })}
                  className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                  placeholder="Nhập địa chỉ văn phòng"
                  rows="3"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  Mô tả kinh nghiệm *
                </label>
                <textarea
                  value={lawyerFormData.bio}
                  onChange={(e) => setLawyerFormData({ ...lawyerFormData, bio: e.target.value })}
                  className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                  placeholder="Mô tả về kinh nghiệm và chuyên môn..."
                  rows="4"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">
                  Chuyên môn (chọn ít nhất 1) *
                </label>
                <div className="grid grid-cols-2 gap-3 max-h-32 overflow-y-auto">
                  {specializations.map((spec) => {
                    const specId = spec.id || spec.specId || spec.specializationId;
                    const specName = spec.specName || spec.name || spec.specializationName;
                    return (
                      <label key={specId} className="flex items-center space-x-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={lawyerFormData.specializationIds.includes(parseInt(specId))}
                          onChange={() => handleSpecializationChange(parseInt(specId))}
                          className="w-4 h-4 text-primary border-slate-300 rounded focus:ring-primary focus:ring-2"
                        />
                        <span className="text-sm text-slate-700 dark:text-slate-300">{specName}</span>
                      </label>
                    );
                  })}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  Chứng chỉ hành nghề *
                </label>
                <div className="space-y-4">
                  {certificatePreview && (
                    <div className="flex justify-center">
                      <img
                        src={certificatePreview}
                        alt="Certificate preview"
                        className="max-w-full max-h-48 object-contain rounded-lg border border-slate-300 dark:border-slate-700"
                      />
                    </div>
                  )}
                  <input
                    type="file"
                    id="certificate-input"
                    accept="image/*"
                    onChange={handleCertificateSelect}
                    className="hidden"
                  />
                  <label
                    htmlFor="certificate-input"
                    className="flex items-center justify-center gap-2 w-full px-4 py-3 rounded-lg border-2 border-dashed border-slate-300 dark:border-slate-700 hover:border-primary dark:hover:border-primary cursor-pointer transition-colors bg-slate-50 dark:bg-slate-800/50"
                  >
                    <span className="material-symbols-outlined text-primary">upload</span>
                    <span className="text-slate-700 dark:text-slate-300 font-medium">
                      {certificateFile ? certificateFile.name : 'Chọn ảnh chứng chỉ'}
                    </span>
                  </label>
                  <p className="text-xs text-slate-500 dark:text-slate-400 text-center">
                    JPG, PNG hoặc GIF (tối đa 10MB)
                  </p>
                </div>
              </div>
            </div>

            <div className="flex gap-3 mt-8">
              <button
                onClick={handleCloseLawyerModal}
                className="flex-1 px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 font-medium"
              >
                Hủy
              </button>
              <button
                onClick={handleSubmitLawyer}
                disabled={submittingLawyer}
                className="flex-1 px-4 py-2 rounded-lg bg-green-600 text-white hover:bg-green-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {submittingLawyer ? 'Đang gửi...' : 'Đăng ký luật sư'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
