package com.jk.common.security.xss;

import org.apache.commons.lang3.RandomUtils;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * 请求seq过滤器
 * @author cuiP
 */
public class LogSeqFilter implements Filter {

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
		MDC.put("seq", UUID.randomUUID().toString());
		try {
			chain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}

	@Override
	public void destroy() {
	}

}