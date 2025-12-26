import { useEffect, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useChatWebSocket = (conversationId, onMessageReceived) => {
  const clientRef = useRef(null);
  const isConnectedRef = useRef(false);
  const subscriptionRef = useRef(null);
  const readSubscriptionRef = useRef(null);
  const onMessageReceivedRef = useRef(onMessageReceived);

  // Keep callback ref updated
  useEffect(() => {
    onMessageReceivedRef.current = onMessageReceived;
  }, [onMessageReceived]);

  const connect = useCallback(() => {
    if (clientRef.current?.active) {
      return;
    }

    const socket = new SockJS('http://localhost:8080/chat');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000, // Auto-reconnect after 5 seconds
      heartbeatIncoming: 10000, // Expect heartbeat every 10s
      heartbeatOutgoing: 10000, // Send heartbeat every 10s
      onConnect: () => {
        isConnectedRef.current = true;
        console.log('WebSocket connected');
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        isConnectedRef.current = false;
      },
      onWebSocketClose: () => {
        isConnectedRef.current = false;
        console.log('WebSocket closed - will reconnect');
      },
      onWebSocketError: (error) => {
        console.error('WebSocket error:', error);
      }
    });

    stompClient.activate();
    clientRef.current = stompClient;
  }, []);

  // Subscribe to conversation when conversationId changes
  useEffect(() => {
    if (!conversationId || !clientRef.current) {
      return;
    }

    // Don't subscribe to temp conversations
    const isTempConversation = String(conversationId).startsWith('temp_');
    if (isTempConversation) {
      return;
    }

    // Unsubscribe previous conversation
    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe();
      subscriptionRef.current = null;
    }
    if (readSubscriptionRef.current) {
      readSubscriptionRef.current.unsubscribe();
      readSubscriptionRef.current = null;
    }

    // Wait for connection with timeout
    let attempts = 0;
    const maxAttempts = 50;
    
    const subscribeWhenReady = () => {
      attempts++;
      
      if (isConnectedRef.current && clientRef.current && clientRef.current.connected) {
        try {
          // Subscribe to new messages
          subscriptionRef.current = clientRef.current.subscribe(
            `/topic/conversations/${conversationId}`,
            (message) => {
              try {
                const receivedMessage = JSON.parse(message.body);
                if (onMessageReceivedRef.current) {
                  onMessageReceivedRef.current(receivedMessage);
                }
              } catch (error) {
                console.error('Error parsing message:', error);
              }
            }
          );
          
          // Subscribe to read status updates
          readSubscriptionRef.current = clientRef.current.subscribe(
            `/topic/conversations/${conversationId}/read`,
            (message) => {
              try {
                if (onMessageReceivedRef.current) {
                  onMessageReceivedRef.current({ 
                    type: 'MESSAGE_READ',
                    conversationId: conversationId,
                    userId: message.body
                  });
                }
              } catch (error) {
                console.error('Error parsing read status:', error);
              }
            }
          );
          
        } catch (error) {
          console.error('Error subscribing:', error);
        }
      } else if (attempts < maxAttempts) {
        setTimeout(subscribeWhenReady, 100);
      }
    };

    const timeoutId = setTimeout(subscribeWhenReady, 200);

    return () => {
      clearTimeout(timeoutId);
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
        subscriptionRef.current = null;
      }
      if (readSubscriptionRef.current) {
        readSubscriptionRef.current.unsubscribe();
        readSubscriptionRef.current = null;
      }
    };
  }, [conversationId]);

  const disconnect = useCallback(() => {
    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe();
      subscriptionRef.current = null;
    }
    if (readSubscriptionRef.current) {
      readSubscriptionRef.current.unsubscribe();
      readSubscriptionRef.current = null;
    }
    if (clientRef.current) {
      clientRef.current.deactivate();
      clientRef.current = null;
      isConnectedRef.current = false;
    }
  }, []);

  const sendMessage = useCallback((messageData) => {
    if (clientRef.current && isConnectedRef.current && clientRef.current.connected) {
      try {
        clientRef.current.publish({
          destination: '/app/chat.sendMessage',
          body: JSON.stringify(messageData)
        });
      } catch (error) {
        console.error('Error sending message:', error);
      }
    }
  }, []);

  // Connect on mount, disconnect on unmount
  useEffect(() => {
    connect();
    return () => {
      disconnect();
    };
  }, [connect, disconnect]);

  // Handle tab visibility - reconnect when tab becomes active
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        // Tab is active - check connection status
        if (clientRef.current && !clientRef.current.connected) {
          disconnect();
          setTimeout(() => connect(), 500);
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, [connect, disconnect]);

  return {
    sendMessage,
    disconnect,
    isConnected: isConnectedRef.current
  };
};
