package com.jk.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
public class HttpRequestUtil {

    private HttpRequestUtil(){
    	
    }
    
    public static String encodeUrl(String originalUrl){
    	if(originalUrl != null){
        	try {
    			return URLEncoder.encode(originalUrl, "utf-8");
    		} catch (UnsupportedEncodingException e) {
    			log.error("Url encode error for " + originalUrl, e);
    		}
    	}
    	return originalUrl;
    }

    /**
     * get client ip address
     * @param request client request
     * @return ip address
     */
    public static String ipAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getUserAgent(HttpServletRequest request){
        return request.getHeader("User-Agent");
    }
    
    public static String extractRequestUrl(HttpServletRequest request){
        return extractRequestUrl(request, true);
    }
    
    /**
     *  get request url
     * @param request
     * @param includingQuery
     * @return request url
     */
    public static String extractRequestUrl(HttpServletRequest request, boolean includingQuery){
        String url = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String query = request.getQueryString();
        if (pathInfo != null || (query != null && includingQuery)) {
            StringBuilder sb = new StringBuilder(url);
            if (pathInfo != null) {
                sb.append(pathInfo);
            }
            if (query != null && includingQuery) {
                sb.append('?').append(query);
            }
            url = sb.toString();
        }
        return url;
    }
    
}
