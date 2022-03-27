package app.simple.inure.models;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class NotesPackageInfo implements Parcelable {
    
    private PackageInfo packageInfo;
    private String note;
    private long dateCreated;
    private long dateUpdated;
    
    public NotesPackageInfo(PackageInfo packageInfo, String note, long dateCreated, long dateUpdated) {
        this.packageInfo = packageInfo;
        this.note = note;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }
    
    protected NotesPackageInfo(Parcel in) {
        packageInfo = in.readParcelable(PackageInfo.class.getClassLoader());
        note = in.readString();
        dateCreated = in.readLong();
        dateUpdated = in.readLong();
    }
    
    public static final Creator <NotesPackageInfo> CREATOR = new Creator <NotesPackageInfo>() {
        @Override
        public NotesPackageInfo createFromParcel(Parcel in) {
            return new NotesPackageInfo(in);
        }
        
        @Override
        public NotesPackageInfo[] newArray(int size) {
            return new NotesPackageInfo[size];
        }
    };
    
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
    
    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public long getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public long getDateUpdated() {
        return dateUpdated;
    }
    
    public void setDateUpdated(long dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(packageInfo, flags);
        dest.writeString(note);
        dest.writeLong(dateCreated);
        dest.writeLong(dateUpdated);
    }
}
