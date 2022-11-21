package app.simple.inure.models;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class BatteryOptimizationModel implements Parcelable {
    
    public static final String TYPE_IGNORE_BATTERY_OPTIMIZATIONS = "TYPE_IGNORE_BATTERY_OPTIMIZATIONS";
    public static final String TYPE_SYSTEM_EXCIDLE = "system-excidle";
    public static final String TYPE_SYSTEM = "system";
    public static final String TYPE_USER = "user";
    
    private PackageInfo packageInfo;
    private String type;
    private boolean isOptimized;
    
    public BatteryOptimizationModel(PackageInfo packageInfo, String type, boolean isOptimized) {
        this.packageInfo = packageInfo;
        this.type = type;
        this.isOptimized = isOptimized;
    }
    
    public BatteryOptimizationModel() {
    
    }
    
    protected BatteryOptimizationModel(Parcel in) {
        packageInfo = in.readParcelable(PackageInfo.class.getClassLoader());
        type = in.readString();
        isOptimized = in.readByte() != 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(packageInfo, flags);
        dest.writeString(type);
        dest.writeByte((byte) (isOptimized ? 1 : 0));
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <BatteryOptimizationModel> CREATOR = new Creator <BatteryOptimizationModel>() {
        @Override
        public BatteryOptimizationModel createFromParcel(Parcel in) {
            return new BatteryOptimizationModel(in);
        }
        
        @Override
        public BatteryOptimizationModel[] newArray(int size) {
            return new BatteryOptimizationModel[size];
        }
    };
    
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
    
    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isOptimized() {
        return isOptimized;
    }
    
    public void setOptimized(boolean optimized) {
        isOptimized = optimized;
    }
}
