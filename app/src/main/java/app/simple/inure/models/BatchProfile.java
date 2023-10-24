package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "batch_profile")
public class BatchProfile implements Parcelable {
    
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
    
    public BatchProfile(String profileName, String packageNames, long dateCreated) {
        this.profileName = profileName;
        this.packageNames = packageNames;
        this.dateCreated = dateCreated;
    }
    
    protected BatchProfile(Parcel in) {
        id = in.readInt();
        profileName = in.readString();
        packageNames = in.readString();
        dateCreated = in.readLong();
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
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(profileName);
        dest.writeString(packageNames);
        dest.writeLong(dateCreated);
    }
}
