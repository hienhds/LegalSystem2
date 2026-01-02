import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import axiosInstance from '../utils/axiosInstance';

export default function LegalDocuments() {
  const [chuDeList, setChuDeList] = useState([]);
  const [selectedChuDe, setSelectedChuDe] = useState(null);
  const [treeData, setTreeData] = useState([]);
  const [selectedDieu, setSelectedDieu] = useState(null);
  const [loading, setLoading] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [expandedNodes, setExpandedNodes] = useState({});
  const [loadedNodes, setLoadedNodes] = useState({}); // Cache loaded data

  // Fetch danh sách chủ đề
  useEffect(() => {
    fetchChuDeList();
  }, []);

  const fetchChuDeList = async () => {
    try {
      const response = await axiosInstance.get('/api/documents/chu-de');
      if (response.data.success) {
        setChuDeList(response.data.data || []);
        // Auto select first chu de
        if (response.data.data && response.data.data.length > 0) {
          loadChuDeTree(response.data.data[0]);
        }
      }
    } catch (error) {
      console.error('Error fetching chu de list:', error);
    }
  };

  // Load ONLY de muc list (không load children)
  const loadChuDeTree = async (chuDe) => {
    try {
      setLoading(true);
      setSelectedChuDe(chuDe);
      setSelectedDieu(null);
      
      // Reset expanded nodes và loaded cache
      setExpandedNodes({});
      setLoadedNodes({});
      
      // Chỉ load de muc, không load chuong và dieu
      const deMucRes = await axiosInstance.get(`/api/documents/chu-de/${chuDe.chuDeId}`);
      if (deMucRes.data.success) {
        // FORCE empty children - bỏ tất cả nested data từ API
        const deMucList = (deMucRes.data.data || []).map(dm => ({
          id: dm.id,
          deMucId: dm.deMucId,
          text: dm.text,
          chuongList: [] // Empty - sẽ load lazy
        }));
        setTreeData(deMucList);
      }
    } catch (error) {
      console.error('Error loading tree:', error);
    } finally {
      setLoading(false);
    }
  };

  // Load chuong khi expand de muc
  const loadChuongForDeMuc = async (deMuc) => {
    const nodeKey = `demuc-${deMuc.id}`;
    
    // Nếu đã load rồi thì không load lại
    if (loadedNodes[nodeKey]) {
      return;
    }

    try {
      const chuongRes = await axiosInstance.get(`/api/documents/de-muc/${deMuc.deMucId}`);
      if (chuongRes.data.success) {
        // Hiển thị tất cả items (cả Chương lẫn Mục)
        const chuongList = (chuongRes.data.data || []).map(c => ({
          id: c.id,
          text: c.text,
          dieuList: [] // Sẽ load lazy
        }));
        
        // Update tree data
        setTreeData(prev => prev.map(dm => 
          dm.id === deMuc.id ? { ...dm, chuongList } : dm
        ));
        
        // Mark as loaded
        setLoadedNodes(prev => ({ ...prev, [nodeKey]: true }));
      }
    } catch (error) {
      console.error('Error loading chuong:', error);
    }
  };

  // Load dieu khi expand chuong (hiển thị tất cả)
  const loadMucOrDieuForChuong = async (deMucId, chuong) => {
    const nodeKey = `chuong-${chuong.id}`;
    
    // Nếu đã load rồi thì không load lại
    if (loadedNodes[nodeKey]) {
      return;
    }

    try {
      const response = await axiosInstance.get(`/api/documents/chuong/${chuong.id}`);
      if (response.data.success) {
        const items = response.data.data || [];
        
        // Hiển thị tất cả items như Điều trực tiếp
        const dieuList = items;
        
        setTreeData(prev => prev.map(dm => {
          if (dm.id === deMucId) {
            return {
              ...dm,
              chuongList: dm.chuongList.map(c => 
                c.id === chuong.id ? { ...c, dieuList } : c
              )
            };
          }
          return dm;
        }));
        
        // Mark as loaded
        setLoadedNodes(prev => ({ ...prev, [nodeKey]: true }));
      }
    } catch (error) {
      console.error('Error loading dieu:', error);
    }
  };

  const toggleNode = async (nodeId, nodeType, data) => {
    const isExpanding = !expandedNodes[nodeId];
    
    setExpandedNodes(prev => ({
      ...prev,
      [nodeId]: isExpanding
    }));

    // Lazy load khi expand
    if (isExpanding) {
      if (nodeType === 'demuc') {
        await loadChuongForDeMuc(data);
      } else if (nodeType === 'chuong') {
        await loadMucOrDieuForChuong(data.deMucId, data.chuong);
      }
    }
  };

  const handleDieuClick = (dieu) => {
    setSelectedDieu(dieu);
  };

  const filteredTree = searchKeyword
    ? treeData.filter(deMuc =>
        deMuc.text?.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        deMuc.chuongList?.some(chuong =>
          chuong.text?.toLowerCase().includes(searchKeyword.toLowerCase()) ||
          chuong.dieuList?.some(dieu =>
            dieu.tieuDe?.toLowerCase().includes(searchKeyword.toLowerCase())
          )
        )
      )
    : treeData;

  return (
    <Layout showFooter={false}>
      <div className="flex flex-1 max-w-7xl mx-auto w-full h-[calc(100vh-64px)] overflow-hidden">
        {/* Sidebar */}
        <aside className="w-80 bg-gray-100 dark:bg-slate-900 border-r border-gray-300 dark:border-slate-700 flex flex-col h-full hidden md:flex shrink-0">
          {/* Search */}
          <div className="p-4 border-b border-gray-300 dark:border-slate-700 bg-gray-200 dark:bg-slate-800">
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-500 dark:text-gray-400">
                <span className="material-symbols-outlined text-sm">search</span>
              </span>
              <input
                className="w-full py-2 pl-10 pr-4 text-sm text-gray-700 bg-white dark:bg-slate-950 border border-gray-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent dark:text-gray-200 placeholder-gray-400"
                placeholder="Nhập từ khóa để tìm kiếm..."
                type="text"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
              />
            </div>
          </div>

          {/* Tree Structure */}
          <div className="flex-1 overflow-y-auto custom-scroll p-4 text-sm">
            {loading ? (
              <div className="text-center py-8 text-gray-500">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                <p className="mt-2">Đang tải...</p>
              </div>
            ) : (
              <ul className="space-y-4">
                {/* Chủ đề selector */}
                <li className="mb-4">
                  <label className="block text-xs font-semibold text-gray-500 dark:text-gray-400 mb-2">
                    CHỌN CHỦ ĐỀ
                  </label>
                  <select
                    className="w-full py-2 px-3 text-sm bg-white dark:bg-slate-950 border border-gray-300 dark:border-slate-600 rounded-md focus:ring-2 focus:ring-blue-600"
                    value={selectedChuDe?.chuDeId || ''}
                    onChange={(e) => {
                      const chuDe = chuDeList.find(c => c.chuDeId === e.target.value);
                      if (chuDe) loadChuDeTree(chuDe);
                    }}
                  >
                    {chuDeList.map((chuDe) => (
                      <option key={chuDe.id} value={chuDe.chuDeId}>
                        {chuDe.text}
                      </option>
                    ))}
                  </select>
                </li>

                {/* Tree */}
                {filteredTree.map((deMuc) => (
                  <li key={deMuc.id}>
                    <div
                      className="flex items-start space-x-2 font-semibold text-gray-800 dark:text-gray-200 cursor-pointer hover:text-blue-600"
                      onClick={() => toggleNode(`demuc-${deMuc.id}`, 'demuc', deMuc)}
                    >
                      <span className="material-symbols-outlined text-base mt-0.5">
                        {expandedNodes[`demuc-${deMuc.id}`] ? 'folder_open' : 'folder'}
                      </span>
                      <span>{deMuc.text}</span>
                    </div>

                    {expandedNodes[`demuc-${deMuc.id}`] && (
                      <ul className="ml-2 mt-2 space-y-2 border-l border-gray-300 dark:border-slate-600 pl-4">
                        {deMuc.chuongList?.length === 0 && loadedNodes[`demuc-${deMuc.id}`] === undefined && (
                          <li className="text-gray-400 text-xs italic">Đang tải...</li>
                        )}
                        {deMuc.chuongList?.map((chuong) => (
                          <li key={chuong.id}>
                            <div
                              className="flex items-start space-x-2 font-medium text-gray-700 dark:text-gray-300 cursor-pointer hover:text-blue-600"
                              onClick={() => toggleNode(`chuong-${chuong.id}`, 'chuong', { deMucId: deMuc.id, chuong })}
                            >
                              <span className="material-symbols-outlined text-base mt-0.5 text-gray-500">
                                {expandedNodes[`chuong-${chuong.id}`] ? 'folder_open' : 'folder'}
                              </span>
                              <span>{chuong.text}</span>
                            </div>

                            {expandedNodes[`chuong-${chuong.id}`] && (
                              <ul className="ml-2 mt-2 space-y-2 border-l border-gray-300 dark:border-slate-600 pl-4">
                                {/* Loading state */}
                                {chuong.dieuList?.length === 0 && loadedNodes[`chuong-${chuong.id}`] === undefined && (
                                  <li className="text-gray-400 text-xs italic">Đang tải...</li>
                                )}
                                
                                {/* Hiển thị tất cả items */}
                                {chuong.dieuList?.map((dieu) => (
                                  <li key={dieu.id}>
                                    <a
                                      className={`flex items-center space-x-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors py-1 group cursor-pointer ${
                                        selectedDieu?.id === dieu.id
                                          ? 'text-blue-600 dark:text-blue-400 font-semibold'
                                          : 'text-gray-600 dark:text-gray-400'
                                      }`}
                                      onClick={() => handleDieuClick(dieu)}
                                    >
                                      <span className="material-symbols-outlined text-sm text-gray-400 group-hover:text-blue-600">
                                        description
                                      </span>
                                      <span className="line-clamp-1">{dieu.tieuDe || dieu.text}</span>
                                    </a>
                                  </li>
                                ))}
                              </ul>
                            )}
                          </li>
                        ))}
                      </ul>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 overflow-y-auto bg-gray-50 dark:bg-black p-4 md:p-8 relative custom-scroll">
          <h1 className="text-2xl font-bold text-center text-gray-800 dark:text-gray-100 mb-6 uppercase tracking-wide">
            Văn Bản Pháp Luật
          </h1>

          <div className="max-w-4xl mx-auto bg-white dark:bg-slate-900 shadow-lg rounded-sm border border-gray-200 dark:border-slate-700 p-8 md:p-12 min-h-[800px]">
            {selectedDieu ? (
              <article className="leading-relaxed text-gray-800 dark:text-gray-200" style={{ fontFamily: 'Roboto, sans-serif' }}>
                {/* Header */}
                <div className="text-center mb-8 space-y-2">
                  <p className="font-bold text-lg uppercase">LUẬT</p>
                  <h2 className="font-bold text-xl uppercase text-blue-700 dark:text-blue-400">
                    {selectedChuDe?.text || 'VĂN BẢN PHÁP LUẬT'}
                  </h2>
                </div>

                {/* Article Content */}
                <div className="mb-6">
                  <h4 className="font-bold text-orange-600 dark:text-orange-400 text-lg mb-4">
                    {selectedDieu.tieuDe}
                  </h4>

                  {/* Nội dung */}
                  {selectedDieu.noiDung && selectedDieu.noiDung.length > 0 && (
                    <div className="space-y-4 text-justify text-base">
                      {selectedDieu.noiDung.map((content, idx) => (
                        <p key={idx}>{content}</p>
                      ))}
                    </div>
                  )}

                  {/* Ghi chú */}
                  {selectedDieu.ghiChu && selectedDieu.ghiChu.length > 0 && (
                    <div className="mt-6 p-4 bg-blue-50 dark:bg-slate-800 rounded-md border-l-4 border-blue-500">
                      <p className="font-semibold text-sm text-blue-700 dark:text-blue-400 mb-2">
                        Ghi chú:
                      </p>
                      <div className="space-y-2 text-sm">
                        {selectedDieu.ghiChu.map((note, idx) => (
                          <p key={idx}>
                            {note.text}
                            {note.link && (
                              <a
                                href={note.link}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="ml-2 text-blue-600 hover:underline"
                              >
                                [Xem thêm]
                              </a>
                            )}
                          </p>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Chỉ dẫn */}
                  {selectedDieu.chiDan && selectedDieu.chiDan.length > 0 && (
                    <div className="mt-6 p-4 bg-amber-50 dark:bg-slate-800 rounded-md border-l-4 border-amber-500">
                      <p className="font-semibold text-sm text-amber-700 dark:text-amber-400 mb-2">
                        Chỉ dẫn liên quan:
                      </p>
                      <div className="space-y-1 text-sm">
                        {selectedDieu.chiDan.map((cd, idx) => (
                          <div key={idx} className="flex items-start space-x-2">
                            <span className="material-symbols-outlined text-xs mt-0.5 text-amber-600">
                              arrow_forward
                            </span>
                            <span className="text-blue-600 dark:text-blue-400 hover:underline cursor-pointer">
                              {cd.text || cd}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </article>
            ) : (
              <div className="text-center py-20 text-gray-500">
                <span className="material-symbols-outlined text-6xl mb-4 text-gray-400">
                  description
                </span>
                <p className="text-lg">Chọn một điều từ sidebar để xem nội dung</p>
              </div>
            )}
          </div>

          <div className="text-center text-gray-500 text-sm mt-8 pb-4">
            © 2026 LegalConnect. All rights reserved.
          </div>
        </main>

        {/* AI Chatbot Button */}
        <div className="fixed bottom-8 right-8 z-50 group">
          <div className="absolute bottom-16 right-0 bg-white dark:bg-slate-800 p-3 rounded-lg shadow-xl border border-gray-200 dark:border-slate-700 w-64 mb-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none group-hover:pointer-events-auto">
            <p className="text-sm text-gray-700 dark:text-gray-300">
              Chào bạn! Tôi là trợ lý AI pháp luật. Bạn cần giúp gì về văn bản này?
            </p>
          </div>
          <button className="bg-blue-700 hover:bg-blue-800 text-white p-3 rounded-full shadow-lg transition-transform transform hover:scale-110 flex items-center justify-center w-14 h-14 border-2 border-white dark:border-slate-600">
            <span className="material-symbols-outlined text-2xl">smart_toy</span>
          </button>
        </div>
      </div>

      <style jsx>{`
        .custom-scroll::-webkit-scrollbar {
          width: 6px;
        }
        .custom-scroll::-webkit-scrollbar-track {
          background: transparent;
        }
        .custom-scroll::-webkit-scrollbar-thumb {
          background-color: #cbd5e1;
          border-radius: 20px;
        }
        .dark .custom-scroll::-webkit-scrollbar-thumb {
          background-color: #4b5563;
        }
      `}</style>
    </Layout>
  );
}
