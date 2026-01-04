import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import axiosInstance from '../utils/axiosInstance';

export default function LegalDocuments() {
  const [chuDeList, setChuDeList] = useState([]);
  const [selectedChuDe, setSelectedChuDe] = useState(null);
  const [treeData, setTreeData] = useState([]);
  const [selectedChuong, setSelectedChuong] = useState(null);
  const [selectedDieu, setSelectedDieu] = useState(null); // Điều được chọn để scroll
  const [loading, setLoading] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [expandedNodes, setExpandedNodes] = useState({});
  const [loadedNodes, setLoadedNodes] = useState({}); // Cache loaded data
  const [chuongContent, setChuongContent] = useState(null); // Nội dung chương với tất cả điều
  const [showChiDanModal, setShowChiDanModal] = useState(false);
  const [selectedChiDan, setSelectedChiDan] = useState(null);
  const [navigationHistory, setNavigationHistory] = useState([]); // Lịch sử điều đã xem
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [showSearchResults, setShowSearchResults] = useState(false);

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
      setSelectedChuong(null);
      setChuongContent(null);
      
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

  // Load dieu khi expand chuong
  const loadDieuForChuong = async (deMucId, chuong) => {
    const nodeKey = `chuong-${chuong.id}`;
    
    // Nếu đã load rồi thì không load lại
    if (loadedNodes[nodeKey]) {
      return;
    }

    try {
      const response = await axiosInstance.get(`/api/documents/chuong/${chuong.id}`);
      if (response.data.success) {
        const dieuList = response.data.data || [];
        
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

  // Load tất cả điều khi click vào chương
  const handleChuongClick = async (chuong) => {
    try {
      setLoading(true);
      setSelectedChuong(chuong);
      setSelectedDieu(null); // Reset selected dieu
      
      const response = await axiosInstance.get(`/api/documents/chuong/${chuong.id}`);
      if (response.data.success) {
        setChuongContent({
          chuong: chuong,
          dieuList: response.data.data || []
        });
        
        // Scroll to top của main content
        setTimeout(() => {
          const mainContent = document.querySelector('main.custom-scroll');
          if (mainContent) {
            mainContent.scrollTo({ top: 0, behavior: 'smooth' });
          }
        }, 100);
      }
    } catch (error) {
      console.error('Error loading chuong content:', error);
    } finally {
      setLoading(false);
    }
  };

  // Click vào điều → load chương + scroll đến điều đó
  const handleDieuClick = async (dieu, chuong) => {
    // Nếu đang ở cùng chương, chỉ scroll không cần load lại
    if (selectedChuong?.id === chuong.id && chuongContent) {
      setSelectedDieu(dieu);
      
      // Scroll mượt không có loading
      setTimeout(() => {
        const element = document.getElementById(`dieu-${dieu.id}`);
        if (element) {
          const mainContent = document.querySelector('main.custom-scroll');
          if (mainContent) {
            const elementTop = element.offsetTop;
            const offset = 180; // Chiều cao header + margin
            mainContent.scrollTo({ top: elementTop - offset, behavior: 'smooth' });
          }
        }
      }, 50);
      return;
    }
    
    // Nếu chương khác, mới load
    try {
      setLoading(true);
      setSelectedChuong(chuong);
      setSelectedDieu(dieu);
      
      const response = await axiosInstance.get(`/api/documents/chuong/${chuong.id}`);
      if (response.data.success) {
        setChuongContent({
          chuong: chuong,
          dieuList: response.data.data || []
        });
        
        // Scroll to điều sau khi render với offset
        setTimeout(() => {
          const element = document.getElementById(`dieu-${dieu.id}`);
          if (element) {
            const mainContent = document.querySelector('main.custom-scroll');
            if (mainContent) {
              const elementTop = element.offsetTop;
              const offset = 180; // Chiều cao header + margin
              mainContent.scrollTo({ top: elementTop - offset, behavior: 'smooth' });
            }
          }
        }, 100);
      }
    } catch (error) {
      console.error('Error loading dieu:', error);
    } finally {
      setLoading(false);
    }
  };

  // Click vào chỉ dẫn → tìm và hiển thị văn bản liên quan
  const handleChiDanClick = async (chiDan) => {
    try {
      setLoading(true);
      
      // Lưu vị trí hiện tại vào lịch sử trước khi chuyển
      if (selectedChuDe && selectedChuong && selectedDieu && chuongContent) {
        const currentState = {
          chuDe: selectedChuDe,
          chuong: selectedChuong,
          dieu: selectedDieu,
          chuongContent: chuongContent,
          treeData: treeData,
          expandedNodes: expandedNodes,
          loadedNodes: loadedNodes
        };
        setNavigationHistory(prev => [...prev, currentState]);
      }
      
      // Call API resolve chỉ dẫn
      const response = await axiosInstance.get(`/api/documents/chi-dan/resolve`, {
        params: { text: chiDan.text }
      });
      
      if (response.data.success) {
        const { chuDeId, deMucId, chuongId, dieuId } = response.data.data;
        console.log('Resolved chi dan:', { chuDeId, deMucId, chuongId, dieuId });
        
        // 1. Load chủ đề nếu khác
        const targetChuDe = chuDeList.find(cd => cd.id === chuDeId);
        console.log('Target chu de:', targetChuDe);
        
        if (targetChuDe && targetChuDe.id !== selectedChuDe?.id) {
          console.log('Switching to new chu de...');
          await loadChuDeTree(targetChuDe);
        }
        
        // 2. Load de muc từ API (không dùng state)
        const deMucResponse = await axiosInstance.get(`/api/documents/chu-de/${targetChuDe.chuDeId}`);
        const deMucList = deMucResponse.data.data || [];
        const targetDeMuc = deMucList.find(dm => dm.id === deMucId);
        
        console.log('Target de muc:', targetDeMuc);
        
        if (targetDeMuc) {
          // 3. Load chuong từ API
          const chuongResponse = await axiosInstance.get(`/api/documents/de-muc/${targetDeMuc.deMucId}`);
          const chuongList = chuongResponse.data.data || [];
          const targetChuong = chuongList.find(c => c.id === chuongId);
          
          console.log('Target chuong:', targetChuong);
          
          if (targetChuong) {
            // 4. Load nội dung chương và điều
            const dieuResponse = await axiosInstance.get(`/api/documents/chuong/${chuongId}`);
            if (dieuResponse.data.success) {
              const dieuList = dieuResponse.data.data || [];
              const targetDieu = dieuList.find(d => d.id === dieuId);
              
              console.log('Target dieu:', targetDieu);
              
              // 5. Update tree data với đầy đủ thông tin
              setTreeData(prev => prev.map(dm => {
                if (dm.id === targetDeMuc.id) {
                  return {
                    ...dm,
                    chuongList: chuongList.map(c => ({
                      id: c.id,
                      text: c.text,
                      dieuList: c.id === targetChuong.id ? dieuList : []
                    }))
                  };
                }
                return dm;
              }));
              
              // 6. Expand nodes
              setExpandedNodes(prev => ({ 
                ...prev, 
                [`demuc-${targetDeMuc.id}`]: true,
                [`chuong-${chuongId}`]: true 
              }));
              
              // 7. Mark as loaded
              setLoadedNodes(prev => ({
                ...prev,
                [`demuc-${targetDeMuc.id}`]: true,
                [`chuong-${chuongId}`]: true
              }));
              
              // 8. Set selected states
              setSelectedChuong(targetChuong);
              setSelectedDieu(targetDieu);
              setChuongContent({
                chuong: targetChuong,
                dieuList: dieuList
              });
              
              // 9. Scroll sidebar đến điều trong tree
              setTimeout(() => {
                const treeElement = document.getElementById(`tree-dieu-${dieuId}`);
                if (treeElement) {
                  const sidebar = treeElement.closest('.custom-scroll');
                  if (sidebar) {
                    const elementTop = treeElement.offsetTop;
                    const sidebarHeight = sidebar.clientHeight;
                    const scrollPosition = elementTop - sidebarHeight / 2 + treeElement.offsetHeight / 2;
                    sidebar.scrollTo({ top: scrollPosition, behavior: 'smooth' });
                  }
                }
              }, 400);
              
              // 10. Scroll main content đến điều
              setTimeout(() => {
                const element = document.getElementById(`dieu-${dieuId}`);
                console.log('Scrolling to element:', element);
                if (element) {
                  const mainContent = document.querySelector('main.custom-scroll');
                  if (mainContent) {
                    const elementTop = element.offsetTop;
                    const offset = 180; // Chiều cao header + margin
                    mainContent.scrollTo({ top: elementTop - offset, behavior: 'smooth' });
                  }
                }
              }, 500);
            }
          } else {
            console.error('Could not find target chuong');
            alert('Không tìm thấy chương trong hệ thống');
          }
        } else {
          console.error('Could not find target de muc');
          alert('Không tìm thấy đề mục trong hệ thống');
        }
      }
    } catch (error) {
      console.error('Error resolving chi dan:', error);
      alert('Lỗi: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  // Quay lại điều trước đó
  const handleGoBack = () => {
    if (navigationHistory.length === 0) return;
    
    // Lấy state cuối cùng từ history
    const previousState = navigationHistory[navigationHistory.length - 1];
    
    // Restore state
    setSelectedChuDe(previousState.chuDe);
    setSelectedChuong(previousState.chuong);
    setSelectedDieu(previousState.dieu);
    setChuongContent(previousState.chuongContent);
    setTreeData(previousState.treeData);
    setExpandedNodes(previousState.expandedNodes);
    setLoadedNodes(previousState.loadedNodes);
    
    // Remove từ history
    setNavigationHistory(prev => prev.slice(0, -1));
    
    // Scroll to điều
    setTimeout(() => {
      const element = document.getElementById(`dieu-${previousState.dieu.id}`);
      if (element) {
        const mainContent = document.querySelector('main.custom-scroll');
        if (mainContent) {
          const elementTop = element.offsetTop;
          const offset = 180;
          mainContent.scrollTo({ top: elementTop - offset, behavior: 'smooth' });
        }
      }
    }, 100);
  };

  // Search full-text
  const handleSearch = async (keyword) => {
    setSearchKeyword(keyword);
    
    if (!keyword || keyword.trim().length < 2) {
      setSearchResults([]);
      setShowSearchResults(false);
      return;
    }
    
    try {
      setIsSearching(true);
      const response = await axiosInstance.get('/api/documents/search', {
        params: { keyword: keyword.trim() }
      });
      
      if (response.data.success) {
        setSearchResults(response.data.data || []);
        setShowSearchResults(true);
      }
    } catch (error) {
      console.error('Error searching:', error);
      setSearchResults([]);
    } finally {
      setIsSearching(false);
    }
  };

  // Click vào kết quả search
  const handleSearchResultClick = async (result) => {
    try {
      setLoading(true);
      setShowSearchResults(false);
      setSearchKeyword(''); // Clear search
      
      // 1. Load chủ đề nếu khác
      const targetChuDe = chuDeList.find(cd => cd.chuDeId === result.chuDeId);
      if (targetChuDe && targetChuDe.id !== selectedChuDe?.id) {
        await loadChuDeTree(targetChuDe);
      }
      
      // 2. Load đề mục từ API để có đúng id
      const deMucResponse = await axiosInstance.get(`/api/documents/chu-de/${targetChuDe.chuDeId}`);
      const deMucList = deMucResponse.data.data || [];
      const targetDeMuc = deMucList.find(dm => dm.deMucId === result.deMucId);
      
      if (!targetDeMuc) {
        alert('Không tìm thấy đề mục');
        return;
      }
      
      // 3. Load danh sách chương từ đề mục
      const chuongResponse = await axiosInstance.get(`/api/documents/de-muc/${targetDeMuc.deMucId}`);
      const chuongList = chuongResponse.data.data || [];
      const targetChuong = chuongList.find(c => c.id === result.chuongId);
      
      if (!targetChuong) {
        alert('Không tìm thấy chương');
        return;
      }
      
      // 4. Load danh sách điều từ chương
      const dieuResponse = await axiosInstance.get(`/api/documents/chuong/${result.chuongId}`);
      if (dieuResponse.data.success) {
        const dieuList = dieuResponse.data.data || [];
        const targetDieu = dieuList.find(d => d.id === result.dieuId);
        
        // 5. Update tree data với đầy đủ thông tin
        setTreeData(prev => prev.map(dm => {
          if (dm.id === targetDeMuc.id) {
            return {
              ...dm,
              chuongList: chuongList.map(c => ({
                id: c.id,
                text: c.text,
                dieuList: c.id === targetChuong.id ? dieuList : []
              }))
            };
          }
          return dm;
        }));
        
        // 6. Expand nodes
        setExpandedNodes(prev => ({ 
          ...prev, 
          [`demuc-${targetDeMuc.id}`]: true,
          [`chuong-${result.chuongId}`]: true 
        }));
        
        // 7. Mark as loaded
        setLoadedNodes(prev => ({
          ...prev,
          [`demuc-${targetDeMuc.id}`]: true,
          [`chuong-${result.chuongId}`]: true
        }));
        
        // 8. Set selected states
        setSelectedChuong(targetChuong);
        setSelectedDieu(targetDieu);
        setChuongContent({
          chuong: targetChuong,
          dieuList: dieuList
        });
        
        // 9. Scroll sidebar đến điều trong tree
        setTimeout(() => {
          const treeElement = document.getElementById(`tree-dieu-${result.dieuId}`);
          if (treeElement) {
            const sidebar = treeElement.closest('.custom-scroll');
            if (sidebar) {
              const elementTop = treeElement.offsetTop;
              const sidebarHeight = sidebar.clientHeight;
              const scrollPosition = elementTop - sidebarHeight / 2 + treeElement.offsetHeight / 2;
              sidebar.scrollTo({ top: scrollPosition, behavior: 'smooth' });
            }
          }
        }, 400);
        
        // 10. Scroll main content đến điều
        setTimeout(() => {
          const element = document.getElementById(`dieu-${result.dieuId}`);
          if (element) {
            const mainContent = document.querySelector('main.custom-scroll');
            if (mainContent) {
              const elementTop = element.offsetTop;
              const offset = 180;
              mainContent.scrollTo({ top: elementTop - offset, behavior: 'smooth' });
            }
          }
        }, 500);
      }
    } catch (error) {
      console.error('Error navigating to search result:', error);
      alert('Lỗi khi điều hướng đến kết quả tìm kiếm');
    } finally {
      setLoading(false);
    }
  };

  const toggleNode = async (nodeId, nodeType, data) => {
    const isExpanding = !expandedNodes[nodeId];
    
    setExpandedNodes(prev => ({
      ...prev,
      [nodeId]: isExpanding
    }));

    // Scroll đề mục lên đầu sidebar khi expand
    if (isExpanding && nodeType === 'demuc') {
      setTimeout(() => {
        const element = document.getElementById(`demuc-${data.id}`);
        const sidebar = element?.closest('.custom-scroll');
        if (element && sidebar) {
          const elementTop = element.offsetTop;
          const sidebarTop = sidebar.scrollTop;
          const sidebarHeight = sidebar.clientHeight;
          
          // Chỉ scroll nếu element ở ngoài viewport, giữ ở vị trí thoải mái
          if (elementTop < sidebarTop || elementTop > sidebarTop + sidebarHeight) {
            sidebar.scrollTo({ top: elementTop - 100, behavior: 'smooth' });
          }
        }
      }, 100);
    }

    // Lazy load khi expand
    if (isExpanding) {
      if (nodeType === 'demuc') {
        await loadChuongForDeMuc(data);
      } else if (nodeType === 'chuong') {
        // Load điều vào tree
        await loadDieuForChuong(data.deMucId, data.chuong);
        // Load content hiển thị luôn
        await handleChuongClick(data.chuong);
      }
    }
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
      <div className="flex flex-1 w-full h-[calc(100vh-64px)] overflow-hidden">
        {/* Sidebar */}
        <aside className="w-80 bg-gray-100 dark:bg-slate-900 border-r border-gray-300 dark:border-slate-700 flex flex-col h-full hidden md:flex shrink-0">
          {/* Search */}
          <div className="p-4 border-b border-gray-300 dark:border-slate-700 bg-gray-200 dark:bg-slate-800">
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-500 dark:text-gray-400">
                <span className="material-symbols-outlined text-sm">search</span>
              </span>
              <input
                className="w-full py-2 pl-10 pr-10 text-sm text-gray-700 bg-white dark:bg-slate-950 border border-gray-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent dark:text-gray-200 placeholder-gray-400"
                placeholder="Tìm kiếm điều luật..."
                type="text"
                value={searchKeyword}
                onChange={(e) => handleSearch(e.target.value)}
              />
              {isSearching && (
                <span className="absolute inset-y-0 right-0 flex items-center pr-3">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                </span>
              )}
              {searchKeyword && !isSearching && (
                <button
                  onClick={() => {
                    setSearchKeyword('');
                    setSearchResults([]);
                    setShowSearchResults(false);
                  }}
                  className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-600"
                >
                  <span className="material-symbols-outlined text-sm">close</span>
                </button>
              )}
            </div>
            
            {/* Search Results Dropdown */}
            {showSearchResults && searchResults.length > 0 && (
              <div className="absolute left-4 right-4 mt-2 bg-white dark:bg-slate-900 border border-gray-300 dark:border-slate-600 rounded-lg shadow-xl max-h-96 overflow-y-auto z-50">
                <div className="p-2">
                  <div className="text-xs text-gray-500 dark:text-gray-400 px-2 py-1 font-semibold">
                    Tìm thấy {searchResults.length} kết quả
                  </div>
                  {searchResults.map((result, index) => (
                    <div
                      key={index}
                      onClick={() => handleSearchResultClick(result)}
                      className="p-3 hover:bg-blue-50 dark:hover:bg-slate-800 cursor-pointer rounded-md transition-colors border-b border-gray-100 dark:border-slate-700 last:border-0"
                    >
                      <div className="flex items-start space-x-2">
                        <span className="material-symbols-outlined text-blue-600 text-sm mt-0.5">description</span>
                        <div className="flex-1 min-w-0">
                          <p className="font-semibold text-sm text-gray-900 dark:text-gray-100 truncate">
                            {result.tieuDe}
                          </p>
                          <p className="text-xs text-gray-600 dark:text-gray-400 mt-1 line-clamp-2">
                            {result.highlightedText}
                          </p>
                          <div className="flex items-center space-x-2 mt-1 text-xs text-gray-500 dark:text-gray-500">
                            <span>{result.chuDeText}</span>
                            <span>•</span>
                            <span>{result.chuongText}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
            
            {showSearchResults && searchResults.length === 0 && searchKeyword && !isSearching && (
              <div className="absolute left-4 right-4 mt-2 bg-white dark:bg-slate-900 border border-gray-300 dark:border-slate-600 rounded-lg shadow-xl p-4 z-50">
                <div className="text-center text-gray-500 dark:text-gray-400">
                  <span className="material-symbols-outlined text-4xl mb-2">search_off</span>
                  <p className="text-sm">Không tìm thấy kết quả cho "{searchKeyword}"</p>
                </div>
              </div>
            )}
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
                  <li key={deMuc.id} id={`demuc-${deMuc.id}`}>
                    <div
                      className="flex items-start space-x-2 font-semibold text-gray-800 dark:text-gray-200 cursor-pointer hover:text-blue-600 transition-colors"
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
                                {chuong.dieuList?.length === 0 && loadedNodes[`chuong-${chuong.id}`] === undefined && (
                                  <li className="text-gray-400 text-xs italic">Đang tải...</li>
                                )}
                                
                                {chuong.dieuList?.map((dieu) => (
                                  <li key={dieu.id} id={`tree-dieu-${dieu.id}`}>
                                    <a
                                      className={`flex items-center space-x-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors py-1 group cursor-pointer ${
                                        selectedDieu?.id === dieu.id
                                          ? 'text-blue-600 dark:text-blue-400 font-semibold'
                                          : 'text-gray-600 dark:text-gray-400'
                                      }`}
                                      onClick={() => handleDieuClick(dieu, chuong)}
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
        <main className="flex-1 overflow-y-auto bg-gray-50 dark:bg-black relative custom-scroll">
          <div className="bg-white dark:bg-slate-900 shadow-lg border-b border-gray-200 dark:border-slate-700 min-h-screen">
            {chuongContent ? (
              <article className="leading-relaxed text-gray-800 dark:text-gray-200" style={{ fontFamily: 'Roboto, sans-serif' }}>
                {/* Sticky Header */}
                <div className="sticky top-0 bg-white dark:bg-slate-900 shadow-md z-30 border-b border-gray-200 dark:border-slate-700 px-6 py-3 relative">
                  {/* Nút Quay lại - compact ở góc trái */}
                  {navigationHistory.length > 0 && (
                    <button
                      onClick={handleGoBack}
                      className="absolute left-4 top-1/2 -translate-y-1/2 flex items-center space-x-1 px-2 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-md transition-all duration-200 shadow-sm hover:shadow text-xs font-medium group"
                      title="Quay lại điều trước"
                    >
                      <span className="material-symbols-outlined text-base">arrow_back</span>
                      <span className="hidden group-hover:inline-block">Quay lại</span>
                    </button>
                  )}
                  
                  <div className="text-center space-y-1">
                    <p className="font-bold text-sm uppercase text-gray-600 dark:text-gray-400">LUẬT</p>
                    <h2 className="font-bold text-lg uppercase text-blue-700 dark:text-blue-400">
                      {selectedChuDe?.text || 'HÔN NHÂN VÀ GIA ĐÌNH'}
                    </h2>
                    <p className="text-xs text-gray-500 italic">
                      (Căn cứ theo Luật ban hành Luật pháp điển về các chủ đề)
                    </p>
                    
                    {/* Đề mục và Chương - nằm trong header cố định */}
                    <div className="mt-2 pt-2 border-t border-gray-200 dark:border-slate-700">
                      {/* Tìm đề mục từ tree data */}
                      {(() => {
                        const deMuc = treeData.find(dm => 
                          dm.chuongList?.some(c => c.id === chuongContent.chuong.id)
                        );
                        return deMuc ? (
                          <div className="mb-1">
                            <p className="text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase">
                              {deMuc.text}
                            </p>
                          </div>
                        ) : null;
                      })()}
                      
                      <h3 className="font-bold text-sm uppercase text-gray-900 dark:text-gray-100">
                        {chuongContent.chuong.text}
                      </h3>
                    </div>
                  </div>
                </div>

                {/* Content Area với padding */}
                <div className="px-8 py-6">

                {/* Danh sách các Điều */}
                <div className="space-y-8">
                  {chuongContent.dieuList.map((dieu, index) => (
                    <div 
                      key={dieu.id || index} 
                      id={`dieu-${dieu.id}`}
                      className={`border-b border-gray-200 dark:border-slate-700 pb-6 last:border-0 scroll-mt-[200px] transition-all duration-500 ease-in-out ${
                        selectedDieu?.id === dieu.id ? 'bg-blue-50 dark:bg-slate-800 -mx-4 px-4 py-4 rounded-lg shadow-sm' : ''
                      }`}
                    >
                      {/* Tiêu đề Điều */}
                      <h4 className="font-bold text-orange-600 dark:text-orange-400 text-base mb-4 relative group">
                        {dieu.tieuDe}
                        
                        {/* Tooltip Ghi Chú - Hiện khi hover */}
                        {dieu.ghiChu && dieu.ghiChu.length > 0 && (
                          <>
                            <span className="ml-2 text-xs text-blue-500 cursor-help">ⓘ</span>
                            <div className="absolute left-0 top-full mt-2 w-96 max-w-md bg-blue-50 dark:bg-slate-800 border border-blue-200 dark:border-blue-700 rounded-lg shadow-xl p-4 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-300 z-50">
                              <p className="font-semibold text-sm text-blue-700 dark:text-blue-400 mb-2">
                                📝 Ghi chú:
                              </p>
                              <div className="space-y-2 text-sm text-gray-700 dark:text-gray-300">
                                {dieu.ghiChu.map((note, idx) => (
                                  <div key={idx}>
                                    <p>{note.text}</p>
                                    {note.link && (
                                      <a
                                        href={note.link}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="text-blue-600 hover:underline text-xs mt-1 inline-block"
                                      >
                                        🔗 Xem văn bản trực tiếp
                                      </a>
                                    )}
                                  </div>
                                ))}
                              </div>
                            </div>
                          </>
                        )}
                      </h4>

                      {/* Nội dung */}
                      {dieu.noiDung && dieu.noiDung.length > 0 && (
                        <div className="space-y-3 text-justify text-base leading-7">
                          {dieu.noiDung.map((content, idx) => (
                            <p key={idx} className="indent-8">{content}</p>
                          ))}
                        </div>
                      )}

                      {/* Chỉ dẫn */}
                      {dieu.chiDan && dieu.chiDan.length > 0 && (
                        <div className="mt-4 p-3 bg-amber-50 dark:bg-slate-800 rounded-md border-l-4 border-amber-500">
                          <p className="font-semibold text-xs text-amber-700 dark:text-amber-400 mb-2">
                            📌 Chỉ dẫn liên quan:
                          </p>
                          <div className="space-y-1 text-sm">
                            {dieu.chiDan.map((cd, idx) => (
                              <div key={idx} className="flex items-start space-x-2">
                                <span className="text-amber-600">•</span>
                                <span 
                                  className="text-blue-600 dark:text-blue-400 hover:underline cursor-pointer transition-colors hover:text-blue-800 dark:hover:text-blue-300"
                                  onClick={() => handleChiDanClick(cd)}
                                >
                                  {cd.text || cd}
                                </span>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
                </div>
              </article>
            ) : (
              <div className="text-center py-20 text-gray-500 px-8">
                <span className="material-symbols-outlined text-6xl mb-4 text-gray-400">
                  description
                </span>
                <p className="text-lg">Chọn một chương từ sidebar để xem nội dung</p>
              </div>
            )}
          </div>

          <div className="text-center text-gray-500 text-sm py-8">
            © 2026 LegalConnect. All rights reserved.
          </div>
        </main>

        {/* Modal Chỉ Dẫn */}
        {showChiDanModal && selectedChiDan && (
          <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4" onClick={() => setShowChiDanModal(false)}>
            <div className="bg-white dark:bg-slate-900 rounded-lg shadow-2xl max-w-2xl w-full max-h-[80vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
              {/* Header */}
              <div className="sticky top-0 bg-white dark:bg-slate-900 border-b border-gray-200 dark:border-slate-700 p-4 flex items-center justify-between">
                <h3 className="text-lg font-bold text-gray-900 dark:text-gray-100">
                  📄 Văn Bản Liên Quan
                </h3>
                <button
                  onClick={() => setShowChiDanModal(false)}
                  className="text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 transition-colors"
                >
                  <span className="material-symbols-outlined">close</span>
                </button>
              </div>

              {/* Content */}
              <div className="p-6 space-y-4">
                <div className="bg-blue-50 dark:bg-slate-800 rounded-lg p-4 border border-blue-200 dark:border-blue-700">
                  <h4 className="font-semibold text-blue-900 dark:text-blue-300 mb-2">
                    {selectedChiDan.text}
                  </h4>
                  {selectedChiDan.mapc && (
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      <span className="font-medium">Mã pháp điển:</span> {selectedChiDan.mapc}
                    </p>
                  )}
                </div>

                <div className="border-t border-gray-200 dark:border-slate-700 pt-4">
                  <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                    Văn bản này có liên quan đến điều luật bạn đang xem. Bạn có thể:
                  </p>
                  
                  <div className="space-y-2">
                    <button
                      className="w-full flex items-center justify-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-3 rounded-lg transition-colors"
                      onClick={() => {
                        // TODO: Implement search by mapc
                        if (selectedChiDan.mapc) {
                          setSearchKeyword(selectedChiDan.text);
                          setShowChiDanModal(false);
                        }
                      }}
                    >
                      <span className="material-symbols-outlined">search</span>
                      <span>Tìm kiếm văn bản này</span>
                    </button>
                    
                    <button
                      className="w-full flex items-center justify-center space-x-2 bg-gray-100 dark:bg-slate-800 hover:bg-gray-200 dark:hover:bg-slate-700 text-gray-700 dark:text-gray-300 px-4 py-3 rounded-lg transition-colors"
                      onClick={() => {
                        // Copy to clipboard
                        navigator.clipboard.writeText(selectedChiDan.text);
                        alert('Đã copy tên văn bản!');
                      }}
                    >
                      <span className="material-symbols-outlined">content_copy</span>
                      <span>Sao chép tên văn bản</span>
                    </button>
                  </div>
                </div>

                <div className="bg-amber-50 dark:bg-slate-800 rounded-lg p-4 border border-amber-200 dark:border-amber-700">
                  <p className="text-xs text-amber-700 dark:text-amber-400">
                    💡 <strong>Gợi ý:</strong> Sử dụng chức năng tìm kiếm ở sidebar để tìm văn bản liên quan này trong hệ thống.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

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
        .custom-scroll {
          scroll-behavior: smooth;
        }
        .custom-scroll::-webkit-scrollbar {
          width: 6px;
        }
        .custom-scroll::-webkit-scrollbar-track {
          background: transparent;
        }
        .custom-scroll::-webkit-scrollbar-thumb {
          background-color: #cbd5e1;
          border-radius: 20px;
          transition: background-color 0.3s ease;
        }
        .custom-scroll::-webkit-scrollbar-thumb:hover {
          background-color: #94a3b8;
        }
        .dark .custom-scroll::-webkit-scrollbar-thumb {
          background-color: #4b5563;
        }
        .dark .custom-scroll::-webkit-scrollbar-thumb:hover {
          background-color: #6b7280;
        }
      `}</style>
    </Layout>
  );
}
