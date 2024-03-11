package com.preschool.library.webutils.filter;

import com.preschool.library.webutils.context.CorrelationIdContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {
    private static final String X_REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String xRequestId = request.getHeader(X_REQUEST_ID);
        CorrelationIdContext.setContext(xRequestId);
        filterChain.doFilter(request, response);
    }
}
