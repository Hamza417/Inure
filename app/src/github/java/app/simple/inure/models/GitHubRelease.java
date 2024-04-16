package app.simple.inure.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GitHubRelease {
    @SerializedName ("tag_name")
    private String tagName;
    
    @SerializedName ("assets")
    private List <GitHubReleaseAsset> assets;
    
    public String getTagName() {
        return tagName;
    }
    
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    public List <GitHubReleaseAsset> getAssets() {
        return assets;
    }
    
    public void setAssets(List <GitHubReleaseAsset> assets) {
        this.assets = assets;
    }
}