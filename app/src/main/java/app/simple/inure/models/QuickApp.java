package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "quick_apps")
public class QuickApp implements Parcelable {
    
    @ColumnInfo (name = "date_added")
    private long date;
    
    @PrimaryKey
    @ColumnInfo (name = "package_id")
    @NonNull
    private String packageName;
    
    public QuickApp(long date, @NonNull String packageName) {
        this.date = date;
        this.packageName = packageName;
    }
    
    protected QuickApp(Parcel in) {
        date = in.readLong();
        packageName = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(date);
        dest.writeString(packageName);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <QuickApp> CREATOR = new Creator <>() {
        @Override
        public QuickApp createFromParcel(Parcel in) {
            return new QuickApp(in);
        }
        
        @Override
        public QuickApp[] newArray(int size) {
            return new QuickApp[size];
        }
    };
    
    @NonNull
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(@NonNull String packageName) {
        this.packageName = packageName;
    }
    
    public long getDate() {
        return date;
    }
    
    public void setDate(long date) {
        this.date = date;
    }
}
