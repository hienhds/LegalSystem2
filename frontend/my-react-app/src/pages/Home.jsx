
import React, { useEffect, useState } from "react";
import Layout from "../components/Layout";
import { apiFetch } from "../utils/api";

export default function Home() {
  // State cho dữ liệu động
  const [lawyers, setLawyers] = useState([]);
  const [news, setNews] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { 
    setLoading(true);
    Promise.all([
      apiFetch("http://localhost:8080/api/search/popular-lawyers?limit=6").then(res => res.json()).catch(() => ({ data: [] })),
      apiFetch("http://localhost:8080/api/documents/trending?page=0&size=5").then(res => res.json()).catch(() => ({ data: { content: [] } }))
    ]).then(([lawyersData, newsData]) => {
      console.log('Luật sư nổi bật:', lawyersData);
      console.log('Tin pháp luật:', newsData);
      setLawyers(Array.isArray(lawyersData.data) ? lawyersData.data : []);
      setNews(Array.isArray(newsData.data?.content) ? newsData.data.content : []);
    }).finally(() => setLoading(false));
  }, []);

  return (
    <Layout showFooter={true}>
      {/* Hero Section */}
        <section className="relative bg-custom-blue-dark/5 dark:bg-background-dark">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 py-24 sm:py-32">
            <div className="flex flex-col gap-6 text-center items-center">
              <h1 className="text-slate-900 dark:text-white text-4xl font-black leading-tight tracking-[-0.033em] sm:text-5xl lg:text-6xl max-w-3xl">Kết nối người dân với luật sư nhanh chóng và hiệu quả.</h1>
              <p className="text-slate-700 dark:text-slate-300 text-lg font-normal leading-normal max-w-2xl">Tìm kiếm luật sư, nhận tư vấn pháp lý và tra cứu văn bản pháp luật một cách dễ dàng.</p>
              <div className="mt-6 w-full max-w-2xl">
                <div className="relative">
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4">
                    <span className="material-symbols-outlined text-slate-500">search</span>
                  </div>
                  <input className="block w-full rounded-full border-0 bg-white dark:bg-slate-800 py-4 pl-12 pr-32 text-slate-900 dark:text-slate-100 shadow-lg ring-1 ring-inset ring-slate-200 dark:ring-slate-700 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-custom-blue-dark sm:text-sm sm:leading-6" placeholder="Tìm kiếm luật sư, lĩnh vực pháp lý..." type="search" />
                  <div className="absolute inset-y-0 right-0 flex py-1.5 pr-1.5">
                    <button className="inline-flex items-center rounded-full px-6 text-sm font-semibold bg-custom-blue-dark text-white hover:bg-custom-blue-dark/90" type="submit">Tìm kiếm</button>
                  </div>
                </div>
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-2">Gợi ý: Luật sư đất đai, tư vấn ly hôn,...</p>
              </div>
            </div>
          </div>
        </section>
        {/* Legal Fields Section */}
        <section className="py-16 sm:py-24 bg-white dark:bg-slate-900">
          <div className="mx-auto max-w-7xl px-4 sm:px-6">
            <div className="text-center mb-12">
              <h2 className="text-slate-900 dark:text-slate-50 tracking-light text-3xl font-bold leading-tight sm:text-4xl sm:font-black sm:leading-tight sm:tracking-[-0.033em]">Các lĩnh vực pháp lý</h2>
              <p className="mt-4 text-slate-700 dark:text-slate-300 text-base font-normal leading-normal max-w-[720px] mx-auto">Tìm luật sư chuyên nghiệp theo lĩnh vực bạn quan tâm.</p>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 sm:gap-6">
              {/* Legal fields cards */}
              <a className="group flex flex-col items-center text-center p-4 rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-custom-blue-dark/5 dark:hover:bg-slate-800/50 transition-colors" href="#">
                <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-custom-blue-dark/10 text-custom-blue-dark group-hover:bg-custom-blue-dark group-hover:text-white transition-colors">
                  <span className="material-symbols-outlined text-3xl">family_restroom</span>
                </div>
                <h3 className="font-semibold text-slate-900 dark:text-slate-50">Hôn nhân & Gia đình</h3>
              </a>
              <a className="group flex flex-col items-center text-center p-4 rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-custom-blue-dark/5 dark:hover:bg-slate-800/50 transition-colors" href="#">
                <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-custom-blue-dark/10 text-custom-blue-dark group-hover:bg-custom-blue-dark group-hover:text-white transition-colors">
                  <span className="material-symbols-outlined text-3xl">landscape</span>
                </div>
                <h3 className="font-semibold text-slate-900 dark:text-slate-50">Đất đai</h3>
              </a>
              <a className="group flex flex-col items-center text-center p-4 rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-custom-blue-dark/5 dark:hover:bg-slate-800/50 transition-colors" href="#">
                <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-custom-blue-dark/10 text-custom-blue-dark group-hover:bg-custom-blue-dark group-hover:text-white transition-colors">
                  <span className="material-symbols-outlined text-3xl">business_center</span>
                </div>
                <h3 className="font-semibold text-slate-900 dark:text-slate-50">Doanh nghiệp</h3>
              </a>
              <a className="group flex flex-col items-center text-center p-4 rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-custom-blue-dark/5 dark:hover:bg-slate-800/50 transition-colors" href="#">
                <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-custom-blue-dark/10 text-custom-blue-dark group-hover:bg-custom-blue-dark group-hover:text-white transition-colors">
                  <span className="material-symbols-outlined text-3xl">gavel</span>
                </div>
                <h3 className="font-semibold text-slate-900 dark:text-slate-50">Hình sự</h3>
              </a>
              <a className="group flex flex-col items-center text-center p-4 rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-custom-blue-dark/5 dark:hover:bg-slate-800/50 transition-colors" href="#">
                <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-custom-blue-dark/10 text-custom-blue-dark group-hover:bg-custom-blue-dark group-hover:text-white transition-colors">
                  <span className="material-symbols-outlined text-3xl">work</span>
                </div>
                <h3 className="font-semibold text-slate-900 dark:text-slate-50">Lao động</h3>
              </a>
              <a className="group flex flex-col items-center text-center p-4 rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-custom-blue-dark/5 dark:hover:bg-slate-800/50 transition-colors" href="#">
                <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-custom-blue-dark/10 text-custom-blue-dark group-hover:bg-custom-blue-dark group-hover:text-white transition-colors">
                  <span className="material-symbols-outlined text-3xl">account_balance</span>
                </div>
                <h3 className="font-semibold text-slate-900 dark:text-slate-50">Hành chính</h3>
              </a>
            </div>
          </div>
        </section>
        {/* Featured Lawyers Section */}
        <section className="py-16 sm:py-24 bg-background-light dark:bg-background-dark">
          <div className="mx-auto max-w-7xl px-4 sm:px-6">
            <div className="text-center mb-12">
              <h2 className="text-slate-900 dark:text-slate-50 tracking-light text-3xl font-bold leading-tight sm:text-4xl sm:font-black sm:leading-tight sm:tracking-[-0.033em]">Luật sư nổi bật</h2>
              <p className="mt-4 text-slate-700 dark:text-slate-300 text-base font-normal leading-normal max-w-[720px] mx-auto">Đội ngũ luật sư giàu kinh nghiệm, sẵn sàng hỗ trợ bạn.</p>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {loading ? (
                <div className="col-span-3 text-center text-slate-500">Đang tải dữ liệu...</div>
              ) : lawyers.length > 0 ? (
                lawyers.map(lawyer => (
                  <div key={lawyer.lawyerId} className="bg-white dark:bg-slate-900 rounded-xl shadow-lg overflow-hidden border border-slate-200 dark:border-slate-800">
                    <img
                      alt={lawyer.fullName}
                      className="w-full h-56 object-cover object-center"
                      src={lawyer.avatarUrl
                        ? (lawyer.avatarUrl.startsWith('http')
                            ? lawyer.avatarUrl
                            : `http://localhost:8080${lawyer.avatarUrl}`)
                        : "https://via.placeholder.com/400x224?text=Avatar"}
                    />
                    <div className="p-6">
                      <h3 className="text-xl font-bold text-slate-900 dark:text-slate-50">{lawyer.fullName}</h3>
                      <p className="text-custom-blue-dark font-semibold mt-1">Đoàn luật sư: {lawyer.barAssociationName || "Chưa cập nhật"}</p>
                      <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">Kinh nghiệm: {typeof lawyer.yearsOfExp === 'number' ? `${lawyer.yearsOfExp} năm` : "Chưa cập nhật"}</p>
                      <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">Giới thiệu: {lawyer.bio || "Chưa cập nhật"}</p>
                      <a 
                        href={`/lawyer/${lawyer.lawyerId}`} 
                        className="inline-block mt-4 px-4 py-2 bg-custom-blue-dark text-white rounded-lg hover:bg-custom-blue-dark/90 transition-colors text-sm font-semibold"
                      >
                        Liên hệ luật sư
                      </a>
                    </div>
                  </div>
                ))
              ) : (
                <div className="col-span-3 text-center text-slate-500">Không có dữ liệu luật sư nổi bật.</div>
              )}
            </div>
          </div>
        </section>
        {/* Features Section */}
        <section className="py-16 sm:py-24 bg-white dark:bg-slate-900">
          <div className="mx-auto max-w-7xl px-4 sm:px-6">
            <div className="text-center mb-12">
              <h2 className="text-slate-900 dark:text-slate-50 tracking-light text-3xl font-bold leading-tight sm:text-4xl sm:font-black sm:leading-tight sm:tracking-[-0.033em]">Tính năng chính</h2>
              <p className="mt-4 text-slate-700 dark:text-slate-300 text-base font-normal leading-normal max-w-[720px] mx-auto">Các công cụ mạnh mẽ giúp bạn dễ dàng tiếp cận và quản lý thông tin pháp lý cần thiết.</p>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              <div className="p-6 flex flex-col items-center text-center bg-background-light dark:bg-slate-800/50 rounded-xl border border-slate-200 dark:border-slate-800">
                <div className="text-custom-blue-dark text-4xl mb-4"><span className="material-symbols-outlined">forum</span></div>
                <h3 className="font-bold text-lg text-slate-900 dark:text-slate-50">Tư vấn pháp lý</h3>
                <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">Kết nối trực tiếp với luật sư để nhận tư vấn chuyên sâu.</p>
              </div>
              <div className="p-6 flex flex-col items-center text-center bg-background-light dark:bg-slate-800/50 rounded-xl border border-slate-200 dark:border-slate-800">
                <div className="text-custom-blue-dark text-4xl mb-4"><span className="material-symbols-outlined">menu_book</span></div>
                <h3 className="font-bold text-lg text-slate-900 dark:text-slate-50">Tra cứu văn bản</h3>
                <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">Truy cập hệ thống văn bản pháp luật đầy đủ và cập nhật.</p>
              </div>
              <div className="p-6 flex flex-col items-center text-center bg-background-light dark:bg-slate-800/50 rounded-xl border border-slate-200 dark:border-slate-800">
                <div className="text-custom-blue-dark text-4xl mb-4"><span className="material-symbols-outlined">quiz</span></div>
                <h3 className="font-bold text-lg text-slate-900 dark:text-slate-50">Hỏi đáp pháp luật</h3>
                <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">Giải đáp các thắc mắc pháp lý thường gặp từ cộng đồng.</p>
              </div>
              <div className="p-6 flex flex-col items-center text-center bg-background-light dark:bg-slate-800/50 rounded-xl border border-slate-200 dark:border-slate-800">
                <div className="text-custom-blue-dark text-4xl mb-4"><span className="material-symbols-outlined">calendar_add_on</span></div>
                <h3 className="font-bold text-lg text-slate-900 dark:text-slate-50">Đặt lịch tư vấn</h3>
                <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">Chủ động sắp xếp lịch hẹn với luật sư phù hợp.</p>
              </div>
            </div>
          </div>
        </section>
        {/* News Section */}
        <section className="py-16 sm:py-24 bg-background-light dark:bg-background-dark">
          <div className="mx-auto max-w-7xl px-4 sm:px-6">
            <div className="text-center mb-12">
              <h2 className="text-slate-900 dark:text-slate-50 tracking-light text-3xl font-bold leading-tight sm:text-4xl sm:font-black sm:leading-tight sm:tracking-[-0.033em]">Tin pháp luật & Văn bản mới</h2>
              <p className="mt-4 text-slate-700 dark:text-slate-300 text-base font-normal leading-normal max-w-[720px] mx-auto">Cập nhật những thông tin, quy định pháp luật mới nhất.</p>
            </div>
            <div className="space-y-4">
              {loading ? (
                <div className="text-center text-slate-500">Đang tải dữ liệu...</div>
              ) : news.length > 0 ? (
                news.map(item => (
                  <a key={item.documentId} className="block p-4 bg-white dark:bg-slate-900 rounded-lg shadow-sm border border-slate-200 dark:border-slate-800 hover:shadow-md transition-shadow" href={`/document/${item.documentId}`}>
                    <p className="font-semibold text-custom-blue-dark dark:text-blue-400">{item.category || "TIN TỨC"}</p>
                    <h3 className="mt-1 font-bold text-lg text-slate-900 dark:text-slate-50">{item.title}</h3>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">{item.createdAt ? new Date(item.createdAt).toLocaleDateString() : ""}</p>
                  </a>
                ))
              ) : (
                <div className="text-center text-slate-500">Không có dữ liệu tin pháp luật mới.</div>
              )}
            </div>
          </div>
        </section>
    </Layout>
  );
}
