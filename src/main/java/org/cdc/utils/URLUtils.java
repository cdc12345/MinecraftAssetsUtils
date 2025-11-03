package org.cdc.utils;

import org.cdc.data.ClientIndex;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.Locale;

public class URLUtils {

    public static URLConnection wrapURI(URI uri) throws IOException {
        var url = uri.toURL();
        URLConnection conn = url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.addRequestProperty("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0");
        return conn;
    }

    public static String getLanguageURL(Locale locale, ClientIndex clientIndex){
        return clientIndex.getAssetDownloadURL("minecraft/lang/"+locale.toString().toLowerCase()+".json");
    }
}
