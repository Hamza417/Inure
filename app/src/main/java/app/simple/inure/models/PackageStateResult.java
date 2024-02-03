package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class PackageStateResult implements Parcelable {
    
    private String packageName;
    private boolean success;
    
    public PackageStateResult(String packageName, boolean success) {
        this.packageName = packageName;
        this.success = success;
    }
    
    protected PackageStateResult(Parcel in) {
        packageName = in.readString();
        success = in.readByte() != 0;
    }
    
    public static final Creator <PackageStateResult> CREATOR = new Creator <>() {
        @Override
        public PackageStateResult createFromParcel(Parcel in) {
            return new PackageStateResult(in);
        }
        
        @Override
        public PackageStateResult[] newArray(int size) {
            return new PackageStateResult[size];
        }
    };
    
    public String getPackageName() {
        return packageName;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "UninstallResult{" +
                "packageName='" + packageName + '\'' +
                ", success=" + success +
                '}';
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeByte((byte) (success ? 1 : 0));
    }
}
