package app.simple.inure.models;

import android.content.pm.ActivityInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class ActivityInfoModel implements Parcelable {
    private ActivityInfo activityInfo;
    private String status;
    private String name;
    private String permission;
    private String trackerId;
    private Boolean exported;
    private String target;
    private boolean enabled;
    private boolean isReceiver = false;
    private boolean isActivity = false;
    
    public ActivityInfoModel() {
    }
    
    public ActivityInfoModel(ActivityInfo activityInfo, String status, String name, String permission, String trackerId, Boolean exported, String target, boolean enabled, boolean isReceiver, boolean isActivity) {
        this.activityInfo = activityInfo;
        this.status = status;
        this.name = name;
        this.permission = permission;
        this.trackerId = trackerId;
        this.exported = exported;
        this.target = target;
        this.enabled = enabled;
        this.isReceiver = isReceiver;
        this.isActivity = isActivity;
    }
    
    protected ActivityInfoModel(Parcel in) {
        activityInfo = in.readParcelable(ActivityInfo.class.getClassLoader());
        status = in.readString();
        name = in.readString();
        permission = in.readString();
        trackerId = in.readString();
        byte tmpExported = in.readByte();
        exported = tmpExported == 0 ? null : tmpExported == 1;
        target = in.readString();
        enabled = in.readByte() != 0;
        isReceiver = in.readByte() != 0;
        isActivity = in.readByte() != 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(activityInfo, flags);
        dest.writeString(status);
        dest.writeString(name);
        dest.writeString(permission);
        dest.writeString(trackerId);
        dest.writeByte((byte) (exported == null ? 0 : exported ? 1 : 2));
        dest.writeString(target);
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeByte((byte) (isReceiver ? 1 : 0));
        dest.writeByte((byte) (isActivity ? 1 : 0));
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <ActivityInfoModel> CREATOR = new Creator <ActivityInfoModel>() {
        @Override
        public ActivityInfoModel createFromParcel(Parcel in) {
            return new ActivityInfoModel(in);
        }
        
        @Override
        public ActivityInfoModel[] newArray(int size) {
            return new ActivityInfoModel[size];
        }
    };
    
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
    
    public String getTrackerId() {
        return trackerId;
    }
    
    public void setTrackerId(String trackerId) {
        this.trackerId = trackerId;
    }
    
    public Boolean getExported() {
        return exported;
    }
    
    public void setExported(Boolean exported) {
        this.exported = exported;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isReceiver() {
        return isReceiver;
    }
    
    public void setReceiver(boolean receiver) {
        isReceiver = receiver;
    }
    
    public boolean isActivity() {
        return isActivity;
    }
    
    public void setActivity(boolean activity) {
        isActivity = activity;
    }
}
