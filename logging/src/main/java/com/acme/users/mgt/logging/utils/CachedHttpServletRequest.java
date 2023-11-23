package com.acme.users.mgt.logging.utils;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.util.StreamUtils;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Http servlet request with cached body.
 */
public class CachedHttpServletRequest extends HttpServletRequestWrapper {
    private byte[] cachedBody;

    public CachedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        try (InputStream requestInputStream = request.getInputStream()) {
            this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
        }
    }

    public CachedHttpServletRequest(HttpServletRequest request, byte[] cachedBody) {
        super(request);
        this.cachedBody = cachedBody;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedInputStream(this.cachedBody);
    }

    public byte[] getCachedBody() {
        return cachedBody;
    }

}
