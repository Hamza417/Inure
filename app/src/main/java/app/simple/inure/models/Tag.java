package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "tags")
public class Tag implements Parcelable {
    
    @PrimaryKey
    @ColumnInfo (name = "tag")
    @NonNull
    private String tag;
    @ColumnInfo (name = "packages")
    private String packages;
    @ColumnInfo (name = "icon")
    private int icon;
    
    public static final Creator <Tag> CREATOR = new Creator <>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }
        
        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
    
    @ColumnInfo (name = "date_added")
    private long dateAdded;
    
    public Tag(@NonNull String tag, String packages, int icon) {
        this.tag = tag;
        this.packages = packages;
        this.icon = icon;
        this.dateAdded = System.currentTimeMillis();
    }
    
    protected Tag(Parcel in) {
        tag = in.readString();
        packages = in.readString();
        icon = in.readInt();
        dateAdded = in.readLong();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        
        dest.writeString(tag);
        dest.writeString(packages);
        dest.writeInt(icon);
        dest.writeLong(dateAdded);
    }
    
    @NonNull
    public String getTag() {
        return tag;
    }
    
    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }
    
    public String getPackages() {
        return packages;
    }
    
    public void setPackages(String packages) {
        this.packages = packages;
    }
    
    public int getIcon() {
        return icon;
    }
    
    public void setIcon(int icon) {
        this.icon = icon;
    }
    
    public long getDateAdded() {
        return dateAdded;
    }
    
    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "Tag{" +
                "tag='" + tag + '\'' +
                ", packages='" + packages + '\'' +
                ", icon=" + icon +
                ", dateAdded=" + dateAdded +
                '}';
    }
}
