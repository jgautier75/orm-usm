package com.acme.users.mgt.logging.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

/**
 * Cached inputStream.
 */
public class CachedInputStream extends ServletInputStream {
    private InputStream cachedBodyInputStream;

    public CachedInputStream(byte[] cachedBody) {
        this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public boolean isFinished() {
        try {
            return cachedBodyInputStream.available() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        // Disable listener
    }

    @Override
    public int read() throws IOException {
        return cachedBodyInputStream.read();
    }
}
