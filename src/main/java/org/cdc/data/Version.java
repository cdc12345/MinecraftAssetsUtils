package org.cdc.data;

import com.google.gson.annotations.SerializedName;

/**
 * "id": "1.21.5", "type": "release", "url": "https://piston-meta.mojang.com/v1/packages/09fe09042d1767d677649d1e4a9d26ce3b462ebb/1.21.5.json", "time": "2025-03-25T12:24:50+00:00", "releaseTime": "2025-03-25T12:14:58+00:00", "sha1": "09fe09042d1767d677649d1e4a9d26ce3b462ebb", "complianceLevel": 1
 */

public class Version implements IVersionInformationProvider,Cloneable{

    @SerializedName("id")
    private String id;
    @SerializedName("type")
    private String type;
    @SerializedName("url")
    private String url;
    @SerializedName("sha1")
    private String sha1;

    private ClientIndex clientIndex;


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    public boolean isRelease(){
        return type.equals("release");
    }

    public boolean isSnapshot(){
        return type.equals("snapshot");
    }

    public String getUrl() {
        return url;
    }

    public String getSha1() {
        return sha1;
    }

    public ClientIndex getClient(){
        if (clientIndex == null){
            clientIndex = new ClientIndex(this);
        }
        return clientIndex;
    }

    @Override
    protected Object clone() {
        Version version = new Version();
        version.type = type;
        version.id = id;
        version.url = url;
        version.sha1 = sha1;
        version.clientIndex = clientIndex;
        return version;
    }

    @Override
    public String toString() {
        return "Version{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", sha1='" + sha1 + '\'' +
                '}';
    }
}
