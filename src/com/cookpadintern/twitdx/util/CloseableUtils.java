package com.cookpadintern.twitdx.util;

import java.io.Closeable;
import java.io.IOException;

public final class CloseableUtils {
    private CloseableUtils() {}

    public static void close(Closeable resource) {
        if (resource == null) return;
        try {
            resource.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
