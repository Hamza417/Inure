package app.simple.inure.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PermissionInfo implements Parcelable {
    
    private android.content.pm.PermissionInfo permissionInfo;
    private boolean isGranted;
    private String name;
    private String label;
    
    public PermissionInfo(android.content.pm.PermissionInfo permissionInfo, boolean isGranted, String name, String label) {
        this.permissionInfo = permissionInfo;
        this.isGranted = isGranted;
        this.name = name;
        this.label = label;
    }
    
    public PermissionInfo(boolean isGranted, String name) {
        this.isGranted = isGranted;
        this.name = name;
    }
    
    protected PermissionInfo(Parcel in) {
        permissionInfo = in.readParcelable(android.content.pm.PermissionInfo.class.getClassLoader());
        isGranted = in.readByte() != 0;
        name = in.readString();
        label = in.readString();
    }
    
    public PermissionInfo() {
    
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(permissionInfo, flags);
        dest.writeByte((byte) (isGranted ? 1 : 0));
        dest.writeString(name);
        dest.writeString(label);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <PermissionInfo> CREATOR = new Creator <PermissionInfo>() {
        @Override
        public PermissionInfo createFromParcel(Parcel in) {
            return new PermissionInfo(in);
        }
        
        @Override
        public PermissionInfo[] newArray(int size) {
            return new PermissionInfo[size];
        }
    };
    
    public boolean isGranted() {
        return isGranted;
    }
    
    public void setGranted(boolean granted) {
        isGranted = granted;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public android.content.pm.PermissionInfo getPermissionInfo() {
        return permissionInfo;
    }
    
    public void setPermissionInfo(android.content.pm.PermissionInfo permissionInfo) {
        this.permissionInfo = permissionInfo;
    }
}
