import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import Header from "../components/Header";
import axiosInstance from "../utils/axiosInstance";

export default function LawyerDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [lawyer, setLawyer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [reviews, setReviews] = useState([]);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [reviewsPagination, setReviewsPagination] = useState({
    page: 0,
    size: 5,
    totalPages: 0,
    totalElements: 0,
  });

  useEffect(() => {
    fetchLawyerDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (activeTab === 'reviews') {
      fetchReviews();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab, reviewsPagination.page]);

  const fetchLawyerDetail = async () => {
    try {
      setLoading(true);
      const response = await axiosInstance.get(`/api/lawyers/${id}`);
      if (response.data.success) {
        setLawyer(response.data.data);
      }
    } catch (error) {
      console.error('Error fetching lawyer detail:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchReviews = async () => {
    try {
      setReviewsLoading(true);
      const response = await axiosInstance.get(`/api/lawyers/${id}/reviews`, {
        params: {
          page: reviewsPagination.page,
          size: reviewsPagination.size,
        },
      });
      if (response.data.success) {
        const data = response.data.data;
        setReviews(data.content || []);
        setReviewsPagination(prev => ({
          ...prev,
          totalPages: data.totalPages || 0,
          totalElements: data.totalElements || 0,
        }));
      }
    } catch (error) {
      console.error('Error fetching reviews:', error);
    } finally {
      setReviewsLoading(false);
    }
  };

  // Helper function to get full avatar URL
  const getAvatarUrl = (avatarUrl, fullName) => {
    if (!avatarUrl) {
      return `https://ui-avatars.com/api/?name=${encodeURIComponent(fullName || 'Lawyer')}&size=160&background=3b82f6&color=fff&bold=true`;
    }
    // If avatarUrl is relative path, prepend backend URL
    if (avatarUrl.startsWith('/uploads') || avatarUrl.startsWith('uploads')) {
      return `http://localhost:8080${avatarUrl.startsWith('/') ? '' : '/'}${avatarUrl}`;
    }
    // If already full URL, return as is
    return avatarUrl;
  };

  // Helper function to get full file URL (for certificates, documents, etc.)
  const getFileUrl = (fileUrl) => {
    if (!fileUrl) return null;
    // If fileUrl is relative path, prepend backend URL
    if (fileUrl.startsWith('/uploads') || fileUrl.startsWith('uploads')) {
      return `http://localhost:8080${fileUrl.startsWith('/') ? '' : '/'}${fileUrl}`;
    }
    // If already full URL, return as is
    return fileUrl;
  };

  const renderStars = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;

    for (let i = 0; i < fullStars; i++) {
      stars.push(
        <span key={`full-${i}`} className="material-symbols-outlined !text-lg" style={{ fontVariationSettings: "'FILL' 1" }}>
          star
        </span>
      );
    }
    if (hasHalfStar) {
      stars.push(
        <span key="half" className="material-symbols-outlined !text-lg">
          star_half
        </span>
      );
    }
    const remainingStars = 5 - Math.ceil(rating);
    for (let i = 0; i < remainingStars; i++) {
      stars.push(
        <span key={`empty-${i}`} className="material-symbols-outlined !text-lg">
          star
        </span>
      );
    }
    return stars;
  };

  if (loading) {
    return (
      <>
        <Header />
        <div className="flex justify-center items-center min-h-screen">
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-slate-600 dark:text-slate-400">Đang tải thông tin luật sư...</p>
          </div>
        </div>
      </>
    );
  }

  if (!lawyer) {
    return (
      <>
        <Header />
        <div className="flex justify-center items-center min-h-screen">
          <div className="text-center">
            <span className="material-symbols-outlined text-6xl text-slate-400">person_off</span>
            <p className="mt-4 text-slate-600 dark:text-slate-400 text-lg">Không tìm thấy thông tin luật sư</p>
            <button
              onClick={() => navigate('/find-lawyer')}
              className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Quay lại
            </button>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <Header />
      <link
        href="https://fonts.googleapis.com/css2?family=Lora:wght@700&family=Noto+Sans:wght@400;500;700&display=swap"
        rel="stylesheet"
      />
      <link
        href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
        rel="stylesheet"
      />

      {/* Breadcrumb */}
      <div className="bg-slate-50 dark:bg-slate-900 py-4 border-b border-slate-200 dark:border-slate-800">
        <div className="container mx-auto px-4">
          <nav className="flex items-center gap-2 text-sm">
            <Link to="/" className="text-slate-600 hover:text-blue-600">Trang chủ</Link>
            <span className="text-slate-400">/</span>
            <Link to="/find-lawyer" className="text-slate-600 hover:text-blue-600">Tìm luật sư</Link>
            <span className="text-slate-400">/</span>
            <span className="text-slate-900 dark:text-slate-100">{lawyer.fullName}</span>
          </nav>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Sidebar - Lawyer Info */}
          <aside className="lg:col-span-1">
            <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 sticky top-24">
              {/* Avatar */}
              <div className="text-center mb-6">
                <div className="relative inline-block">
                  <img
                    className="w-40 h-40 rounded-full object-cover mx-auto border-4 border-blue-600"
                    src={getAvatarUrl(lawyer.avatarUrl, lawyer.fullName)}
                    alt={lawyer.fullName}
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(lawyer.fullName || 'L')}&size=160&background=3b82f6&color=fff&bold=true`;
                    }}
                  />
                  {lawyer.verificationStatus === 'APPROVED' && (
                    <span
                      className="material-symbols-outlined absolute bottom-2 right-2 text-white bg-blue-600 rounded-full p-2 text-2xl"
                      style={{ fontVariationSettings: "'FILL' 1" }}
                    >
                      verified
                    </span>
                  )}
                </div>
                <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100 mt-4 font-serif">
                  Luật sư {lawyer.fullName}
                </h1>
                
                {/* Rating */}
                <div className="flex items-center justify-center gap-1 text-yellow-400 mt-2">
                  {renderStars(lawyer.averageRating || 0)}
                </div>
                <p className="text-slate-500 text-sm mt-1">
                  {lawyer.averageRating?.toFixed(1) || '0.0'}/5.0 ({lawyer.reviewCount || 0} đánh giá)
                </p>
              </div>

              {/* Contact Info */}
              <div className="space-y-4 mb-6">
                <div className="flex items-start gap-3">
                  <span className="material-symbols-outlined text-slate-400">mail</span>
                  <div className="flex-1">
                    <p className="text-xs text-slate-500 dark:text-slate-400">Email</p>
                    <p className="text-sm text-slate-900 dark:text-slate-100">{lawyer.email}</p>
                  </div>
                </div>
                
                {lawyer.phoneNumber && (
                  <div className="flex items-start gap-3">
                    <span className="material-symbols-outlined text-slate-400">call</span>
                    <div className="flex-1">
                      <p className="text-xs text-slate-500 dark:text-slate-400">Số điện thoại</p>
                      <p className="text-sm text-slate-900 dark:text-slate-100">{lawyer.phoneNumber}</p>
                    </div>
                  </div>
                )}

                <div className="flex items-start gap-3">
                  <span className="material-symbols-outlined text-slate-400">location_on</span>
                  <div className="flex-1">
                    <p className="text-xs text-slate-500 dark:text-slate-400">Khu vực</p>
                    <p className="text-sm text-slate-900 dark:text-slate-100">{lawyer.barAssociationName}</p>
                  </div>
                </div>

                {lawyer.officeAddress && (
                  <div className="flex items-start gap-3">
                    <span className="material-symbols-outlined text-slate-400">business</span>
                    <div className="flex-1">
                      <p className="text-xs text-slate-500 dark:text-slate-400">Văn phòng</p>
                      <p className="text-sm text-slate-900 dark:text-slate-100">{lawyer.officeAddress}</p>
                    </div>
                  </div>
                )}

                <div className="flex items-start gap-3">
                  <span className="material-symbols-outlined text-slate-400">workspace_premium</span>
                  <div className="flex-1">
                    <p className="text-xs text-slate-500 dark:text-slate-400">Kinh nghiệm</p>
                    <p className="text-sm text-slate-900 dark:text-slate-100">{lawyer.yearsOfExp || 0} năm</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <span className="material-symbols-outlined text-slate-400">badge</span>
                  <div className="flex-1">
                    <p className="text-xs text-slate-500 dark:text-slate-400">Chứng chỉ hành nghề</p>
                    <p className="text-sm text-slate-900 dark:text-slate-100">{lawyer.barLicenseId}</p>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="space-y-3">
                <button className="w-full py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold flex items-center justify-center gap-2">
                  <span className="material-symbols-outlined">calendar_month</span>
                  Đặt lịch hẹn
                </button>
                <button className="w-full py-3 border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 font-semibold flex items-center justify-center gap-2">
                  <span className="material-symbols-outlined">chat</span>
                  Nhắn tin
                </button>
              </div>
            </div>
          </aside>

          {/* Main Content */}
          <main className="lg:col-span-2">
            <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
              {/* Tabs */}
              <div className="border-b border-slate-200 dark:border-slate-800 mb-6">
                <nav className="flex gap-8">
                  <button
                    onClick={() => setActiveTab('overview')}
                    className={`pb-4 font-semibold border-b-2 transition-colors ${
                      activeTab === 'overview'
                        ? 'border-blue-600 text-blue-600'
                        : 'border-transparent text-slate-500 hover:text-slate-700'
                    }`}
                  >
                    Tổng quan
                  </button>
                  <button
                    onClick={() => setActiveTab('reviews')}
                    className={`pb-4 font-semibold border-b-2 transition-colors ${
                      activeTab === 'reviews'
                        ? 'border-blue-600 text-blue-600'
                        : 'border-transparent text-slate-500 hover:text-slate-700'
                    }`}
                  >
                    Đánh giá ({lawyer.reviewCount || 0})
                  </button>
                </nav>
              </div>

              {/* Tab Content */}
              {activeTab === 'overview' && (
                <div className="space-y-6">
                  {/* About */}
                  <section>
                    <h2 className="text-xl font-bold text-slate-900 dark:text-slate-100 mb-3 font-serif">
                      Giới thiệu
                    </h2>
                    <p className="text-slate-600 dark:text-slate-300 leading-relaxed">
                      {lawyer.bio || 'Chưa có thông tin giới thiệu.'}
                    </p>
                  </section>

                  {/* Specializations */}
                  <section>
                    <h2 className="text-xl font-bold text-slate-900 dark:text-slate-100 mb-3 font-serif">
                      Lĩnh vực chuyên môn
                    </h2>
                    <div className="flex flex-wrap gap-2">
                      {(lawyer.specializations || []).map((spec, idx) => (
                        <span
                          key={idx}
                          className="px-4 py-2 bg-blue-600/10 text-blue-600 rounded-full font-semibold text-sm"
                        >
                          {spec}
                        </span>
                      ))}
                    </div>
                  </section>

                  {/* Certificate */}
                  {lawyer.certificateUrl && (
                    <section>
                      <h2 className="text-xl font-bold text-slate-900 dark:text-slate-100 mb-3 font-serif">
                        Chứng chỉ hành nghề
                      </h2>
                      <div className="border border-slate-200 dark:border-slate-800 rounded-lg overflow-hidden">
                        <img
                          src={getFileUrl(lawyer.certificateUrl)}
                          alt="Chứng chỉ hành nghề"
                          className="w-full h-auto"
                          onError={(e) => {
                            e.target.style.display = 'none';
                            console.error('Certificate image failed to load:', lawyer.certificateUrl);
                          }}
                        />
                      </div>
                    </section>
                  )}

                  {/* Additional Info */}
                  <section>
                    <h2 className="text-xl font-bold text-slate-900 dark:text-slate-100 mb-3 font-serif">
                      Thông tin bổ sung
                    </h2>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="p-4 bg-slate-50 dark:bg-slate-800 rounded-lg">
                        <p className="text-sm text-slate-500 dark:text-slate-400">Trạng thái</p>
                        <p className="text-lg font-semibold text-slate-900 dark:text-slate-100">
                          {lawyer.verificationStatus === 'APPROVED' ? '✅ Đã xác minh' : '⏳ Chờ xác minh'}
                        </p>
                      </div>
                      <div className="p-4 bg-slate-50 dark:bg-slate-800 rounded-lg">
                        <p className="text-sm text-slate-500 dark:text-slate-400">Đánh giá trung bình</p>
                        <p className="text-lg font-semibold text-slate-900 dark:text-slate-100">
                          ⭐ {lawyer.averageRating?.toFixed(1) || '0.0'}/5.0
                        </p>
                      </div>
                    </div>
                  </section>
                </div>
              )}

              {activeTab === 'reviews' && (
                <div className="space-y-6">
                  {reviewsLoading ? (
                    <div className="flex justify-center items-center py-12">
                      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                    </div>
                  ) : reviews.length === 0 ? (
                    <div className="text-center py-12 text-slate-500">
                      <span className="material-symbols-outlined text-5xl mb-4">reviews</span>
                      <p>Chưa có đánh giá nào</p>
                    </div>
                  ) : (
                    <>
                      {/* Review Stats */}
                      <div className="bg-slate-50 dark:bg-slate-800/50 rounded-lg p-6 flex items-center justify-between">
                        <div>
                          <div className="flex items-center gap-2 mb-2">
                            <span className="text-4xl font-bold text-slate-900 dark:text-slate-100">
                              {lawyer.averageRating?.toFixed(1) || '0.0'}
                            </span>
                            <div className="flex items-center text-yellow-400 text-xl">
                              {renderStars(lawyer.averageRating || 0)}
                            </div>
                          </div>
                          <p className="text-slate-600 dark:text-slate-400">
                            {reviewsPagination.totalElements} đánh giá
                          </p>
                        </div>
                      </div>

                      {/* Reviews List */}
                      <div className="space-y-4">
                        {reviews.map((review) => (
                          <div key={review.appointmentId} className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-lg p-6">
                            <div className="flex items-start gap-4">
                              {/* Avatar */}
                              <img
                                src={getAvatarUrl(review.citizenAvatar, review.citizenName)}
                                alt={review.citizenName}
                                className="w-12 h-12 rounded-full object-cover"
                                onError={(e) => {
                                  e.target.onerror = null;
                                  e.target.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(review.citizenName || 'U')}&size=48&background=3b82f6&color=fff&bold=true`;
                                }}
                              />
                              
                              {/* Review Content */}
                              <div className="flex-1">
                                <div className="flex items-center justify-between mb-2">
                                  <h4 className="font-semibold text-slate-900 dark:text-slate-100">
                                    {review.citizenName}
                                  </h4>
                                  <span className="text-sm text-slate-500 dark:text-slate-400">
                                    {new Date(review.reviewedAt).toLocaleDateString('vi-VN')}
                                  </span>
                                </div>
                                
                                {/* Rating Stars */}
                                <div className="flex items-center gap-1 text-yellow-400 mb-3">
                                  {renderStars(review.rating || 0)}
                                </div>
                                
                                {/* Comment */}
                                {review.reviewComment && (
                                  <p className="text-slate-600 dark:text-slate-300">
                                    {review.reviewComment}
                                  </p>
                                )}
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>

                      {/* Pagination */}
                      {reviewsPagination.totalPages > 1 && (
                        <div className="flex justify-center gap-2 mt-6">
                          <button
                            onClick={() => setReviewsPagination(prev => ({ ...prev, page: Math.max(0, prev.page - 1) }))}
                            disabled={reviewsPagination.page === 0}
                            className="px-4 py-2 border border-slate-300 dark:border-slate-600 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            Trước
                          </button>
                          <span className="px-4 py-2 text-slate-600 dark:text-slate-400">
                            Trang {reviewsPagination.page + 1} / {reviewsPagination.totalPages}
                          </span>
                          <button
                            onClick={() => setReviewsPagination(prev => ({ ...prev, page: Math.min(prev.totalPages - 1, prev.page + 1) }))}
                            disabled={reviewsPagination.page >= reviewsPagination.totalPages - 1}
                            className="px-4 py-2 border border-slate-300 dark:border-slate-600 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            Sau
                          </button>
                        </div>
                      )}
                    </>
                  )}
                </div>
              )}
            </div>
          </main>
        </div>
      </div>
    </>
  );
}
