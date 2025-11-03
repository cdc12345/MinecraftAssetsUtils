package org.cdc;

import org.cdc.data.VersionManifest;
import org.cdc.utils.URLUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

/**
 * This is a main class
 */
public class Main {

    public static void main(String[] args) throws URISyntaxException, IOException {
        VersionManifest versionManifest = new VersionManifest();

        System.out.println(versionManifest.getLatestStable());

        System.out.println(versionManifest.getLatestStableVersion());

        System.out.println(versionManifest.getLatestStableVersion().getClient().getAssetIndex());

        var locale = Locale.getDefault();
        String name = locale.toString().toLowerCase(Locale.ROOT);

        System.out.println(name);

        Files.copy(URLUtils.wrapURI(new URI(versionManifest.getLatestStableVersion().getClient().getAssetDownloadURL("minecraft/lang/"+name+".json"))).getInputStream(), Path.of("cache"), StandardCopyOption.REPLACE_EXISTING);

        System.out.println(URLUtils.getLanguageURL(locale,versionManifest.getLatestStableVersion().getClient()));
    }
}
