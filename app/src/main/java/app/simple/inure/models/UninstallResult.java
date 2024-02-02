package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class UninstallResult implements Parcelable {
    
    private String packageName;
    private boolean success;
    
    public UninstallResult(String packageName, boolean success) {
        this.packageName = packageName;
        this.success = success;
    }
    
    protected UninstallResult(Parcel in) {
        packageName = in.readString();
        success = in.readByte() != 0;
    }
    
    public static final Creator <UninstallResult> CREATOR = new Creator <UninstallResult>() {
        @Override
        public UninstallResult createFromParcel(Parcel in) {
            return new UninstallResult(in);
        }
        
        @Override
        public UninstallResult[] newArray(int size) {
            return new UninstallResult[size];
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
