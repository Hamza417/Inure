package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "tags")
public class Tag implements Parcelable {
    
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
    
    @PrimaryKey
    @ColumnInfo (name = "tag")
    @NonNull
    private String tag;
    @ColumnInfo (name = "packages")
    private String packages;
    @ColumnInfo (name = "icon")
    private int icon;
    
    public Tag(@NonNull String tag, String packages, int icon) {
        this.tag = tag;
        this.packages = packages;
        this.icon = icon;
    }
    
    protected Tag(Parcel in) {
        tag = in.readString();
        packages = in.readString();
        icon = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tag);
        dest.writeString(packages);
        dest.writeInt(icon);
    }
    
    @Override
    public int describeContents() {
        return 0;
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
}
