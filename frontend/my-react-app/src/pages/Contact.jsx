import React, { useState, useEffect, useCallback, useRef } from "react";
import { flushSync } from "react-dom";
import { useLocation, useNavigate } from "react-router-dom";
import Layout from "../components/Layout";
import { apiFetch } from "../utils/api";
import useUserProfile from "../hooks/useUserProfile";
import { useChatWebSocket } from "../hooks/useChatWebSocket";
import "./Contact.css";

export default function Contact() {
  const { user, loading: userLoading } = useUserProfile();
  const location = useLocation();
  const navigate = useNavigate();
  const [conversations, setConversations] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageInput, setMessageInput] = useState("");
  const [loading, setLoading] = useState(true);
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [openMenuId, setOpenMenuId] = useState(null);
  const [imageModalUrl, setImageModalUrl] = useState(null);
  const fileInputRef = useRef(null);
  const imageInputRef = useRef(null);
  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);
  const lastHandledLawyerId = useRef(null);
  const lastClearedConversationId = useRef(null);
  
  // Derive filteredConversations from conversations and searchQuery
  const filteredConversations = React.useMemo(() => {
    if (!searchQuery.trim()) {
      return conversations;
    }
    
    const query = searchQuery.toLowerCase();
    return conversations.filter(conv => {
      const otherUser = conv.otherUser;
      const name = otherUser?.fullName || otherUser?.name || '';
      const email = otherUser?.email || '';
      const phone = otherUser?.phoneNumber || '';
      const lastMsg = conv.lastMessage?.content || '';

      return name.toLowerCase().includes(query) ||
             email.toLowerCase().includes(query) ||
             phone.includes(query) ||
             lastMsg.toLowerCase().includes(query);
    });
  }, [conversations, searchQuery]);
  
  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMoreMessages, setHasMoreMessages] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const pageSize = 20; // Load 20 messages at a time

  // Auto scroll to bottom when new message arrives
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  // Scroll to bottom when messages change
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // WebSocket for real-time messages
  const handleNewMessage = useCallback((data) => {
    // Check if this is a message read event
    if (data.type === 'MESSAGE_READ' || data.eventType === 'READ') {
      // Update all messages in this conversation to READ status
      setMessages((prevMessages) => {
        const updated = [...prevMessages].map(msg => {
          if (msg.status !== 'READ') {
            return { ...msg, status: 'READ' };
          }
          return msg;
        });
        return updated;
      });
      return;
    }
    
    // Otherwise, it's a new message - check for duplicates
    const newMessage = data;
    setMessages((prevMessages) => {
      // Check if message already exists by messageId
      const isDuplicate = prevMessages.some(msg => msg.messageId === newMessage.messageId);
      if (isDuplicate) {
        return prevMessages;
      }
      return [...prevMessages, newMessage];
    });
    
    // Update conversation list with new last message
    setConversations(prevConvs => 
      prevConvs.map(conv => 
        conv.conversationId === newMessage.conversationId
          ? { ...conv, lastMessage: newMessage }
          : conv
      )
    );
  }, []);

  useChatWebSocket(
    selectedConversation?.conversationId,
    handleNewMessage
  );

  // Load conversations when component mounts
  useEffect(() => {
    if (!userLoading && user) {
      // Get the actual user ID - could be userId (for citizens) or lawyerId (for lawyers)
      const actualUserId = user.userId || user.lawyerId;
      
      if (actualUserId) {
        // Pass location.state to loadConversations to prevent auto-selecting
        loadConversations(location.state);
      } else {
        setLoading(false);
      }
    } else if (!userLoading && !user) {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userLoading, user]);

  // Handle navigation from FindLawyer page
  useEffect(() => {
    console.log('📍 Location state:', location.state);
    console.log('👤 User:', user);
    console.log('💬 Conversations:', conversations.length);
    console.log('⏳ Loading:', loading);
    
    // Wait for conversations to load first
    if (location.state?.selectedLawyer && user && !loading) {
      const lawyer = location.state.selectedLawyer;
      console.log('🎯 Selected lawyer:', lawyer);
      
      // Check if this is a new lawyer (not already handled)
      if (lastHandledLawyerId.current === lawyer.lawyerId) {
        console.log('⚠️ Already handled this lawyer:', lawyer.lawyerId);
        return; // Already handled this lawyer
      }
      
      // Mark this lawyer as handled
      lastHandledLawyerId.current = lawyer.lawyerId;
      console.log('✅ Handling new lawyer:', lawyer.lawyerId);
      
      // Clear location state using navigate (React Router way)
      navigate('/contact', { replace: true, state: {} });
      
      // Check if conversation already exists (now that conversations are loaded)
      const existingConv = conversations.find(conv => 
        conv.otherUser?.lawyerId === lawyer.lawyerId
      );
      
      if (existingConv) {
        // Select existing conversation
        console.log('📌 Found existing conversation:', existingConv.conversationId);
        setSelectedConversation(existingConv);
      } else {
        // Create temporary conversation only if not exists
        const tempConv = {
          conversationId: `temp_${Date.now()}`,
          otherUser: lawyer,
          lastMessage: null,
          lastMessageTime: new Date().toISOString(),
          unreadCount: 0
        };
        
        console.log('🆕 Creating temp conversation:', tempConv.conversationId);
        setConversations(prev => [tempConv, ...prev]);
        setSelectedConversation(tempConv);
        console.log('✅ Selected conversation set to:', tempConv.conversationId, tempConv.otherUser?.fullName);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.state, user, navigate, loading]);

  // Load messages when conversation is selected
  useEffect(() => {
    if (selectedConversation) {
      setMessages([]);
      setCurrentPage(0);
      setHasMoreMessages(true);
      
      // Don't load messages for temporary conversations (not yet created in backend)
      const isTempConversation = String(selectedConversation.conversationId).startsWith('temp_');
      
      if (!isTempConversation) {
        loadMessages(selectedConversation.conversationId, 0, false);
        
        // Backup polling: Check for new messages every 15 seconds
        // This is primarily a backup for when WebSocket is disconnected
        const pollingInterval = setInterval(() => {
          // Only poll if tab is visible
          if (!document.hidden) {
            checkForNewMessages();
          }
        }, 15000);
        
        return () => clearInterval(pollingInterval);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedConversation?.conversationId]);

  // Search lawyers via API with debounce
  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      setIsSearching(false);
      return;
    }

    // Search for new lawyers via API (existing conversation filtering is handled by useMemo)
    const searchTimeout = setTimeout(async () => {
      setIsSearching(true);
      try {
        const response = await apiFetch(
          `http://localhost:8080/api/search/lawyers?keyword=${encodeURIComponent(searchQuery)}&page=0&size=5`
        );
        const data = await response.json();
        
        if (data.success && data.data) {
          const lawyers = data.data.content || [];
          // Filter out lawyers who already have conversations
          const existingUserIds = new Set(
            conversations.map(conv => conv.otherUser?.userId || conv.otherUser?.lawyerId)
          );
          const newLawyers = lawyers.filter(lawyer => !existingUserIds.has(lawyer.lawyerId));
          setSearchResults(newLawyers);
        }
      } catch (error) {
        console.error("Error searching lawyers:", error);
      } finally {
        setIsSearching(false);
      }
    }, 500); // Debounce 500ms

    return () => clearTimeout(searchTimeout);
  }, [searchQuery, conversations]);

  // Handle tab visibility change - check for new messages when tab becomes active
  useEffect(() => {
    let wasHidden = false;
    
    const handleVisibilityChange = () => {
      if (document.hidden) {
        wasHidden = true;
      } else if (wasHidden && selectedConversation) {
        // Tab is active again after being hidden - check for missed messages
        // Add slight delay to avoid race with WebSocket reconnect
        setTimeout(() => checkForNewMessages(), 1000);
        wasHidden = false;
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedConversation]);

  // Auto-clear search when a conversation is selected (only once per conversation)
  useEffect(() => {
    if (selectedConversation && selectedConversation.conversationId !== lastClearedConversationId.current) {
      console.log('🧹 Auto-clearing search because conversation is selected');
      lastClearedConversationId.current = selectedConversation.conversationId;
      setSearchQuery('');
      setSearchResults([]);
    }
  }, [selectedConversation]);

  // Check for new messages (lightweight polling)
  const checkForNewMessages = async () => {
    if (!selectedConversation || messages.length === 0) return;
    
    // Don't check for temp conversations
    const isTempConversation = String(selectedConversation.conversationId).startsWith('temp_');
    if (isTempConversation) return;
    
    try {
      const lastMessage = messages[messages.length - 1];
      const response = await apiFetch(
        `http://localhost:8080/api/chat/conversations/${selectedConversation.conversationId}/messages?page=0&size=10`
      );
      const data = await response.json();
      if (data.success) {
        const latestMessages = data.data.content || [];
        const newMessages = latestMessages.filter(msg => 
          msg.messageId > lastMessage.messageId
        );
        
        if (newMessages.length > 0) {
          // Deduplicate before adding
          setMessages(prev => {
            const existingIds = new Set(prev.map(m => m.messageId));
            const uniqueNew = newMessages.filter(m => !existingIds.has(m.messageId));
            return uniqueNew.length > 0 ? [...prev, ...uniqueNew.reverse()] : prev;
          });
        }
      }
    } catch (error) {
      console.error("Error checking new messages:", error);
    }
  };

  // Create or open conversation with a lawyer
  const startConversationWithLawyer = async (lawyer) => {
    try {
      console.log('🎯 Starting conversation with lawyer:', lawyer.fullName);
      const currentUserId = user?.userId || user?.lawyerId;
      if (!currentUserId) {
        console.log('❌ No current user ID');
        return;
      }

      // Check if conversation already exists
      const existingConv = conversations.find(
        conv => (conv.otherUser?.userId === lawyer.lawyerId || conv.otherUser?.lawyerId === lawyer.lawyerId)
      );

      if (existingConv) {
        // Just select the existing conversation
        console.log('📌 Found existing conversation:', existingConv.conversationId);
        setSelectedConversation(existingConv);
        // Don't clear search - let it clear naturally or user can clear
        console.log('✅ Selected existing conversation');
        return;
      }

      // Create new conversation by sending first message
      // For now, just create a placeholder conversation object
      const newConv = {
        conversationId: `temp_${Date.now()}`, // Temporary ID
        otherUser: {
          userId: lawyer.lawyerId,
          lawyerId: lawyer.lawyerId,
          fullName: lawyer.fullName,
          avatarUrl: lawyer.avatarUrl,
          email: lawyer.email,
          phoneNumber: lawyer.phoneNumber,
          userType: 'LAWYER'
        },
        lastMessage: null,
        unreadCount: 0
      };

      console.log('🆕 Creating new temp conversation:', newConv.conversationId);
      
      // Add to conversations and select it
      setConversations(prev => [newConv, ...prev]);
      setSelectedConversation(newConv);
      
      console.log('✅ Conversation created and selected:', newConv.conversationId);

      // The actual conversation will be created when user sends first message
    } catch (error) {
      console.error("Error starting conversation:", error);
    }
  };

  // Scroll listener for infinite scroll
  useEffect(() => {
    const container = messagesContainerRef.current;
    if (!container) return;

    const handleScroll = () => {
      // Check if scrolled to top (within 100px)
      if (container.scrollTop < 100 && !loadingMore && hasMoreMessages) {
        loadMoreMessages();
      }
    };

    container.addEventListener('scroll', handleScroll);
    return () => container.removeEventListener('scroll', handleScroll);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loadingMore, hasMoreMessages, selectedConversation]);

  const loadConversations = async (locationState = null) => {
    try {
      // Get the actual user ID - could be userId or lawyerId
      const actualUserId = user?.userId || user?.lawyerId;
      
      if (!actualUserId) {
        setLoading(false);
        return;
      }
      
      const response = await apiFetch(
        `http://localhost:8080/api/chat/conversations?userId=${actualUserId}`
      );
      const data = await response.json();
      
      if (data.success) {
        // Map the flat structure to nested otherUser object
        const mappedConversations = (data.data || []).map(conv => ({
          ...conv,
          otherUser: {
            userId: conv.otherUserId,
            lawyerId: conv.otherUserId, // Could be lawyer or user
            fullName: conv.otherUserName,
            avatarUrl: conv.otherUserAvatar,
            userType: conv.otherUserType
          }
        }));
        
        // Merge with existing temp conversations (preserve temp conversations)
        setConversations(prev => {
          const tempConvs = prev.filter(c => String(c.conversationId).startsWith('temp_'));
          console.log('🔄 Merging conversations - Temp:', tempConvs.length, 'Backend:', mappedConversations.length);
          return [...tempConvs, ...mappedConversations];
        });
        
        // Auto-select first conversation only if:
        // 1. Nothing is currently selected
        // 2. Not navigating from FindLawyer (no location.state with selectedLawyer)
        // 3. There are conversations available
        const hasLocationState = locationState && locationState.selectedLawyer;
        if (!selectedConversation && !hasLocationState && mappedConversations.length > 0) {
          console.log('🔄 Auto-selecting first conversation:', mappedConversations[0].conversationId);
          setSelectedConversation(mappedConversations[0]);
        } else {
          console.log('✅ Keeping current selectedConversation:', selectedConversation?.conversationId, 'hasLocationState:', hasLocationState);
        }
      }
    } catch (error) {
      console.error("Error loading conversations:", error);
    } finally {
      setLoading(false);
    }
  };

  const loadMessages = async (conversationId, page = 0, append = false) => {
    try {
      const response = await apiFetch(
        `http://localhost:8080/api/chat/conversations/${conversationId}/messages?page=${page}&size=${pageSize}`
      );
      const data = await response.json();
      if (data.success) {
        const newMessages = data.data.content || [];
        const totalPages = data.data.totalPages || 0;
        
        // Check if there are more messages
        setHasMoreMessages(page < totalPages - 1);
        
        if (append) {
          // Prepend older messages (for infinite scroll) - with deduplication
          setMessages(prev => {
            const existingIds = new Set(prev.map(m => m.messageId));
            const uniqueNew = newMessages.filter(m => !existingIds.has(m.messageId));
            return [...uniqueNew.reverse(), ...prev];
          });
        } else {
          // Initial load - replace all messages (deduplicate just in case)
          const uniqueMessages = Array.from(
            new Map(newMessages.map(m => [m.messageId, m])).values()
          );
          setMessages(uniqueMessages.reverse());
          setCurrentPage(0);
          // Mark as read
          markAsRead(conversationId);
        }
      }
    } catch (error) {
      console.error("Error loading messages:", error);
    }
  };

  // Load more messages when scrolling up
  const loadMoreMessages = async () => {
    if (!selectedConversation || loadingMore || !hasMoreMessages) return;
    
    // Don't load more for temp conversations
    const isTempConversation = String(selectedConversation.conversationId).startsWith('temp_');
    if (isTempConversation) return;
    
    setLoadingMore(true);
    const nextPage = currentPage + 1;
    
    // Save current scroll position
    const container = messagesContainerRef.current;
    const previousScrollHeight = container?.scrollHeight || 0;
    
    await loadMessages(selectedConversation.conversationId, nextPage, true);
    
    // Restore scroll position (prevent jump to top)
    if (container) {
      const newScrollHeight = container.scrollHeight;
      container.scrollTop = newScrollHeight - previousScrollHeight;
    }
    
    setCurrentPage(nextPage);
    setLoadingMore(false);
  };

  const markAsRead = async (conversationId) => {
    // Don't mark as read for temp conversations
    const isTempConversation = String(conversationId).startsWith('temp_');
    if (isTempConversation) return;
    
    try {
      const actualUserId = user?.userId || user?.lawyerId;
      if (!actualUserId) return;
      
      await apiFetch(
        `http://localhost:8080/api/chat/conversations/${conversationId}/read?userId=${actualUserId}`,
        { method: 'PUT' }
      );
      
      // Update message status to READ in local state
      setMessages(prevMessages => 
        prevMessages.map(msg => ({
          ...msg,
          status: msg.senderId !== actualUserId ? msg.status : 'READ'
        }))
      );
      
      // Update conversation unread count
      setConversations(prevConvs =>
        prevConvs.map(conv =>
          conv.conversationId === conversationId
            ? { ...conv, unreadCount: 0 }
            : conv
        )
      );
    } catch (error) {
      console.error("Error marking as read:", error);
    }
  };

  const sendMessage = async () => {
    if ((!messageInput.trim() && !selectedFile) || !selectedConversation) return;

    // Get the actual user ID - could be userId or lawyerId
    const actualUserId = user.userId || user.lawyerId;
    
    // Clear inputs immediately for better UX
    const currentMessage = messageInput;
    const currentFile = selectedFile;
    const wasTemporaryConversation = String(selectedConversation.conversationId).startsWith('temp_');
    setMessageInput("");
    clearSelectedFile();

    try {
      let conversationId = selectedConversation.conversationId;
      
      // If this is a new conversation (temp ID), create it first
      if (wasTemporaryConversation) {
        const createConvResponse = await apiFetch(
          'http://localhost:8080/api/chat/conversations',
          {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              userId1: actualUserId,
              userId2: selectedConversation.otherUser.lawyerId,
              userType1: user.lawyerId ? "LAWYER" : "CITIZEN",
              userType2: "LAWYER"
            })
          }
        );
        
        const convData = await createConvResponse.json();
        if (convData.success && convData.data) {
          conversationId = convData.data.conversationId;
          // Update the selected conversation with real ID
          const updatedConv = { ...selectedConversation, conversationId };
          setSelectedConversation(updatedConv);
          setConversations(prev => 
            prev.map(c => c.conversationId === selectedConversation.conversationId ? updatedConv : c)
          );
        } else {
          setMessageInput(currentMessage);
          setSelectedFile(currentFile);
          alert('Không thể tạo cuộc trò chuyện!');
          return;
        }
      }

      // If there's a file, send as FormData
      if (currentFile) {
        const formData = new FormData();
        formData.append('conversationId', conversationId);
        formData.append('senderId', actualUserId);
        formData.append('senderType', user.lawyerId ? "LAWYER" : "CITIZEN");
        formData.append('content', currentMessage || 'Đã gửi file');
        formData.append('messageType', currentFile.type.startsWith('image/') ? 'IMAGE' : 'FILE');
        formData.append('file', currentFile);

        const response = await apiFetch(
          'http://localhost:8080/api/chat/messages/upload',
          {
            method: 'POST',
            body: formData
          }
        );
        
        const data = await response.json();
        if (data.success) {
          // If this was a temp conversation, reload to get real conversation from backend
          if (wasTemporaryConversation) {
            await loadConversations();
            loadMessages(conversationId, 0, false);
          }
        } else {
          setMessageInput(currentMessage);
          setSelectedFile(currentFile);
          alert('Gửi file thất bại!');
        }
      } else {
        // Send text message
        const messageData = {
          conversationId: conversationId,
          senderId: actualUserId,
          senderType: user.lawyerId ? "LAWYER" : "CITIZEN",
          content: currentMessage,
          messageType: "TEXT"
        };

        const response = await apiFetch(
          'http://localhost:8080/api/chat/messages',
          {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(messageData)
          }
        );
        
        const data = await response.json();
        if (data.success) {
          // If this was a temp conversation, reload to get real conversation from backend
          if (wasTemporaryConversation) {
            await loadConversations();
            loadMessages(conversationId, 0, false);
          }
        } else {
          setMessageInput(currentMessage);
        }
      }
    } catch (error) {
      console.error("Error sending message:", error);
      setMessageInput(currentMessage);
      setSelectedFile(currentFile);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const handleImageSelect = (e) => {
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

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        alert('Kích thước file không được vượt quá 10MB!');
        return;
      }
      setSelectedFile(file);
      setPreviewUrl(null);
    }
  };

  const clearSelectedFile = () => {
    setSelectedFile(null);
    setPreviewUrl(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
    if (imageInputRef.current) imageInputRef.current.value = '';
  };

  const toggleMenu = (messageId) => {
    setOpenMenuId(openMenuId === messageId ? null : messageId);
  };

  const downloadImage = (imageUrl, fileName) => {
    const link = document.createElement('a');
    link.href = imageUrl;
    link.download = fileName || 'image.jpg';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    setOpenMenuId(null);
  };

  const openImageModal = (imageUrl) => {
    setImageModalUrl(imageUrl);
  };

  const closeImageModal = () => {
    setImageModalUrl(null);
  };

  const deleteMessage = (messageId) => {
    // TODO: Implement delete message API
    console.log('Delete message:', messageId);
    setOpenMenuId(null);
  };

  const copyMessage = (content) => {
    navigator.clipboard.writeText(content);
    setOpenMenuId(null);
  };

  const replyMessage = (messageId) => {
    // TODO: Implement reply functionality
    console.log('Reply to message:', messageId);
    setOpenMenuId(null);
  };

  const forwardMessage = (messageId) => {
    // TODO: Implement forward functionality
    console.log('Forward message:', messageId);
    setOpenMenuId(null);
  };

  if (loading) {
    return (
      <Layout showFooter={false}>
        <div className="flex items-center justify-center h-screen">
          <p className="text-slate-500">Đang tải...</p>
        </div>
      </Layout>
    );
  }

  return (
    <Layout showFooter={false} fullWidth={true}>
      <div className="chat-layout-container flex h-full overflow-hidden">
        {/* Sidebar */}
        <aside className="w-96 flex-shrink-0 border-r border-border-light dark:border-border-dark flex flex-col bg-surface-light dark:bg-surface-dark h-full">
          <div className="p-4 border-b border-border-light dark:border-border-dark">
            <h1 className="text-2xl font-bold text-text-primary-light dark:text-text-primary-dark">Trò chuyện</h1>
            <div className="relative mt-4">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-secondary-light dark:text-text-secondary-dark">search</span>
              <input 
                className="w-full bg-background-light dark:bg-background-dark border border-border-light dark:border-border-dark rounded-full py-2 pl-10 pr-4 focus:ring-2 focus:ring-primary focus:border-primary" 
                placeholder="Tìm kiếm theo tên, email, SĐT..." 
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>
          <nav className="flex-1 overflow-y-auto">
            {/* Search results for new lawyers */}
            {searchQuery && searchResults.length > 0 && (
              <div className="border-b border-border-light dark:border-border-dark">
                <div className="p-2 bg-gray-100 dark:bg-gray-800">
                  <p className="text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase px-2">
                    Luật sư khác ({searchResults.length})
                  </p>
                </div>
                <ul>
                  {searchResults.map(lawyer => (
                    <li 
                      key={`lawyer_${lawyer.lawyerId}`}
                      className="flex items-center p-3 space-x-3 cursor-pointer hover:bg-hover-light dark:hover:bg-hover-dark transition-colors mx-1"
                      onMouseDown={(e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        startConversationWithLawyer(lawyer);
                      }}
                    >
                      <div className="relative flex-shrink-0">
                        <img 
                          alt={lawyer.fullName} 
                          className="w-14 h-14 rounded-full object-cover bg-gray-200" 
                          src={lawyer.avatarUrl ? `http://localhost:8080${lawyer.avatarUrl}` : 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="150" height="150"%3E%3Crect fill="%23ddd" width="150" height="150"/%3E%3Ctext fill="%23999" x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" font-size="48"%3E?%3C/text%3E%3C/svg%3E'} 
                          onError={(e) => { e.target.style.display = 'none'; }}
                        />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="font-medium text-text-primary-light dark:text-text-primary-dark truncate">
                            {lawyer.fullName}
                          </p>
                          <span className="text-xs px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 rounded-full">
                            Luật sư
                          </span>
                        </div>
                        <p className="text-sm text-text-secondary-light dark:text-text-secondary-dark truncate">
                          {lawyer.specializations?.join(', ') || 'Luật sư'}
                        </p>
                        {lawyer.email && (
                          <p className="text-xs text-text-secondary-light dark:text-text-secondary-dark truncate">
                            {lawyer.email}
                          </p>
                        )}
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            )}
            
            {/* Loading indicator */}
            {isSearching && (
              <div className="p-4 text-center">
                <p className="text-sm text-text-secondary-light dark:text-text-secondary-dark">
                  Đang tìm kiếm...
                </p>
              </div>
            )}

            {/* Existing conversations */}
            {searchQuery && filteredConversations.length > 0 && (
              <div className="p-2 bg-gray-100 dark:bg-gray-800">
                <p className="text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase px-2">
                  Cuộc trò chuyện ({filteredConversations.length})
                </p>
              </div>
            )}
            
            {filteredConversations.length === 0 && !searchQuery ? (
              <div className="p-4 text-center text-slate-500">
                Chưa có cuộc trò chuyện nào
              </div>
            ) : (
              <ul>
                {filteredConversations.map(conv => (
                  <li 
                    key={conv.conversationId}
                    className={`flex items-center p-3 space-x-3 cursor-pointer ${
                      selectedConversation?.conversationId === conv.conversationId 
                        ? 'bg-primary/10 dark:bg-primary/20' 
                        : 'hover:bg-hover-light dark:hover:bg-hover-dark transition-colors rounded-lg mx-1'
                    }`}
                    onClick={() => setSelectedConversation(conv)}
                  >
                    <div className="relative flex-shrink-0">
                      <img 
                        alt={`Ảnh đại diện ${conv.otherUser?.fullName || conv.otherUser?.name || 'User'}`} 
                        className="w-14 h-14 rounded-full object-cover bg-gray-200" 
                        src={conv.otherUser?.avatarUrl ? `http://localhost:8080${conv.otherUser.avatarUrl}` : 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="150" height="150"%3E%3Crect fill="%23ddd" width="150" height="150"/%3E%3Ctext fill="%23999" x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" font-size="48"%3E?%3C/text%3E%3C/svg%3E'} 
                        onError={(e) => { e.target.style.display = 'none'; }}
                      />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex justify-between items-center">
                        <p className="font-semibold truncate text-text-primary-light dark:text-text-primary-dark">
                          {conv.otherUser?.fullName || conv.otherUser?.name || 'Unknown User'}
                        </p>
                        <p className="text-xs text-text-secondary-light dark:text-text-secondary-dark flex-shrink-0">
                          {conv.lastMessage?.sentAt ? new Date(conv.lastMessage.sentAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : ''}
                        </p>
                      </div>
                      <div className="flex justify-between items-start mt-1">
                        <p className="text-sm text-text-secondary-light dark:text-text-secondary-dark truncate">
                          {conv.lastMessage?.content || 'Chưa có tin nhắn'}
                        </p>
                        {conv.unreadCount > 0 && (
                          <span className="flex-shrink-0 text-xs bg-primary text-white rounded-full px-2 py-0.5">
                            {conv.unreadCount}
                          </span>
                        )}
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </nav>
        </aside>
        {/* Main chat section */}
        <section className="flex-1 flex flex-col bg-background-light dark:bg-background-dark h-full overflow-hidden">
          {selectedConversation ? (
            <>
              <header className="flex-shrink-0 flex items-center justify-between p-4 border-b border-border-light dark:border-border-dark bg-background-light dark:bg-background-dark">
                <div className="flex items-center space-x-3">
                  <img 
                    alt={`Ảnh đại diện ${selectedConversation.otherUser?.fullName || selectedConversation.otherUser?.name || 'User'}`} 
                    className="w-12 h-12 rounded-full object-cover bg-gray-200" 
                    src={selectedConversation.otherUser?.avatarUrl ? `http://localhost:8080${selectedConversation.otherUser.avatarUrl}` : 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="150" height="150"%3E%3Crect fill="%23ddd" width="150" height="150"/%3E%3Ctext fill="%23999" x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" font-size="48"%3E?%3C/text%3E%3C/svg%3E'} 
                    onError={(e) => { e.target.style.display = 'none'; }}
                  />
                  <div>
                    <h2 className="font-bold text-lg text-text-primary-light dark:text-text-primary-dark">
                      {selectedConversation.otherUser?.fullName || selectedConversation.otherUser?.name || 'Unknown User'}
                    </h2>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      {selectedConversation.lastMessage?.sentAt 
                        ? `Hoạt động ${new Date(selectedConversation.lastMessage.sentAt).toLocaleDateString('vi-VN')}`
                        : 'Chưa có hoạt động'}
                    </p>
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <button className="p-2 rounded-full hover:bg-hover-light dark:hover:bg-hover-dark text-text-secondary-light dark:text-text-secondary-dark transition-colors">
                    <span className="material-symbols-outlined">call</span>
                  </button>
                  <button className="p-2 rounded-full hover:bg-hover-light dark:hover:bg-hover-dark text-text-secondary-light dark:text-text-secondary-dark transition-colors">
                    <span className="material-symbols-outlined">videocam</span>
                  </button>
                  <button className="p-2 rounded-full hover:bg-hover-light dark:hover:bg-hover-dark text-text-secondary-light dark:text-text-secondary-dark transition-colors">
                    <span className="material-symbols-outlined">info</span>
                  </button>
                </div>
              </header>
              <div ref={messagesContainerRef} className="flex-1 p-6 overflow-y-auto space-y-4" style={{ maxHeight: 'calc(100vh - 180px)' }}>
                {/* Loading indicator for infinite scroll */}
                {loadingMore && (
                  <div className="flex justify-center py-2">
                    <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                  </div>
                )}
                
                {messages.length === 0 ? (
                  <div className="flex items-center justify-center h-full text-slate-500">
                    Chưa có tin nhắn nào
                  </div>
                ) : (
                  messages.map((msg, index) => {
                    const currentUserId = user?.userId || user?.lawyerId;
                    const isFromMe = msg.senderId === currentUserId;
                    const isImage = msg.messageType === 'IMAGE' || (msg.fileUrl && msg.fileUrl.match(/\.(jpg|jpeg|png|gif|webp)$/i));
                    const fullImageUrl = msg.fileUrl ? `http://localhost:8080${msg.fileUrl}` : null;
                    
                    // Check if previous message is from same sender
                    const prevMsg = index > 0 ? messages[index - 1] : null;
                    const isSameSender = prevMsg && prevMsg.senderId === msg.senderId;
                    const showAvatar = !isFromMe && !isSameSender;
                    const showTime = !isSameSender;
                    
                    return (
                      <div key={msg.messageId} className={`flex items-start gap-2 ${isFromMe ? 'justify-end' : ''} group relative ${isSameSender ? 'mt-1' : 'mt-3'}`}>
                        {!isFromMe && (
                          showAvatar ? (
                            <img 
                              alt={selectedConversation.otherUser?.fullName || selectedConversation.otherUser?.name || 'User'} 
                              className="w-8 h-8 rounded-full flex-shrink-0 bg-gray-200" 
                              src={selectedConversation.otherUser?.avatarUrl ? `http://localhost:8080${selectedConversation.otherUser.avatarUrl}` : 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="150" height="150"%3E%3Crect fill="%23ddd" width="150" height="150"/%3E%3Ctext fill="%23999" x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" font-size="48"%3E?%3C/text%3E%3C/svg%3E'} 
                              onError={(e) => { e.target.style.display = 'none'; }}
                            />
                          ) : (
                            <div className="w-8 h-8 flex-shrink-0"></div>
                          )
                        )}
                        
                        {/* Three-dot menu button - outside message container */}
                        <button
                          onClick={() => toggleMenu(msg.messageId)}
                          className={`absolute top-0 ${isFromMe ? 'right-11' : 'left-11'} p-1 rounded-full hover:bg-hover-light dark:hover:bg-hover-dark text-text-secondary-light dark:text-text-secondary-dark opacity-0 group-hover:opacity-100 transition-opacity z-10`}
                          title="Tuỳ chọn"
                        >
                          <span className="material-symbols-outlined" style={{ fontSize: 18 }}>more_horiz</span>
                        </button>
                        
                        <div className={`flex flex-col items-${isFromMe ? 'end' : 'start'} gap-1`}>
                          {/* Message content */}
                          <div className={`${
                            isFromMe 
                              ? 'bg-[#0068FF] text-white rounded-2xl rounded-tr-md' 
                              : 'bg-[#E5E5EA] dark:bg-gray-700 text-gray-900 dark:text-white rounded-2xl rounded-tl-md'
                          } ${isImage && !msg.content ? 'p-1' : 'px-4 py-2'} max-w-md relative`}>
                            {msg.content && <p className="break-words">{msg.content}</p>}
                            
                            {/* Display image directly if it's an image */}
                            {isImage && fullImageUrl && (
                              <div className="mt-2 relative group/img">
                                <img 
                                  src={fullImageUrl} 
                                  alt="Hình ảnh" 
                                  className="max-w-xs rounded-lg cursor-pointer hover:opacity-90 transition-opacity"
                                  onClick={() => openImageModal(fullImageUrl)}
                                  onError={(e) => { e.target.style.display = 'none'; }}
                                />
                                {/* Quick action overlay */}
                                <div className="absolute inset-0 bg-black/0 group-hover/img:bg-black/10 rounded-lg transition-all flex items-center justify-center opacity-0 group-hover/img:opacity-100">
                                  <button 
                                    onClick={() => downloadImage(fullImageUrl, msg.fileName || 'image.jpg')}
                                    className="bg-white/90 hover:bg-white text-gray-800 rounded-full p-2 shadow-lg"
                                    title="Lưu ảnh"
                                  >
                                    <span className="material-symbols-outlined" style={{ fontSize: 20 }}>download</span>
                                  </button>
                                </div>
                              </div>
                            )}
                            
                            {/* Display file info if it's not an image */}
                            {msg.fileUrl && !isImage && (
                              <div className="mt-2 flex items-center gap-3 p-4 rounded-lg bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800 min-w-[280px]">
                                {/* File Icon based on type */}
                                {(() => {
                                  const fileName = msg.fileName || '';
                                  const ext = fileName.split('.').pop()?.toLowerCase();
                                  
                                  // PDF files
                                  if (ext === 'pdf') {
                                    return (
                                      <div className="flex-shrink-0 w-12 h-12 bg-red-500 rounded-lg flex items-center justify-center text-white font-bold text-xs shadow-sm">
                                        PDF
                                      </div>
                                    );
                                  }
                                  // Word documents
                                  if (ext === 'doc' || ext === 'docx') {
                                    return (
                                      <div className="flex-shrink-0 w-12 h-12 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold text-xs shadow-sm">
                                        DOC
                                      </div>
                                    );
                                  }
                                  // Excel files
                                  if (ext === 'xls' || ext === 'xlsx') {
                                    return (
                                      <div className="flex-shrink-0 w-12 h-12 bg-green-600 rounded-lg flex items-center justify-center text-white font-bold text-xs shadow-sm">
                                        XLS
                                      </div>
                                    );
                                  }
                                  // PowerPoint files
                                  if (ext === 'ppt' || ext === 'pptx') {
                                    return (
                                      <div className="flex-shrink-0 w-12 h-12 bg-orange-600 rounded-lg flex items-center justify-center text-white font-bold text-xs shadow-sm">
                                        PPT
                                      </div>
                                    );
                                  }
                                  // ZIP/RAR archives
                                  if (ext === 'zip' || ext === 'rar' || ext === '7z') {
                                    return (
                                      <div className="flex-shrink-0 w-12 h-12 bg-yellow-600 rounded-lg flex items-center justify-center text-white font-bold text-xs shadow-sm">
                                        ZIP
                                      </div>
                                    );
                                  }
                                  // Text files
                                  if (ext === 'txt') {
                                    return (
                                      <div className="flex-shrink-0 w-12 h-12 bg-gray-600 rounded-lg flex items-center justify-center text-white font-bold text-xs shadow-sm">
                                        TXT
                                      </div>
                                    );
                                  }
                                  // Executable files
                                  if (ext === 'exe') {
                                    return (
                                      <div className="flex-shrink-0 w-12 h-12 bg-purple-600 rounded-lg flex items-center justify-center text-white font-bold text-xs shadow-sm">
                                        EXE
                                      </div>
                                    );
                                  }
                                  // Default file icon
                                  return (
                                    <div className="flex-shrink-0 w-12 h-12 bg-gray-500 rounded-lg flex items-center justify-center text-white font-bold text-xs shadow-sm">
                                      FILE
                                    </div>
                                  );
                                })()}
                                
                                <div className="flex-1 min-w-0">
                                  <p className="font-semibold text-sm truncate text-gray-900 dark:text-gray-100">
                                    {msg.fileName || 'Document'}
                                  </p>
                                  {msg.fileSize && (
                                    <span className="text-xs text-gray-600 dark:text-gray-400 mt-0.5 block">
                                      {(msg.fileSize / 1024).toFixed(2)} KB
                                    </span>
                                  )}
                                </div>
                                
                                <a 
                                  href={fullImageUrl} 
                                  target="_blank" 
                                  rel="noopener noreferrer"
                                  download
                                  className="flex-shrink-0 p-2 rounded-full hover:bg-blue-100 dark:hover:bg-blue-800 text-blue-600 dark:text-blue-400 transition-colors"
                                  title="Tải xuống"
                                >
                                  <span className="material-symbols-outlined" style={{ fontSize: 20 }}>download</span>
                                </a>
                              </div>
                            )}
                          </div>
                          
                          {/* Time and status - only show if not same sender or last in group */}
                          {showTime && (
                            <div className="flex items-center gap-2 mt-1">
                              <span className="text-xs text-text-secondary-light dark:text-text-secondary-dark">
                                {msg.sentAt 
                                  ? new Date(msg.sentAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
                                  : '--:--'
                                }
                              </span>
                              {isFromMe && (
                                <span className="text-xs" style={{ color: msg.status === "READ" ? '#0068FF' : '#9ca3af' }}>
                                  {msg.status === "READ" ? "Đã xem" : "Đã gửi"}
                                </span>
                              )}
                            </div>
                          )}
                        </div>
                        
                        {/* Dropdown menu - outside message column */}
                        {openMenuId === msg.messageId && (
                          <div className={`absolute ${isFromMe ? 'right-11' : 'left-11'} top-8 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 py-1 z-20 min-w-[180px]`}>
                              {isImage && fullImageUrl && (
                                <button
                                  onClick={() => downloadImage(fullImageUrl, msg.fileName || 'image.jpg')}
                                  className="w-full px-4 py-2 text-left hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2 text-sm"
                                >
                                  <span className="material-symbols-outlined" style={{ fontSize: 18 }}>download</span>
                                  Lưu ảnh
                                </button>
                              )}
                              <button
                                onClick={() => replyMessage(msg.messageId)}
                                className="w-full px-4 py-2 text-left hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2 text-sm"
                              >
                                <span className="material-symbols-outlined" style={{ fontSize: 18 }}>reply</span>
                                Trả lời
                              </button>
                              <button
                                onClick={() => forwardMessage(msg.messageId)}
                                className="w-full px-4 py-2 text-left hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2 text-sm"
                              >
                                <span className="material-symbols-outlined" style={{ fontSize: 18 }}>forward</span>
                                Chuyển tiếp
                              </button>
                              {msg.content && (
                                <button
                                  onClick={() => copyMessage(msg.content)}
                                  className="w-full px-4 py-2 text-left hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2 text-sm"
                                >
                                  <span className="material-symbols-outlined" style={{ fontSize: 18 }}>content_copy</span>
                                  Sao chép
                                </button>
                              )}
                              {isFromMe && (
                                <button
                                  onClick={() => deleteMessage(msg.messageId)}
                                  className="w-full px-4 py-2 text-left hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2 text-sm text-red-500"
                                >
                                  <span className="material-symbols-outlined" style={{ fontSize: 18 }}>delete</span>
                                  Thu hồi
                                </button>
                              )}
                            </div>
                          )}
                        
                        {isFromMe && (
                          <img 
                            alt="Ảnh đại diện của bạn" 
                            className="w-8 h-8 rounded-full flex-shrink-0 bg-gray-200" 
                            src={user?.avatarUrl ? `http://localhost:8080${user.avatarUrl}` : 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="150" height="150"%3E%3Crect fill="%23ddd" width="150" height="150"/%3E%3Ctext fill="%23999" x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" font-size="48"%3E?%3C/text%3E%3C/svg%3E'} 
                            onError={(e) => { e.target.style.display = 'none'; }}
                          />
                        )}
                      </div>
                    );
                  })
                )}
                {/* Auto-scroll anchor */}
                <div ref={messagesEndRef} />
              </div>
              <footer className="flex-shrink-0 p-4 border-t border-border-light dark:border-border-dark bg-surface-light dark:bg-surface-dark">
                {/* File Preview */}
                {(selectedFile || previewUrl) && (
                  <div className="mb-3 p-3 bg-background-light dark:bg-background-dark rounded-lg border border-border-light dark:border-border-dark">
                    <div className="flex items-center gap-3">
                      {previewUrl ? (
                        <img src={previewUrl} alt="Preview" className="w-16 h-16 object-cover rounded" />
                      ) : (
                        <div className="w-16 h-16 flex items-center justify-center bg-primary/10 rounded">
                          <span className="material-symbols-outlined text-primary">description</span>
                        </div>
                      )}
                      <div className="flex-1">
                        <p className="font-medium text-sm">{selectedFile?.name}</p>
                        <p className="text-xs text-text-secondary-light dark:text-text-secondary-dark">
                          {(selectedFile?.size / 1024 / 1024).toFixed(2)} MB
                        </p>
                      </div>
                      <button 
                        onClick={clearSelectedFile}
                        className="p-2 rounded-full hover:bg-hover-light dark:hover:bg-hover-dark text-text-secondary-light dark:text-text-secondary-dark transition-colors"
                      >
                        <span className="material-symbols-outlined">close</span>
                      </button>
                    </div>
                  </div>
                )}
                <div className="flex items-center space-x-2">
                  <input 
                    ref={imageInputRef}
                    type="file" 
                    accept="image/*" 
                    className="hidden" 
                    onChange={handleImageSelect}
                  />
                  <input 
                    ref={fileInputRef}
                    type="file" 
                    className="hidden" 
                    onChange={handleFileSelect}
                  />
                  <button 
                    className="p-2 rounded-full hover:bg-hover-light dark:hover:bg-hover-dark text-primary transition-colors"
                    onClick={() => imageInputRef.current?.click()}
                    title="Gửi ảnh"
                  >
                    <span className="material-symbols-outlined">image</span>
                  </button>
                  <button 
                    className="p-2 rounded-full hover:bg-hover-light dark:hover:bg-hover-dark text-primary transition-colors"
                    onClick={() => fileInputRef.current?.click()}
                    title="Gửi file"
                  >
                    <span className="material-symbols-outlined">attach_file</span>
                  </button>
                  <div className="flex-1 relative">
                    <input 
                      className="w-full bg-surface-light dark:bg-surface-dark border-transparent rounded-full py-2.5 px-4 pr-12 focus:ring-primary focus:border-primary" 
                      placeholder="Nhập tin nhắn..." 
                      type="text"
                      value={messageInput}
                      onChange={(e) => setMessageInput(e.target.value)}
                      onKeyDown={handleKeyPress}
                    />
                    <button className="absolute right-2 top-1/2 -translate-y-1/2 p-2 rounded-full hover:bg-hover-light dark:hover:bg-hover-dark text-text-secondary-light dark:text-text-secondary-dark transition-colors">
                      <span className="material-symbols-outlined">sentiment_satisfied</span>
                    </button>
                  </div>
                  <button 
                    className="p-2 rounded-full hover:bg-hover-light dark:hover:bg-hover-dark text-primary transition-colors"
                    onClick={sendMessage}
                  >
                    <span className="material-symbols-outlined">send</span>
                  </button>
                </div>
              </footer>
            </>
          ) : (
            <div className="flex items-center justify-center h-full text-slate-500">
              Chọn một cuộc trò chuyện để bắt đầu
            </div>
          )}
        </section>
      </div>
      
      {/* Image Modal (like Zalo) */}
      {imageModalUrl && (
        <div 
          className="fixed inset-0 bg-black/90 z-50 flex items-center justify-center p-4"
          onClick={closeImageModal}
        >
          <button 
            className="absolute top-4 right-4 text-white hover:text-gray-300 z-10"
            onClick={closeImageModal}
          >
            <span className="material-symbols-outlined" style={{ fontSize: 32 }}>close</span>
          </button>
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-4 z-10">
            <button 
              onClick={(e) => {
                e.stopPropagation();
                downloadImage(imageModalUrl, 'image.jpg');
              }}
              className="bg-white/90 hover:bg-white text-gray-800 rounded-full px-4 py-2 shadow-lg flex items-center gap-2"
            >
              <span className="material-symbols-outlined" style={{ fontSize: 20 }}>download</span>
              Lưu ảnh
            </button>
          </div>
          <img 
            src={imageModalUrl} 
            alt="Xem ảnh" 
            className="max-w-full max-h-full object-contain"
            onClick={(e) => e.stopPropagation()}
          />
        </div>
      )}
      
      {/* Click outside to close menu */}
      {openMenuId && (
        <div 
          className="fixed inset-0 z-[5]" 
          onClick={() => setOpenMenuId(null)}
        />
      )}
    </Layout>
  );
}
