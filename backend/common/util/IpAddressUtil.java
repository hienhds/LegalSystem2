package com.example.backend.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to extract client IP address from HTTP request
 */
@Slf4j
public class IpAddressUtil {
    
    private static final String[] IP_HEADER_CANDIDATES = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    };
    
    /**
     * Get client IP address from HTTP request
     * Handles X-Forwarded-For header for proxied requests
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                log.debug("Client IP from header {}: {}", header, ip);
                return ip;
            }
        }
        
        String remoteAddr = request.getRemoteAddr();
        log.debug("Client IP from remote address: {}", remoteAddr);
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}
