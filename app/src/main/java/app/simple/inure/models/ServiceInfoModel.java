package app.simple.inure.models;

import android.content.pm.ServiceInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class ServiceInfoModel implements Parcelable {
    
    private ServiceInfo serviceInfo;
    private int flags;
    private int foregroundType;
    private boolean enabled;
    private boolean exported;
    private String permissions;
    private String name;
    private String status;
    
    public ServiceInfoModel() {
    }
    
    public ServiceInfoModel(ServiceInfo serviceInfo, int flags, int foregroundType, boolean enabled, boolean exported, String permissions, String name, String status) {
        this.serviceInfo = serviceInfo;
        this.flags = flags;
        this.foregroundType = foregroundType;
        this.enabled = enabled;
        this.exported = exported;
        this.permissions = permissions;
        this.name = name;
        this.status = status;
    }
    
    protected ServiceInfoModel(Parcel in) {
        serviceInfo = in.readParcelable(ServiceInfo.class.getClassLoader());
        flags = in.readInt();
        foregroundType = in.readInt();
        enabled = in.readByte() != 0;
        exported = in.readByte() != 0;
        permissions = in.readString();
        name = in.readString();
        status = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(serviceInfo, flags);
        dest.writeInt(flags);
        dest.writeInt(foregroundType);
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeByte((byte) (exported ? 1 : 0));
        dest.writeString(permissions);
        dest.writeString(name);
        dest.writeString(status);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <ServiceInfoModel> CREATOR = new Creator <ServiceInfoModel>() {
        @Override
        public ServiceInfoModel createFromParcel(Parcel in) {
            return new ServiceInfoModel(in);
        }
        
        @Override
        public ServiceInfoModel[] newArray(int size) {
            return new ServiceInfoModel[size];
        }
    };
    
    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
    
    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    
    public int getFlags() {
        return flags;
    }
    
    public void setFlags(int flags) {
        this.flags = flags;
    }
    
    public int getForegroundType() {
        return foregroundType;
    }
    
    public void setForegroundType(int foregroundType) {
        this.foregroundType = foregroundType;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isExported() {
        return exported;
    }
    
    public void setExported(boolean exported) {
        this.exported = exported;
    }
    
    public String getPermissions() {
        return permissions;
    }
    
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
