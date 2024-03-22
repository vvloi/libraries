package com.preschool.library.webutils.filter.requestcache;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.util.StreamUtils;

@Getter
public class PayloadCachingRequest extends HttpServletRequestWrapper {
    private final byte[] cachedPayload;

    @SneakyThrows
    public PayloadCachingRequest(HttpServletRequest request) {
        super(request);
        InputStream requestInputStream = request.getInputStream();
        cachedPayload = StreamUtils.copyToByteArray(requestInputStream);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedPayloadServletInputStream(cachedPayload);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedPayload);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }

    public static class CachedPayloadServletInputStream extends ServletInputStream {

        private final InputStream cachedPayloadInputStream;

        public CachedPayloadServletInputStream(byte[] cachedBody) {
            cachedPayloadInputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        @SneakyThrows
        public boolean isFinished() {
            return cachedPayloadInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return cachedPayloadInputStream.read();
        }
    }
}
