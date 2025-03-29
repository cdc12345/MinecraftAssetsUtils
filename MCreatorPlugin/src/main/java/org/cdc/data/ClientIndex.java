package org.cdc.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.cdc.Constants;
import org.cdc.utils.URLWrapperUtils;

import java.net.URI;

public class ClientIndex {

    private final JsonObject client;

    private final int assetIndex;
    private final JsonObject assetsIndexJsonObject;

    public ClientIndex(Version version){
        var gson = new Gson();
        try {
            System.out.println(version.getUrl());
            client = gson.fromJson(new String(URLWrapperUtils.wrapURI(new URI(version.getUrl())).getInputStream().readAllBytes()), JsonObject.class);

            assetIndex = client.get("assetIndex").getAsJsonObject().get("id").getAsInt();
            String assetsIndexUrl = client.get("assetIndex").getAsJsonObject().get("url").getAsString();

            assetsIndexJsonObject = gson.fromJson(new String(URLWrapperUtils.wrapURI(new URI(assetsIndexUrl)).getInputStream().readAllBytes()), JsonObject.class).get("objects").getAsJsonObject();

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject toJsonObject() {
        return client;
    }

    public int getAssetIndex() {
        return assetIndex;
    }

    public JsonObject getAssetsIndexJsonObject() {
        return assetsIndexJsonObject;
    }

    public String getAssetDownloadURL(String path){
        String hash = getAssetsIndexJsonObject().get(path).getAsJsonObject().get("hash").getAsString();
        return Constants.ASSET_URL + "/" + hash.substring(0,2) + "/" + hash;
    }
}
