package app.simple.inure.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PermissionInfo implements Parcelable {
    
    private boolean isGranted;
    private String name;
    
    public PermissionInfo(boolean isGranted, String name) {
        this.isGranted = isGranted;
        this.name = name;
    }
    
    protected PermissionInfo(Parcel in) {
        isGranted = in.readByte() != 0;
        name = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isGranted ? 1 : 0));
        dest.writeString(name);
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
}
