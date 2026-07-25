package com.jiubuntu.wms.global.idempotency;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 멱등성 키 검증을 위해 요청 본문을 미리 읽어 해시를 계산해야 하는데,
 * 원본 HttpServletRequest의 InputStream은 한 번 읽으면 재사용할 수 없어
 * 바디를 캐싱해 여러 번(해시 계산용 + 실제 컨트롤러 역직렬화용) 읽을 수 있게 한다.
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    public byte[] getCachedBody() {
        return cachedBody;
    }

}
