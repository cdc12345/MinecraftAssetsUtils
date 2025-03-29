package org.cdc.data;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cdc.Constants;
import org.cdc.utils.URLWrapperUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VersionManifest {
    private final JsonObject original;
    private final String latestStable;
    private final String latestSnapshot;

    private final List<Version> versionList;

    public VersionManifest() throws URISyntaxException, IOException {
        URI manifest = new URI(Constants.VERSIONS_MANIFEST_URL);

        var gson = new GsonBuilder().create();
        original = gson.fromJson(new String(URLWrapperUtils.wrapURI(manifest).getInputStream().readAllBytes()),JsonObject.class);
        var latest = original.get("latest").getAsJsonObject();
        latestSnapshot = latest.get("snapshot").getAsString();
        latestStable = latest.get("release").getAsString();

        versionList = new ArrayList<>();
        var version = original.get("versions").getAsJsonArray();
        for (JsonElement version1: version){
            versionList.add(gson.fromJson(version1, Version.class));
        }
    }

    public String getLatestSnapshot() {
        return latestSnapshot;
    }

    public String getLatestStable() {
        return latestStable;
    }

    public List<Version> getVersionList() {
        return Collections.unmodifiableList(versionList);
    }

    public Version getSpecificVersion(String version){
        return getVersionList().stream().filter(a->version.equals(a.getId())).findFirst().get();
    }

    public Version getLatestStableVersion(){
        return getSpecificVersion(getLatestStable());
    }

    public Version getLatestSnapshotVersion(){
        return getSpecificVersion(getLatestSnapshot());
    }

    public JsonObject getOriginal() {
        return original.deepCopy();
    }
}
