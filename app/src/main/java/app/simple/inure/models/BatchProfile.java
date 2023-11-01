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
    @ColumnInfo (name = "filter_style")
    private int filterStyle;
    @ColumnInfo (name = "sort_style")
    private String sortStyle;
    @ColumnInfo (name = "reversed")
    private boolean reversed;
    
    @ColumnInfo (name = "date_created")
    private long dateCreated;
    @ColumnInfo (name = "app_type")
    private String appType;
    
    public BatchProfile(String profileName, String packageNames, int filterStyle, String sortStyle, boolean reversed, String appType, long dateCreated) {
        this.profileName = profileName;
        this.packageNames = packageNames;
        this.filterStyle = filterStyle;
        this.sortStyle = sortStyle;
        this.reversed = reversed;
        this.appType = appType;
        this.dateCreated = dateCreated;
    }
    
    protected BatchProfile(Parcel in) {
        id = in.readInt();
        profileName = in.readString();
        packageNames = in.readString();
        filterStyle = in.readInt();
        sortStyle = in.readString();
        reversed = in.readByte() != 0;
        appType = in.readString();
        dateCreated = in.readLong();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(profileName);
        dest.writeString(packageNames);
        dest.writeInt(filterStyle);
        dest.writeString(sortStyle);
        dest.writeByte((byte) (reversed ? 1 : 0));
        dest.writeString(appType);
        dest.writeLong(dateCreated);
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public int getFilterStyle() {
        return filterStyle;
    }
    
    public void setFilterStyle(int filterStyle) {
        this.filterStyle = filterStyle;
    }
    
    public String getSortStyle() {
        return sortStyle;
    }
    
    public void setSortStyle(String sortStyle) {
        this.sortStyle = sortStyle;
    }
    
    public boolean isReversed() {
        return reversed;
    }
    
    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }
    
    public String getAppType() {
        return appType;
    }
    
    public void setAppType(String appType) {
        this.appType = appType;
    }
    
    public long getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }
}
