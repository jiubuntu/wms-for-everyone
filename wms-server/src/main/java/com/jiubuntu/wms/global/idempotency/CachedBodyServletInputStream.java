package com.jiubuntu.wms.global.idempotency;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;

public class CachedBodyServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream buffer;

    public CachedBodyServletInputStream(byte[] cachedBody) {
        this.buffer = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public boolean isFinished() {
        return buffer.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException("비동기 읽기는 지원하지 않습니다.");
    }

    @Override
    public int read() {
        return buffer.read();
    }

}
