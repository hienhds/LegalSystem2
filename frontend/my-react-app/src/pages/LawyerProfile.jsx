import React, { useState, useEffect } from "react";
import axios from "axios";
import Header from "../components/Header";
import useUserProfile from "../hooks/useUserProfile";

export default function LawyerProfile() {
  const { user, loading } = useUserProfile();
  const [showEditModal, setShowEditModal] = useState(false);
  const [showAvatarModal, setShowAvatarModal] = useState(false);
  const [barAssociations, setBarAssociations] = useState([]);
  const [formData, setFormData] = useState({
    fullName: "",
    phoneNumber: "",
    address: "",
    barLicenseId: "",
    bio: "",
    officeAddress: "",
    yearsOfExp: 0,
    barAssociationId: null,
  });
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    fetchBarAssociations();
  }, []);

  const fetchBarAssociations = async () => {
    try {
      const token = localStorage.getItem("accessToken");
      const response = await axios.get("http://localhost:8080/api/bar-association", {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.data.success) {
        setBarAssociations(response.data.data || []);
      }
    } catch (error) {
      console.error("Error fetching bar associations:", error);
    }
  };

  const handleOpenEditModal = () => {
    setFormData({
      fullName: user?.fullName || "",
      phoneNumber: user?.phoneNumber || "",
      address: user?.address || "",
      barLicenseId: user?.barLicenseId || "",
      bio: user?.bio || "",
      officeAddress: user?.officeAddress || "",
      yearsOfExp: user?.yearsOfExp || 0,
      barAssociationId: user?.barAssociationId || null,
    });
    setShowEditModal(true);
  };

  const handleCloseModal = () => {
    setShowEditModal(false);
  };

  const handleSaveProfile = async () => {
    try {
      setSaving(true);
      const token = localStorage.getItem("accessToken");
      const response = await axios.put(
        "http://localhost:8080/api/lawyers/profile",
        formData,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response.data.success) {
        alert("Cập nhật thông tin thành công!");
        handleCloseModal();
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
      const token = localStorage.getItem("accessToken");
      const formData = new FormData();
      formData.append('file', selectedFile);

      const response = await axios.post(
        "http://localhost:8080/api/users/avatar",
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'multipart/form-data',
          },
        }
      );

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

  if (loading) {
    return <div className="text-center py-10">Đang tải dữ liệu...</div>;
  }

  if (!user || !user.lawyerId) {
    return <div className="text-center py-10 text-red-500">Không tìm thấy thông tin luật sư.</div>;
  }

  const avatarUrl = user.avatarUrl
    ? user.avatarUrl.startsWith("http")
      ? user.avatarUrl
      : `http://localhost:8080${user.avatarUrl}`
    : "https://ui-avatars.com/api/?name=Lawyer&background=cccccc&color=222222&size=128";

  const certificateUrl = user.certificateImageUrl
    ? user.certificateImageUrl.startsWith("http")
      ? user.certificateImageUrl
      : `http://localhost:8080${user.certificateImageUrl}`
    : "";

  const verified = user.verificationStatus === "APPROVED";

  return (
    <div className="bg-background-light dark:bg-background-dark font-display min-h-screen flex flex-col">
      <Header />
      <main className="w-full max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 sm:py-12">
        <div className="mb-8">
          <h1 className="text-4xl font-display font-bold text-center">Hồ Sơ Luật Sư</h1>
          <p className="text-center mt-2 text-lg text-gray-500">Quản lý thông tin và chuyên môn của bạn trên LegalConnect.</p>
        </div>
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-md p-6 sm:p-8">
          <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6 sm:gap-8 border-b border-gray-200 dark:border-gray-700 pb-8 mb-8">
            <div className="relative group cursor-pointer" onClick={handleAvatarClick}>
              <div
                className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-32 border-4 border-gray-300 dark:border-gray-600 shadow-md flex-shrink-0 transition-opacity group-hover:opacity-75"
                style={{ backgroundImage: `url('${avatarUrl}')` }}
                data-alt="Lawyer avatar"
              ></div>
              <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/0 group-hover:bg-black/40 transition-all">
                <div className="opacity-0 group-hover:opacity-100 transition-opacity text-white text-center">
                  <span className="material-symbols-outlined text-4xl">photo_camera</span>
                  <p className="text-sm font-medium mt-1">Đổi ảnh</p>
                </div>
              </div>
            </div>
            <div className="flex-1 text-center sm:text-left">
              <div className="flex items-center justify-center sm:justify-start gap-3">
                <h2 className="text-3xl font-display font-bold">{user.fullName || "Chưa cập nhật"}</h2>
                {verified ? (
                  <span className="inline-flex items-center gap-1.5 bg-success/10 text-success px-2.5 py-1 rounded-full text-sm font-semibold">
                    <span className="material-symbols-outlined !text-base !font-semibold">verified</span>
                    Đã xác minh
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1.5 bg-yellow-100 text-yellow-700 px-2.5 py-1 rounded-full text-sm font-semibold">
                    <span className="material-symbols-outlined !text-base !font-semibold">hourglass</span>
                    Chờ xác minh
                  </span>
                )}
              </div>
              <p className="text-gray-500 text-base font-normal leading-normal mt-2">Mã thẻ luật sư: <span className="font-medium text-gray-900 dark:text-white">{user.barLicenseId || "Chưa cập nhật"}</span></p>
              <p className="text-gray-500 text-base font-normal leading-normal mt-1">{user.barAssociationName || "Chưa cập nhật"}</p>
            </div>
            <button
              onClick={handleOpenEditModal}
              className="group relative flex min-w-[140px] cursor-pointer items-center justify-center gap-2.5 overflow-hidden rounded-xl h-12 px-6 bg-gradient-to-r from-primary to-blue-600 text-white text-sm font-semibold leading-normal tracking-wide shadow-lg shadow-primary/30 hover:shadow-xl hover:shadow-primary/40 hover:scale-[1.02] transition-all duration-300 border border-primary/20"
            >
              <div className="absolute inset-0 bg-gradient-to-r from-white/0 via-white/10 to-white/0 transform -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
              <span className="material-symbols-outlined text-xl relative z-10">edit</span>
              <span className="truncate relative z-10">Chỉnh sửa hồ sơ</span>
            </button>
          </div>
          <div className="space-y-8">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-y-6 gap-x-8 text-left">
              <div className="md:col-span-1">
                <h3 className="text-lg font-bold text-primary dark:text-accent">Thông tin liên hệ</h3>
                <p className="text-sm text-gray-500 mt-1">Chi tiết liên lạc và địa chỉ văn phòng của bạn.</p>
              </div>
              <div className="md:col-span-2 space-y-4">
                <div className="flex flex-col sm:flex-row text-left">
                  <p className="w-full sm:w-1/3 text-gray-500 font-semibold">Email</p>
                  <p className="w-full sm:w-2/3 font-medium">{user.email || "Chưa cập nhật"}</p>
                </div>
                <div className="flex flex-col sm:flex-row text-left">
                  <p className="w-full sm:w-1/3 text-gray-500 font-semibold">Số điện thoại</p>
                  <p className="w-full sm:w-2/3 font-medium">{user.phoneNumber || "Chưa cập nhật"}</p>
                </div>
                <div className="flex flex-col sm:flex-row text-left">
                  <p className="w-full sm:w-1/3 text-gray-500 font-semibold">Địa chỉ văn phòng</p>
                  <p className="w-full sm:w-2/3 font-medium">{user.officeAddress || "Chưa cập nhật"}</p>
                </div>
              </div>
            </div>
            <div className="border-t border-gray-200 dark:border-gray-700"></div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-y-6 gap-x-8 text-left">
              <div className="md:col-span-1">
                <h3 className="text-lg font-bold text-primary dark:text-accent">Thông tin chuyên môn</h3>
                <p className="text-sm text-gray-500 mt-1">Giới thiệu về kinh nghiệm và lĩnh vực hành nghề của bạn.</p>
              </div>
              <div className="md:col-span-2 space-y-4">
                <div className="flex flex-col text-left">
                  <p className="text-gray-500 font-semibold mb-2">Tiểu sử</p>
                  <p className="text-gray-900 dark:text-white font-medium leading-relaxed">{user.bio || "Chưa cập nhật"}</p>
                </div>
                <div className="flex flex-col sm:flex-row text-left">
                  <p className="w-full sm:w-1/3 text-gray-500 font-semibold">Số năm kinh nghiệm</p>
                  <p className="w-full sm:w-2/3 font-medium">{user.yearsOfExp ? `${user.yearsOfExp} năm` : "Chưa cập nhật"}</p>
                </div>
                <div className="flex flex-col sm:flex-row text-left">
                  <p className="w-full sm:w-1/3 text-gray-500 font-semibold">Chuyên môn</p>
                  <div className="w-full sm:w-2/3 flex flex-wrap gap-2">
                    {Array.isArray(user.specializationNames) && user.specializationNames.length > 0 ? (
                      user.specializationNames.map((spec, idx) => (
                        <span
                          key={idx}
                          className="bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 px-3 py-1 rounded-full text-sm font-medium"
                        >
                          {spec}
                        </span>
                      ))
                    ) : (
                      <span className="text-gray-500">Chưa cập nhật</span>
                    )}
                  </div>
                </div>
                <div className="flex flex-col sm:flex-row text-left">
                  <p className="w-full sm:w-1/3 text-gray-500 font-semibold">Ảnh chứng chỉ</p>
                  <div className="w-full sm:w-2/3">
                    {certificateUrl ? (
                      <div className="bg-center bg-no-repeat aspect-video bg-cover rounded-lg border border-gray-200 dark:border-gray-700 w-full max-w-sm" data-alt="Certificate image" style={{ backgroundImage: `url('${certificateUrl}')` }}></div>
                    ) : (
                      <span className="text-gray-500">Chưa cập nhật</span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Edit Profile Modal */}
      {showEditModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50 overflow-y-auto">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-2xl w-full p-6 shadow-2xl my-8">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Chỉnh sửa thông tin luật sư</h2>
              <button
                onClick={handleCloseModal}
                className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <div className="space-y-6">
              {/* Thông tin cá nhân */}
              <div>
                <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-4">Thông tin cá nhân</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                      Địa chỉ
                    </label>
                    <input
                      type="text"
                      value={formData.address}
                      onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                      className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                      placeholder="Nhập địa chỉ"
                    />
                  </div>
                </div>
              </div>

              {/* Thông tin luật sư */}
              <div className="border-t border-slate-200 dark:border-slate-700 pt-6">
                <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-4">Thông tin hành nghề</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                      Mã thẻ luật sư
                    </label>
                    <input
                      type="text"
                      value={formData.barLicenseId}
                      onChange={(e) => setFormData({ ...formData, barLicenseId: e.target.value })}
                      className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                      placeholder="Nhập mã thẻ luật sư"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                      Số năm kinh nghiệm
                    </label>
                    <input
                      type="text"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      value={formData.yearsOfExp === 0 ? "" : formData.yearsOfExp}
                      onChange={(e) => {
                        const value = e.target.value.replace(/[^0-9]/g, "");
                        setFormData({ ...formData, yearsOfExp: value === "" ? 0 : parseInt(value) });
                      }}
                      className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                      placeholder="Nhập số năm kinh nghiệm"
                    />
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                      Liên đoàn luật sư
                    </label>
                    <select
                      value={formData.barAssociationId || ""}
                      onChange={(e) => setFormData({ ...formData, barAssociationId: e.target.value ? parseInt(e.target.value) : null })}
                      className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                    >
                      <option value="">-- Chọn liên đoàn --</option>
                      {barAssociations.map((ba) => (
                        <option key={ba.barAssociationId} value={ba.barAssociationId}>
                          {ba.barAssociationName}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                      Địa chỉ văn phòng
                    </label>
                    <input
                      type="text"
                      value={formData.officeAddress}
                      onChange={(e) => setFormData({ ...formData, officeAddress: e.target.value })}
                      className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                      placeholder="Nhập địa chỉ văn phòng"
                    />
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                      Tiểu sử
                    </label>
                    <textarea
                      value={formData.bio}
                      onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                      className="w-full px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary focus:border-primary"
                      placeholder="Nhập tiểu sử và giới thiệu bản thân"
                      rows="4"
                    />
                  </div>
                </div>
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
              <div className="relative">
                <div
                  className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-40 border-4 border-primary shadow-lg"
                  style={{ backgroundImage: `url('${previewUrl || avatarUrl}')` }}
                ></div>
              </div>

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
    </div>
  );
}
