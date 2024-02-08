package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.NonNull;

public class PackageStateResult implements Parcelable {
    
    private String name;
    private String packageName;
    private boolean success;
    
    public PackageStateResult(String name, String packageName, boolean success) {
        this.name = name;
        this.packageName = packageName;
        this.success = success;
    }
    
    protected PackageStateResult(Parcel in) {
        name = in.readString();
        packageName = in.readString();
        success = in.readByte() != 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(packageName);
        dest.writeByte((byte) (success ? 1 : 0));
    }
    
    @Override
    public int describeContents() {
        return 0;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "PackageStateResult{" +
                "name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                ", success=" + success +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageStateResult that = (PackageStateResult) o;
        return success == that.success && Objects.equals(name, that.name) && Objects.equals(packageName, that.packageName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, packageName, success);
    }
}
