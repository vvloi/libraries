package com.preschool.libraries.base.interceptor;

import static feign.Util.UTF_8;

import feign.Response;
import java.io.*;
import java.nio.charset.Charset;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public class CachedBody implements Response.Body {
    private InputStream inputStream;
    private int length;

    public CachedBody(Response.Body source) throws IOException {
        this.inputStream = source.asInputStream();
        this.length = source.length();
    }

    @SneakyThrows
    @Override
    public Integer length() {
        return length;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public InputStream asInputStream() throws IOException {
        return new ByteArrayInputStream(inputStream.readAllBytes());
    }

    @Override
    public Reader asReader(Charset charset) throws IOException {
        return new InputStreamReader(inputStream, UTF_8);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
