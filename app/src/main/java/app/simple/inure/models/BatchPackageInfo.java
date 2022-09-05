package app.simple.inure.models;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class BatchPackageInfo implements Parcelable {
    
    private PackageInfo packageInfo;
    private boolean selected;
    private long dateSelected;
    private boolean isCompleted;
    
    public BatchPackageInfo(PackageInfo packageInfo, boolean selected, long dateSelected, boolean isCompleted) {
        this.packageInfo = packageInfo;
        this.selected = selected;
        this.dateSelected = dateSelected;
        this.isCompleted = isCompleted;
    }
    
    public BatchPackageInfo(PackageInfo packageInfo, boolean selected, long dateSelected) {
        this.packageInfo = packageInfo;
        this.selected = selected;
        this.dateSelected = dateSelected;
        this.isCompleted = false;
    }
    
    protected BatchPackageInfo(Parcel in) {
        packageInfo = in.readParcelable(PackageInfo.class.getClassLoader());
        selected = in.readByte() != 0;
        dateSelected = in.readLong();
        isCompleted = in.readByte() != 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(packageInfo, flags);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeLong(dateSelected);
        dest.writeByte((byte) (isCompleted ? 1 : 0));
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <BatchPackageInfo> CREATOR = new Creator <BatchPackageInfo>() {
        @Override
        public BatchPackageInfo createFromParcel(Parcel in) {
            return new BatchPackageInfo(in);
        }
        
        @Override
        public BatchPackageInfo[] newArray(int size) {
            return new BatchPackageInfo[size];
        }
    };
    
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
    
    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public long getDateSelected() {
        return dateSelected;
    }
    
    public void setDateSelected(long dateSelected) {
        this.dateSelected = dateSelected;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}