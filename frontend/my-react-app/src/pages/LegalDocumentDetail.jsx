import React, { useState, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import Layout from "../components/Layout";
import axiosInstance from "../utils/axiosInstance";

export default function LegalDocumentDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [document, setDocument] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState("content");
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [parsedContent, setParsedContent] = useState(null);
  const [tableOfContents, setTableOfContents] = useState([]);

  useEffect(() => {
    fetchDocumentDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const fetchDocumentDetail = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await axiosInstance.get(`/api/documents/${id}`);
      
      if (response.data.success) {
        const docData = response.data.data;
        setDocument(docData);
        
        // Check if we have content or need to fetch file
        if (docData.content) {
          parseXMLContent(docData.content);
        } else if (docData.fileUrl) {
          fetchXMLFromUrl(docData.fileUrl);
        }
      }
    } catch (err) {
      console.error("Error fetching document:", err);
      setError("Không thể tải chi tiết văn bản. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const fetchXMLFromUrl = async (fileUrl) => {
    try {
      const response = await axiosInstance.get(fileUrl, {
        responseType: 'text'
      });
      parseXMLContent(response.data);
    } catch (err) {
      console.error("Error fetching XML file:", err);
    }
  };

  const parseXMLContent = (xmlString) => {
    try {
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(xmlString, "text/xml");
      
      // Extract table of contents
      const toc = [];
      const chuongs = xmlDoc.getElementsByTagName("Chuong");
      const dieus = xmlDoc.getElementsByTagName("Dieu");
      
      Array.from(chuongs).forEach((chuong) => {
        const soEl = chuong.getElementsByTagName("So")[0];
        const tenEl = chuong.getElementsByTagName("Ten")[0];
        const so = soEl?.textContent || "";
        const ten = tenEl?.textContent || "";
        toc.push({ type: 'chuong', id: `chuong-${so}`, title: `Chương ${so}: ${ten}`, level: 0 });
      });
      
      Array.from(dieus).forEach((dieu) => {
        const soEl = dieu.getElementsByTagName("So")[0];
        const tieuDeEl = dieu.getElementsByTagName("TieuDe")[0];
        const so = soEl?.textContent || "";
        const tieuDe = tieuDeEl?.textContent || "";
        toc.push({ type: 'dieu', id: `dieu-${so}`, title: `Điều ${so}. ${tieuDe}`, level: 1 });
      });
      
      setTableOfContents(toc);
      setParsedContent(xmlDoc);
    } catch (err) {
      console.error("Error parsing XML:", err);
    }
  };

  const renderXMLContent = () => {
    if (!parsedContent) {
      return (
        <div className="text-slate-700 dark:text-slate-300 text-justify leading-7">
          {document?.content || "Nội dung văn bản đang được cập nhật..."}
        </div>
      );
    }

    const content = [];
    const vanBan = parsedContent.getElementsByTagName("VanBan")[0];
    if (!vanBan) {
      return (
        <div className="text-slate-700 dark:text-slate-300 text-justify leading-7">
          Không thể hiển thị nội dung văn bản
        </div>
      );
    }

    // Get all children of VanBan in order
    const children = Array.from(vanBan.children);
    
    children.forEach((element, idx) => {
      const tagName = element.tagName;
      
      if (tagName === "Chuong") {
        const soEl = element.getElementsByTagName("So")[0];
        const tenEl = element.getElementsByTagName("Ten")[0];
        const so = soEl?.textContent || "";
        const ten = tenEl?.textContent || "";
        
        content.push(
          <div key={`chuong-${so}-${idx}`} id={`chuong-${so}`} className="mb-8 mt-6">
            <h2 className="text-xl font-bold text-center text-slate-900 dark:text-white" style={{ fontFamily: "'Lora', serif" }}>
              CHƯƠNG {so}: {ten.toUpperCase()}
            </h2>
          </div>
        );
      }
      
      if (tagName === "Dieu") {
        const soEl = element.getElementsByTagName("So")[0];
        const tieuDeEl = element.getElementsByTagName("TieuDe")[0];
        const noiDungEl = element.getElementsByTagName("NoiDung")[0];
        const khoans = element.getElementsByTagName("Khoan");
        
        const so = soEl?.textContent || "";
        const tieuDe = tieuDeEl?.textContent || "";
        const noiDung = noiDungEl?.textContent || "";
        
        content.push(
          <div key={`dieu-${so}-${idx}`} id={`dieu-${so}`} className="mb-6">
            <h3 className="text-lg font-bold pt-2 mb-3 text-slate-800 dark:text-slate-100" style={{ fontFamily: "'Lora', serif" }}>
              Điều {so}. {tieuDe}
            </h3>
            
            {noiDung && (
              <p className="text-slate-700 dark:text-slate-300 text-justify leading-7 mb-3">
                {noiDung}
              </p>
            )}
            
            {khoans.length > 0 && (
              <div className="space-y-3">
                {Array.from(khoans).map((khoan, kIdx) => {
                  const kSoEl = khoan.getElementsByTagName("So")[0];
                  const kNoiDungEl = khoan.getElementsByTagName("NoiDung")[0];
                  const kSo = kSoEl?.textContent || `${kIdx + 1}`;
                  const kNoiDung = kNoiDungEl?.textContent || "";
                  
                  return (
                    <p key={`khoan-${so}-${kIdx}`} className="text-slate-700 dark:text-slate-300 text-justify leading-7">
                      {kSo}. {kNoiDung}
                    </p>
                  );
                })}
              </div>
            )}
          </div>
        );
      }
    });
    
    return content.length > 0 ? (
      <div className="space-y-4">
        {content}
      </div>
    ) : (
      <div className="text-slate-700 dark:text-slate-300 text-justify leading-7">
        {document?.content || "Nội dung văn bản đang được cập nhật..."}
      </div>
    );
  };

  const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN");
  };

  const handleDownload = () => {
    alert("Chức năng tải về đang được phát triển");
  };

  const handlePrint = () => {
    window.print();
  };

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: document?.title,
        url: window.location.href,
      });
    } else {
      navigator.clipboard.writeText(window.location.href);
      alert("Đã copy link vào clipboard!");
    }
  };

  if (loading) {
    return (
      <Layout>
        <link
          href="https://fonts.googleapis.com/css2?family=Lora:wght@400;500;600;700&family=Noto+Sans:wght@400;500;600;700&display=swap"
          rel="stylesheet"
        />
        <div style={{ fontFamily: "'Noto Sans', sans-serif" }}>
          <div className="flex justify-center items-center py-20">
            <div className="text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-4 text-slate-600 dark:text-slate-400">Đang tải văn bản...</p>
            </div>
          </div>
        </div>
      </Layout>
    );
  }

  if (error || !document) {
    return (
      <Layout>
        <link
          href="https://fonts.googleapis.com/css2?family=Lora:wght@400;500;600;700&family=Noto+Sans:wght@400;500;600;700&display=swap"
          rel="stylesheet"
        />
        <div style={{ fontFamily: "'Noto Sans', sans-serif" }}>
          <div className="text-center py-20">
            <p className="text-red-600 dark:text-red-400">{error || "Không tìm thấy văn bản"}</p>
            <button
              onClick={() => navigate("/legal-documents")}
              className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              Quay lại danh sách
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <link
        href="https://fonts.googleapis.com/css2?family=Lora:wght@400;500;600;700&family=Noto+Sans:wght@400;500;600;700&display=swap"
        rel="stylesheet"
      />
      <div style={{ fontFamily: "'Noto Sans', sans-serif" }}>
        <main className="w-full max-w-7xl mx-auto px-4 py-8">
          <div className="grid grid-cols-12 gap-8">
            {/* Sidebar */}
            <aside className={`col-span-12 lg:col-span-4 xl:col-span-3 ${sidebarCollapsed ? "hidden lg:block" : ""}`}>
              <div className="sticky top-24 space-y-6">
                {/* Thông tin văn bản */}
                <div className="rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-4">
                  <h3 className="font-bold text-lg text-slate-900 dark:text-white" style={{ fontFamily: "'Lora', serif" }}>
                    Thông tin văn bản
                  </h3>
                  <div className="space-y-3 text-sm">
                    <div className="flex justify-between">
                      <span className="text-slate-500 dark:text-slate-400">Số/Ký hiệu:</span>
                      <span className="font-medium text-right">{document.documentNumber || "Đang cập nhật"}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-500 dark:text-slate-400">Loại:</span>
                      <span className="font-medium text-right">{document.documentType || "Văn bản"}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-500 dark:text-slate-400">Ngày ban hành:</span>
                      <span className="font-medium text-right">{formatDate(document.createdAt)}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-slate-500 dark:text-slate-400">Trạng thái:</span>
                      <span
                        className={`font-medium px-2 py-0.5 rounded-full text-xs ${
                          document.status === "ACTIVE"
                            ? "text-green-600 dark:text-green-500 bg-green-100 dark:bg-green-900/50"
                            : "text-red-600 dark:text-red-500 bg-red-100 dark:bg-red-900/50"
                        }`}
                      >
                        {document.status === "ACTIVE" ? "Còn hiệu lực" : "Hết hiệu lực"}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-500 dark:text-slate-400">Cơ quan:</span>
                      <span className="font-medium text-right">{document.issuingAuthority || "Đang cập nhật"}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-500 dark:text-slate-400">Người ký:</span>
                      <span className="font-medium text-right">{document.signer || "Đang cập nhật"}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 pt-2">
                    <button
                      onClick={handleDownload}
                      className="flex-1 flex gap-2 items-center justify-center h-9 px-3 bg-blue-600/10 text-blue-600 rounded-lg text-sm font-bold hover:bg-blue-600/20 transition-colors"
                    >
                      <span className="material-symbols-outlined text-base">download</span> Tải về
                    </button>
                    <button
                      onClick={handlePrint}
                      className="flex-1 flex gap-2 items-center justify-center h-9 px-3 bg-slate-100 dark:bg-slate-800 rounded-lg text-sm font-bold hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors"
                    >
                      <span className="material-symbols-outlined text-base">print</span> In
                    </button>
                    <button
                      onClick={handleShare}
                      className="h-9 w-9 flex items-center justify-center bg-slate-100 dark:bg-slate-800 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors"
                    >
                      <span className="material-symbols-outlined text-base">share</span>
                    </button>
                  </div>
                </div>

                {/* Mục lục */}
                <div className="rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-5">
                  <h3 className="font-bold text-lg text-slate-900 dark:text-white mb-4" style={{ fontFamily: "'Lora', serif" }}>
                    Mục lục
                  </h3>
                  <ul className="space-y-1 text-sm max-h-[calc(100vh-30rem)] overflow-y-auto">
                    {tableOfContents.length > 0 ? (
                      tableOfContents.map((item) => (
                        <li key={item.id}>
                          <a
                            className={`block py-2 px-3 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 ${
                              item.level === 0 ? 'font-bold' : `pl-${(item.level + 1) * 3}`
                            }`}
                            href={`#${item.id}`}
                          >
                            {item.title}
                          </a>
                        </li>
                      ))
                    ) : (
                      <>
                        <li>
                          <a
                            className="block py-2 px-3 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 font-bold"
                            href="#chuong-1"
                          >
                            Chương I: Quy định chung
                          </a>
                        </li>
                        <li>
                          <a className="block py-2 px-3 rounded-md hover:bg-slate-100 dark:hover:bg-slate-800 pl-6" href="#dieu-1">
                            Điều 1. Phạm vi điều chỉnh
                          </a>
                        </li>
                      </>
                    )}
                  </ul>
                </div>
              </div>
            </aside>

            {/* Content */}
            <div className="col-span-12 lg:col-span-8 xl:col-span-9">
              <div className="space-y-6">
                {/* Breadcrumbs */}
                <div className="flex flex-wrap gap-2">
                  <Link className="text-slate-500 dark:text-slate-400 text-sm font-medium hover:text-blue-600" to="/">
                    Trang chủ
                  </Link>
                  <span className="text-slate-400 dark:text-slate-500 text-sm font-medium">/</span>
                  <Link
                    className="text-slate-500 dark:text-slate-400 text-sm font-medium hover:text-blue-600"
                    to="/legal-documents"
                  >
                    Văn bản pháp luật
                  </Link>
                  <span className="text-slate-400 dark:text-slate-500 text-sm font-medium">/</span>
                  <span className="text-slate-800 dark:text-slate-200 text-sm font-medium">Chi tiết văn bản</span>
                </div>

                {/* Page Heading */}
                <div className="flex flex-col gap-3">
                  <h1
                    className="text-3xl md:text-4xl font-bold text-slate-900 dark:text-white leading-tight"
                    style={{ fontFamily: "'Lora', serif" }}
                  >
                    {document.title}
                  </h1>
                  <p className="text-slate-600 dark:text-slate-300 text-base font-normal leading-relaxed">
                    {document.summary || "Quy định chi tiết về các hành vi vi phạm, hình thức xử phạt, mức xử phạt..."}
                  </p>
                </div>

                {/* Actions Bar */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="flex items-center gap-3 p-3 rounded-lg bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
                    <div className="rounded-full bg-blue-600/10 p-2.5 text-blue-600">
                      <span className="material-symbols-outlined">visibility</span>
                    </div>
                    <div className="flex flex-col">
                      <p className="text-xs text-slate-500 dark:text-slate-400">Lượt xem</p>
                      <p className="text-sm font-bold text-slate-800 dark:text-slate-100">
                        {(document.viewCount || 0).toLocaleString()}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 p-3 rounded-lg bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
                    <div className="rounded-full bg-blue-600/10 p-2.5 text-blue-600">
                      <span className="material-symbols-outlined">calendar_month</span>
                    </div>
                    <div className="flex flex-col">
                      <p className="text-xs text-slate-500 dark:text-slate-400">Ngày ban hành</p>
                      <p className="text-sm font-bold text-slate-800 dark:text-slate-100">{formatDate(document.createdAt)}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 p-3 rounded-lg bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
                    <div className="rounded-full bg-blue-600/10 p-2.5 text-blue-600">
                      <span className="material-symbols-outlined">folder_open</span>
                    </div>
                    <div className="flex flex-col">
                      <p className="text-xs text-slate-500 dark:text-slate-400">Danh mục</p>
                      <p className="text-sm font-bold text-slate-800 dark:text-slate-100">{document.category || "Chưa phân loại"}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 p-3 rounded-lg bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
                    <div className="rounded-full bg-blue-600/10 p-2.5 text-blue-600">
                      <span className="material-symbols-outlined">verified</span>
                    </div>
                    <div className="flex flex-col">
                      <p className="text-xs text-slate-500 dark:text-slate-400">Tình trạng</p>
                      <p
                        className={`text-sm font-bold ${
                          document.status === "ACTIVE"
                            ? "text-green-600 dark:text-green-500"
                            : "text-red-600 dark:text-red-500"
                        }`}
                      >
                        {document.status === "ACTIVE" ? "Còn hiệu lực" : "Hết hiệu lực"}
                      </p>
                    </div>
                  </div>
                </div>

                {/* Tabs */}
                <div className="sticky top-[73px] z-30 bg-slate-50/80 dark:bg-slate-950/80 backdrop-blur-sm pt-2 -mx-4 px-4">
                  <div className="border-b border-slate-200 dark:border-slate-700 flex gap-6 sm:gap-8 overflow-x-auto">
                    <button
                      onClick={() => setActiveTab("content")}
                      className={`flex flex-col items-center justify-center border-b-[3px] pb-3 pt-2 ${
                        activeTab === "content"
                          ? "border-blue-600 text-blue-600"
                          : "border-transparent text-slate-500 dark:text-slate-400 hover:border-slate-300 dark:hover:border-slate-600"
                      }`}
                    >
                      <p className="text-sm font-bold whitespace-nowrap">Nội dung văn bản</p>
                    </button>
                    <button
                      onClick={() => setActiveTab("related")}
                      className={`flex flex-col items-center justify-center border-b-[3px] pb-3 pt-2 ${
                        activeTab === "related"
                          ? "border-blue-600 text-blue-600"
                          : "border-transparent text-slate-500 dark:text-slate-400 hover:border-slate-300 dark:hover:border-slate-600"
                      }`}
                    >
                      <p className="text-sm font-bold whitespace-nowrap">Văn bản liên quan</p>
                    </button>
                    <button
                      onClick={() => setActiveTab("replacement")}
                      className={`flex flex-col items-center justify-center border-b-[3px] pb-3 pt-2 ${
                        activeTab === "replacement"
                          ? "border-blue-600 text-blue-600"
                          : "border-transparent text-slate-500 dark:text-slate-400 hover:border-slate-300 dark:hover:border-slate-600"
                      }`}
                    >
                      <p className="text-sm font-bold whitespace-nowrap">Văn bản thay thế</p>
                    </button>
                    <button
                      onClick={() => setActiveTab("history")}
                      className={`flex flex-col items-center justify-center border-b-[3px] pb-3 pt-2 ${
                        activeTab === "history"
                          ? "border-blue-600 text-blue-600"
                          : "border-transparent text-slate-500 dark:text-slate-400 hover:border-slate-300 dark:hover:border-slate-600"
                      }`}
                    >
                      <p className="text-sm font-bold whitespace-nowrap">Lịch sử sửa đổi</p>
                    </button>
                  </div>
                </div>

                {/* Document Content */}
                {activeTab === "content" && (
                  <article className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 md:p-8 text-base leading-relaxed space-y-6">
                    {renderXMLContent()}
                  </article>
                )}

                {activeTab === "related" && (
                  <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 md:p-8">
                    <p className="text-center text-slate-500 dark:text-slate-400 py-8">
                      Chưa có văn bản liên quan
                    </p>
                  </div>
                )}

                {activeTab === "replacement" && (
                  <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 md:p-8">
                    <p className="text-center text-slate-500 dark:text-slate-400 py-8">
                      Chưa có văn bản thay thế
                    </p>
                  </div>
                )}

                {activeTab === "history" && (
                  <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 md:p-8">
                    <p className="text-center text-slate-500 dark:text-slate-400 py-8">
                      Chưa có lịch sử sửa đổi
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </main>
      </div>
    </Layout>
  );
}
