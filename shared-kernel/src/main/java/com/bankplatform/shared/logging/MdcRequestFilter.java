package com.bankplatform.shared.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class MdcRequestFilter implements Filter {
    private static final String TRACE_HEADER = "X-B3-TraceId";

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try{
            populateMdc(httpRequest);
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private void populateMdc(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_HEADER);
        if(traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put("traceId", traceId);
        MDC.put("service", serviceName);
        MDC.put("httpMethod", request.getMethod());
        MDC.put("path", request.getRequestURI());
    }
}
