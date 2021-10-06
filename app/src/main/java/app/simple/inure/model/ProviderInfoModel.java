package app.simple.inure.model;

import android.content.pm.ProviderInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class ProviderInfoModel implements Parcelable {
    
    private ProviderInfo providerInfo;
    private String authority;
    private String permissions;
    private String name;
    private String status;
    private boolean exported;
    private boolean enabled;
    
    public ProviderInfoModel() {
    }
    
    public ProviderInfoModel(ProviderInfo providerInfo, String authority, String permissions, String name, String status, boolean exported, boolean enabled) {
        this.providerInfo = providerInfo;
        this.authority = authority;
        this.permissions = permissions;
        this.name = name;
        this.status = status;
        this.exported = exported;
        this.enabled = enabled;
    }
    
    protected ProviderInfoModel(Parcel in) {
        providerInfo = in.readParcelable(ProviderInfo.class.getClassLoader());
        authority = in.readString();
        permissions = in.readString();
        name = in.readString();
        status = in.readString();
        exported = in.readByte() != 0;
        enabled = in.readByte() != 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(providerInfo, flags);
        dest.writeString(authority);
        dest.writeString(permissions);
        dest.writeString(name);
        dest.writeString(status);
        dest.writeByte((byte) (exported ? 1 : 0));
        dest.writeByte((byte) (enabled ? 1 : 0));
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <ProviderInfoModel> CREATOR = new Creator <ProviderInfoModel>() {
        @Override
        public ProviderInfoModel createFromParcel(Parcel in) {
            return new ProviderInfoModel(in);
        }
        
        @Override
        public ProviderInfoModel[] newArray(int size) {
            return new ProviderInfoModel[size];
        }
    };
    
    public ProviderInfo getProviderInfo() {
        return providerInfo;
    }
    
    public void setProviderInfo(ProviderInfo providerInfo) {
        this.providerInfo = providerInfo;
    }
    
    public String getAuthority() {
        return authority;
    }
    
    public void setAuthority(String authority) {
        this.authority = authority;
    }
    
    public String getPermissions() {
        return permissions;
    }
    
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
    
    public boolean isExported() {
        return exported;
    }
    
    public void setExported(boolean exported) {
        this.exported = exported;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
