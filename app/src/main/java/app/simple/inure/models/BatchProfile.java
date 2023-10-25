package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "batch_profile")
public class BatchProfile implements Parcelable {
    
    @PrimaryKey (autoGenerate = true)
    @ColumnInfo (name = "id")
    private int id;
    
    @ColumnInfo (name = "profile_name")
    private String profileName;
    
    /**
     * Use a delimiter to separate package names and date selected
     * Example: app.simple.inure_1620000000000,app.simple.inure_1620000000000
     */
    @ColumnInfo (name = "package_names")
    private String packageNames;
    
    @ColumnInfo (name = "date_created")
    private long dateCreated;
    
    public static final Creator <BatchProfile> CREATOR = new Creator <BatchProfile>() {
        @Override
        public BatchProfile createFromParcel(Parcel in) {
            return new BatchProfile(in);
        }
        
        @Override
        public BatchProfile[] newArray(int size) {
            return new BatchProfile[size];
        }
    };
    @ColumnInfo (name = "sort_style")
    private int filterStyle;
    
    public BatchProfile(String profileName, String packageNames, long dateCreated, int filterStyle) {
        this.profileName = profileName;
        this.packageNames = packageNames;
        this.dateCreated = dateCreated;
        this.filterStyle = filterStyle;
    }
    
    protected BatchProfile(Parcel in) {
        id = in.readInt();
        profileName = in.readString();
        packageNames = in.readString();
        dateCreated = in.readLong();
        filterStyle = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(profileName);
        dest.writeString(packageNames);
        dest.writeLong(dateCreated);
        dest.writeLong(filterStyle);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public String getProfileName() {
        return profileName;
    }
    
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    
    public String getPackageNames() {
        return packageNames;
    }
    
    public void setPackageNames(String packageNames) {
        this.packageNames = packageNames;
    }
    
    public long getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public int getFilterStyle() {
        return filterStyle;
    }
    
    public void setFilterStyle(int sortStyle) {
        this.filterStyle = sortStyle;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
}
