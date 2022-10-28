package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

public class PermissionInfo implements Parcelable {
    
    @Nullable
    private android.content.pm.PermissionInfo permissionInfo;
    private int isGranted;
    private String name;
    private String label;
    
    public PermissionInfo(@Nullable android.content.pm.PermissionInfo permissionInfo, int isGranted, String name, String label) {
        this.permissionInfo = permissionInfo;
        this.isGranted = isGranted;
        this.name = name;
        this.label = label;
    }
    
    public PermissionInfo(int isGranted, String name) {
        this.isGranted = isGranted;
        this.name = name;
    }
    
    public PermissionInfo() {
    
    }
    
    protected PermissionInfo(Parcel in) {
        permissionInfo = in.readParcelable(android.content.pm.PermissionInfo.class.getClassLoader());
        isGranted = in.readInt();
        name = in.readString();
        label = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(permissionInfo, flags);
        dest.writeInt(isGranted);
        dest.writeString(name);
        dest.writeString(label);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <PermissionInfo> CREATOR = new Creator <>() {
        @Override
        public PermissionInfo createFromParcel(Parcel in) {
            return new PermissionInfo(in);
        }
        
        @Override
        public PermissionInfo[] newArray(int size) {
            return new PermissionInfo[size];
        }
    };
    
    /**
     * 0 -> Denied<br>
     * 1 -> Granted<br>
     * 2 -> Not Found/Unknown<br>
     */
    public int isGranted() {
        return isGranted;
    }
    
    /**
     * 0 -> Denied<br>
     * 1 -> Granted<br>
     * 2 -> Not Found/Unknown<br>
     */
    public void setGranted(@IntRange (from = 0, to = 2) int granted) {
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
    
    @Nullable
    public android.content.pm.PermissionInfo getPermissionInfo() {
        return permissionInfo;
    }
    
    public void setPermissionInfo(@Nullable android.content.pm.PermissionInfo permissionInfo) {
        this.permissionInfo = permissionInfo;
    }
}
