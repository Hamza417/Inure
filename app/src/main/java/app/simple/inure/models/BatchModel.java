package app.simple.inure.models;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "batch_state_data")
public class BatchModel implements Parcelable {
    
    @PrimaryKey
    @ColumnInfo (name = "package_name")
    @NonNull
    private String packageName;
    
    @ColumnInfo (name = "selected")
    private boolean isSelected;
    
    @ColumnInfo (name = "package_info")
    private final PackageInfo packageInfo;
    
    public BatchModel(@NonNull String packageName, boolean isSelected, PackageInfo packageInfo) {
        this.packageName = packageName;
        this.isSelected = isSelected;
        this.packageInfo = packageInfo;
    }
    
    protected BatchModel(Parcel in) {
        packageName = in.readString();
        isSelected = in.readByte() != 0;
        packageInfo = in.readParcelable(PackageInfo.class.getClassLoader());
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeParcelable(packageInfo, flags);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <BatchModel> CREATOR = new Creator <BatchModel>() {
        @Override
        public BatchModel createFromParcel(Parcel in) {
            return new BatchModel(in);
        }
        
        @Override
        public BatchModel[] newArray(int size) {
            return new BatchModel[size];
        }
    };
    
    @NonNull
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(@NonNull String packageName) {
        this.packageName = packageName;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
}
