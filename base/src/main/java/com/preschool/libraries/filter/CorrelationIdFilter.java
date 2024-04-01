package com.preschool.libraries.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preschool.libraries.context.CorrelationIdContext;
import com.preschool.libraries.exception.LibraryErrorCode;
import com.preschool.libraries.response.Response;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class CorrelationIdFilter extends OncePerRequestFilter {
    private static final String X_REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String xRequestId = request.getHeader(X_REQUEST_ID);
        log.info(
                "Start handle for X-Request-Id [{}] of path [{}]", xRequestId, request.getRequestURI());
        if (!StringUtils.hasText(xRequestId)) {
            log.error("Missing header x-request-id");
            buildMissingRequestIdHeaderResponse(response);
            return;
        }
        CorrelationIdContext.setContext(xRequestId);
        filterChain.doFilter(request, response);
    }

    @SneakyThrows
    private void buildMissingRequestIdHeaderResponse(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ServletOutputStream out = response.getOutputStream();
        Response<Void> error = Response.error(LibraryErrorCode.MISSING_REQUEST_ID_HEADER);
        new ObjectMapper().writeValue(out, error);
        out.flush();
    }
}
