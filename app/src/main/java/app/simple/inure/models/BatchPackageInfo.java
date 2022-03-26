package app.simple.inure.models;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class BatchPackageInfo implements Parcelable {
    
    private PackageInfo packageInfo;
    private boolean selected;
    private long dateSelected;
    
    public BatchPackageInfo(PackageInfo packageInfo, boolean selected, long dateSelected) {
        this.packageInfo = packageInfo;
        this.selected = selected;
        this.dateSelected = dateSelected;
    }
    
    protected BatchPackageInfo(Parcel in) {
        packageInfo = in.readParcelable(PackageInfo.class.getClassLoader());
        selected = in.readByte() != 0;
        dateSelected = in.readLong();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(packageInfo, flags);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeLong(dateSelected);
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
}