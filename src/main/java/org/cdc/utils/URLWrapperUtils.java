package org.cdc.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;

public class URLWrapperUtils {

    public static URLConnection wrapURI(URI uri) throws IOException {
        var url = uri.toURL();
        var conn = url.openConnection();
        conn.addRequestProperty("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0");
        return conn;
    }
}
