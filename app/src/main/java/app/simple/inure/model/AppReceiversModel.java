package app.simple.inure.model;

import android.content.pm.ActivityInfo;

public class AppReceiversModel {
    private ActivityInfo activityInfo;
    private String status;
    private String name;
    private String permission;
    private Boolean exported;
    
    public AppReceiversModel() {
    }
    
    public AppReceiversModel(ActivityInfo activityInfo, String status, String name, String permission, Boolean exported) {
        this.activityInfo = activityInfo;
        this.status = status;
        this.name = name;
        this.permission = permission;
        this.exported = exported;
    }
    
    public ActivityInfo getActivityInfo() {
        return activityInfo;
    }
    
    public void setActivityInfo(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    public Boolean getExported() {
        return exported;
    }
    
    public void setExported(Boolean exported) {
        this.exported = exported;
    }
}
