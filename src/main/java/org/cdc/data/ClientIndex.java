package org.cdc.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.cdc.Constants;
import org.cdc.utils.URLWrapperUtils;

import java.net.URI;

public class ClientIndex implements IVersionInformationProvider{

    private final JsonObject client;

    private final int assetIndex;
    private final JsonObject assetsIndexJsonObject;
    private final String id;
    private final String type;

    public ClientIndex(Version version){
        var gson = new Gson();
        try {
            this.id = version.getId();
            this.type = version.getType();
            client = gson.fromJson(new String(URLWrapperUtils.wrapURI(new URI(version.getUrl())).getInputStream().readAllBytes()), JsonObject.class);

            var assetIndexS = client.get("assetIndex").getAsJsonObject();
            assetIndex = assetIndexS.get("id").getAsInt();
            String assetsIndexUrl = assetIndexS.getAsJsonObject().get("url").getAsString();

            assetsIndexJsonObject = gson.fromJson(new String(URLWrapperUtils.wrapURI(new URI(assetsIndexUrl)).getInputStream().readAllBytes()), JsonObject.class).get("objects").getAsJsonObject();

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject getJsonObject() {
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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }
}
