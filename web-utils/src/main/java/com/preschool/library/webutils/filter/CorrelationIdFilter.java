package com.preschool.library.webutils.filter;

import com.preschool.library.webutils.context.CorrelationIdContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {
    private static final String X_REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String xRequestId = request.getHeader(X_REQUEST_ID);
        log.info(
                "Start handle for X-Request-Id [{}] of path [{}]", xRequestId, request.getRequestURI());
        CorrelationIdContext.setContext(xRequestId);
        filterChain.doFilter(request, response);
    }
}
